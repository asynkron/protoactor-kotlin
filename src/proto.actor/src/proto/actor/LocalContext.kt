package proto.actor

import proto.mailbox.IMessageInvoker
import proto.mailbox.ResumeMailbox
import proto.mailbox.SuspendMailbox
import java.time.Duration
import java.util.*

internal enum class ContextState {
    None, Alive, Restarting, Stopping
}

class LocalContext(producer: () -> IActor, supervisorStrategy: ISupervisorStrategy, receiveMiddleware: (IContext) -> Task, senderMiddleware: (ISenderContext, PID, MessageEnvelope) -> Task, override val parent: PID?) : IMessageInvoker, IContext, ISupervisor {
    companion object {
        internal fun defaultReceive (context : IContext) : Task {
            val c : LocalContext = context as LocalContext
            if (c.message is PoisonPill) {
                c.self.stop()
                return Actor.Done
            }
            return c.actor.receiveAsync(context)
        }
        internal fun defaultSender (_ : ISenderContext, target : PID, envelope : MessageEnvelope) {
            target.ref().sendUserMessage(target, envelope)
        }
    }
    val EmptyChildren : Collection<PID> = listOf()
    private val _producer : () -> IActor = producer
    private val _receiveMiddleware : (IContext) -> Task = receiveMiddleware
    private val _senderMiddleware : (ISenderContext, PID, MessageEnvelope) -> Task = senderMiddleware
    private val _supervisorStrategy : ISupervisorStrategy = supervisorStrategy
    private var _children : MutableSet<PID>? = null
    private var _message : Any = Any()
    private var _receiveTimeoutTimer : Timer? = null
    private var _restartStatistics : RestartStatistics? = null
    private var _stash : Stack<Any>? = null
    private var _state : ContextState = ContextState.None
    private var _watchers : MutableSet<PID>? = null
    override val children : Collection<PID>
        get() = _children?.toList() ?: EmptyChildren
    override lateinit var actor : IActor
    override lateinit var self : PID
    val message : Any = ""
    override val sender : PID?
        get() {
            val (_,sender,_) = MessageEnvelope.unwrap(_message)
            return sender
        }
    val headers : MessageHeader = NullMessageHeader
    override var receiveTimeout : Duration = Duration.ZERO
    override fun stash () {
        if (_stash == null) {
            _stash = Stack<Any>()
        }
        _stash!!.push(message)
    }
    override fun respond (message : Any) {
        sender!!.tell(message)
    }
    override fun spawn (props : Props) : PID {
        val id : String = ProcessRegistry.instance.nextId()
        return spawnNamed(props, id)
    }
    override fun spawnPrefix (props : Props, prefix : String) : PID {
        val name : String = prefix + ProcessRegistry.instance.nextId()
        return spawnNamed(props, name)
    }
    override fun spawnNamed (props : Props, name : String) : PID {
        val pid : PID = props.spawn("${self.id}/$name", self)
        if (_children == null) {
            _children = mutableSetOf()
        }
        _children!!.add(pid)
        return pid
    }
    override fun watch (pid : PID) {
        pid.sendSystemMessage(Watch(self))
    }
    override fun unwatch (pid : PID) {
        pid.sendSystemMessage(Unwatch(self))
    }
    override fun setReceiveTimeout (duration : Duration) {
        if (duration <= Duration.ZERO) {
            throw IllegalArgumentException("duration")
        }
        if (duration == receiveTimeout) {
            return 
        }
        stopReceiveTimeout()
        receiveTimeout = duration
        if (_receiveTimeoutTimer == null) {
            _receiveTimeoutTimer = Timer(receiveTimeoutCallback, null, receiveTimeout, receiveTimeout)
        } else {
            resetReceiveTimeout()
        }
    }
    override fun cancelReceiveTimeout () {
        if (_receiveTimeoutTimer == null) {
            return 
        }
        stopReceiveTimeout()
        _receiveTimeoutTimer = null
        receiveTimeout = Duration.ZERO
    }
    override fun receiveAsync (message : Any) : Task {
        return processMessageAsync(message)
    }
    override fun tell (target : PID, message : Any) {
        sendUserMessage(target, message)
    }
    override fun request (target : PID, message : Any) {
        val messageEnvelope : MessageEnvelope = MessageEnvelope(message, self, NullMessageHeader)
        sendUserMessage(target, messageEnvelope)
    }
    override fun requestAsync (target : PID, message : Any, timeout : Duration) : Task = requestAsync(target, message, FutureProcess<T>(timeout))
    override fun requestAsync (target : PID, message : Any) : Task = requestAsync(target, message, FutureProcess<T>())
    override fun reenterAfter (target : Task, action : (Task) -> Task) {
        val msg : Any = _message!!
        val cont : Continuation = Continuation({ -> action(target) }, msg)
//        target.continueWith{t ->
//            self.sendSystemMessage(cont)
//        }
    }
    override fun escalateFailure (reason : Exception, who : PID) {
        if (_restartStatistics == null) {
            _restartStatistics = RestartStatistics(0, null)
        }
        val failure : Failure = Failure(who, reason, _restartStatistics!!)
        if (parent == null) {
            handleRootFailure(failure)
        } else {
            self.sendSystemMessage(SuspendMailbox.Instance)
            parent.sendSystemMessage(failure)
        }
    }
    override fun restartChildren (reason : Exception,vararg pids : PID) {
        for(pid in pids) {
            pid.sendSystemMessage(Restart(reason))
        }
    }
    override fun stopChildren (vararg pids : PID) {
        for(pid in pids) {
            pid.sendSystemMessage(Stop.Instance)
        }
    }
    override fun resumeChildren (vararg pids : PID) {
        for(pid in pids) {
            pid.sendSystemMessage(ResumeMailbox.Instance)
        }
    }
    override fun invokeSystemMessageAsync (msg : Any) :Unit  {
        try  {
            when (msg) {
                is Started -> invokeUserMessageAsync(msg)
                is Stop -> handleStopAsync()
                is Terminated -> handleTerminatedAsync(msg)
                is Watch -> handleWatch(msg)
                is Unwatch -> handleUnwatch(msg)
                is Failure -> handleFailure(msg)
                is Restart -> handleRestartAsync()
                is SuspendMailbox -> {}
                is ResumeMailbox -> {}
                is Continuation -> handleContinuation(msg)
            }
        }
        catch (x : Exception) {
           // logger.logError("Error handling SystemMessage {0}", x)
            throw x
        }
    }

    fun handleContinuation(msg : Continuation){
        _message = msg.message
        msg.action()
    }

    override fun invokeUserMessageAsync (msg : Any)  {
        if (receiveTimeout > Duration.ZERO) {
            if (msg !is INotInfluenceReceiveTimeout) {
                stopReceiveTimeout()
            }
        }
        processMessageAsync(msg)
    }
    override fun escalateFailure (reason : Exception, message : Any) {
        escalateFailure(reason, self)
    }
    private fun processMessageAsync (msg : Any) : Task {
        _message = msg
        return if (_receiveMiddleware != null) _receiveMiddleware(this) else defaultReceive(this)
    }
    private fun requestAsync (target : PID, message : Any, future : FutureProcess<T>) : Task {
        var messageEnvelope : MessageEnvelope = MessageEnvelope(message, future.pid, null)
        sendUserMessage(target, messageEnvelope)
        return future.task
    }
    private fun sendUserMessage (target : PID, message : Any) {
        if (_senderMiddleware != null) {
            if (message is MessageEnvelope) {
                _senderMiddleware(this, target, message)
            } else {
                _senderMiddleware(this, target, MessageEnvelope(message, NullPid, NullMessageHeader))
            }
        } else {
            target.tell(message)
        }
    }
    private fun incarnateActor () {
        _state = ContextState.Alive
        actor = _producer()
    }
    private fun handleRestartAsync () {
        _state = ContextState.Restarting
        invokeUserMessageAsync(Restarting.Instance)
        if (_children != null) {
            for(child in _children!!) {
                child.stop()
            }
        }
        tryRestartOrTerminateAsync()
    }
    private fun handleUnwatch (uw : Unwatch) {
        _watchers?.remove(uw.watcher)
    }
    private fun handleWatch (w : Watch) {
        if (_state == ContextState.Stopping) {
            w.watcher.sendSystemMessage(Terminated) //TODO: init
        } else {
            if (_watchers == null) {
                _watchers = mutableSetOf()
            }
            _watchers!!.add(w.watcher)
        }
    }
    private fun handleFailure (msg : Failure) {
        val a = actor
        if (a is ISupervisorStrategy) {
            a.handleFailure(this, msg.who, msg.restartStatistics, msg.reason)
            return 
        }
        _supervisorStrategy.handleFailure(this, msg.who, msg.restartStatistics, msg.reason)
    }
    private fun handleTerminatedAsync (msg : Terminated)  {
        _children?.remove(msg.who)
        invokeUserMessageAsync(msg)
        tryRestartOrTerminateAsync()
    }
    private fun handleRootFailure (failure : Failure) {
        Supervision.defaultStrategy.handleFailure(this, failure.who, failure.restartStatistics, failure.reason)
    }
    private fun handleStopAsync () {
        _state = ContextState.Stopping
        invokeUserMessageAsync(Stopping.Instance)
        val c = _children
        if (c != null) {
            for(child in c) child.stop()
        }
        tryRestartOrTerminateAsync()
    }
    private fun tryRestartOrTerminateAsync () {
        cancelReceiveTimeout()
        val c= _children
        if (c != null && c.count() > 0) {
            return
        }
        when (_state) {
            ContextState.Restarting ->
                restartAsync()
            ContextState.Stopping ->
                stopAsync()
            else -> {
            }
        }
    }
    private fun stopAsync () {
        ProcessRegistry.instance.remove(self)
        invokeUserMessageAsync(Stopped.Instance)
        val w = _watchers
        if (w != null) {
            val terminated : Terminated = Terminated() //TODO: init message
            for(watcher in w) watcher.sendSystemMessage(terminated)
        }

        if (parent != null) {
            val terminated : Terminated = Terminated() //TODO: init message
            parent.sendSystemMessage(terminated)
        }
    }
    private fun restartAsync () {
        incarnateActor()
        self.sendSystemMessage(ResumeMailbox.Instance)
        invokeUserMessageAsync(Started.Instance)
        val s = _stash
        if (s != null) {
            while (s.any()) {
                val msg : Any = s.pop()
                invokeUserMessageAsync(msg)
            }
        }
    }

    private fun resetReceiveTimeout () {
      //  _receiveTimeoutTimer?.change(receiveTimeout, receiveTimeout)
    }
    private fun stopReceiveTimeout () {
      //  _receiveTimeoutTimer?.change(1, 1)
    }
    private fun receiveTimeoutCallback (_ : Any) {
        //self.request(ReceiveTimeout.Instance, null)
    }

    init {
        incarnateActor()
    }
}

