package proto.actor

import proto.mailbox.IMessageInvoker
import proto.mailbox.ResumeMailbox
import proto.mailbox.SuspendMailbox
import java.time.Duration
import java.util.*

internal enum class ContextState {
    None, Alive, Restarting, Stopping
}

class LocalContext(producer: () -> IActor, supervisorStrategy: ISupervisorStrategy, receiveMiddleware: ((IContext) -> Unit)?, senderMiddleware: ((ISenderContext, PID, MessageEnvelope) -> Unit)?, override val parent: PID?) : IMessageInvoker, IContext,ISenderContext, ISupervisor {
    companion object {
        suspend internal fun defaultReceive (context : IContext)  {
            val c : LocalContext = context as LocalContext
            if (c.message is PoisonPill) {
                c.self.stop()
                return
            }
            c.actor.receiveAsync(context)
        }
        internal fun defaultSender (ctx : ISenderContext, target : PID, envelope : MessageEnvelope) {
            target.ref()?.sendUserMessage(target, envelope)
        }
    }
    val EmptyChildren : Collection<PID> = listOf()
    private val _producer : () -> IActor = producer
    private val _receiveMiddleware : ((IContext) -> Unit)? = receiveMiddleware
    private val _senderMiddleware : ((ISenderContext, PID, MessageEnvelope) -> Unit)? = senderMiddleware
    private val _supervisorStrategy : ISupervisorStrategy = supervisorStrategy
    private var _children : MutableSet<PID>? = null
    private var _message : Any = Any()
    private var _receiveTimeoutTimer : Timer? = null
    private var _restartStatistics : RestartStatistics? = null
    private var _stash : Stack<Any>? = null
    private var _state : ContextState = ContextState.None
    private var _receiveTimeout : Duration = Duration.ZERO
    private var _watchers : MutableSet<PID>? = null
    override val children : Collection<PID>
        get() = _children?.toList() ?: EmptyChildren
    override lateinit var actor : IActor
    override lateinit var self : PID
    override val message : Any = Any()
    override val sender : PID?
        get() {
            val (_,sender,_) = MessageEnvelope.unwrap(_message)
            return sender
        }
    override val headers : MessageHeader? = null

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
        val id : String = ProcessRegistry.nextId()
        return spawnNamed(props, id)
    }
    override fun spawnPrefix (props : Props, prefix : String) : PID {
        val name : String = prefix + ProcessRegistry.nextId()
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

    override fun getReceiveTimeout() : Duration = _receiveTimeout

    override fun setReceiveTimeout (duration : Duration) {
        if (duration <= Duration.ZERO) {
            throw IllegalArgumentException("duration")
        }
        if (duration == _receiveTimeout) {
            return 
        }
        stopReceiveTimeout()
        _receiveTimeout = duration
        if (_receiveTimeoutTimer == null) {
            _receiveTimeoutTimer = AsyncTimer({-> self.tell(ReceiveTimeout.Instance) }, _receiveTimeout)
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
        _receiveTimeout = Duration.ZERO
    }
    suspend override fun receiveAsync (message : Any) : Unit {
        return processMessageAsync(message)
    }
    override fun tell (target : PID, message : Any) {
        sendUserMessage(target, message)
    }
    override fun request (target : PID, message : Any) {
        val messageEnvelope : MessageEnvelope = MessageEnvelope(message, self, null)
        sendUserMessage(target, messageEnvelope)
    }
    suspend override fun <T> requestAsync (target : PID, message : Any, timeout : Duration) : T {
        return requestAsync<T>(target, message, FutureProcess<T>(timeout))
    }
    suspend override fun <T> requestAsync (target : PID, message : Any) : T {
        return requestAsync<T>(target, message, FutureProcess<T>())
    }
//    override fun reenterAfter (target : Task, action : (Task) -> Task) {
//        val msg : Any = _message!!
//        val cont : Continuation = Continuation({ -> action(target) }, msg)
//        target.continueWith{t ->
//            self.sendSystemMessage(cont)
//        }
//    }
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
    suspend override fun invokeSystemMessageAsync (msg : Any) :Unit  {
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

    suspend fun handleContinuation(msg : Continuation){
        _message = msg.message
        msg.action()
    }

    suspend override fun invokeUserMessageAsync (msg : Any)  {
        if (_receiveTimeout > Duration.ZERO) {
            if (msg !is INotInfluenceReceiveTimeout) {
                stopReceiveTimeout()
            }
        }
        processMessageAsync(msg)
    }
    suspend override fun escalateFailure (reason : Exception, message : Any) {
        escalateFailure(reason, self)
    }
    suspend private fun processMessageAsync (msg : Any) : Unit {
        _message = msg
        return if (_receiveMiddleware != null) _receiveMiddleware.invoke(this) else defaultReceive(this)
    }
    suspend private  fun <T> requestAsync (target : PID, message : Any, future : FutureProcess<T>) : T {
        val messageEnvelope : MessageEnvelope = MessageEnvelope(message, future.pid, null)
        sendUserMessage(target, messageEnvelope)
        return future.deferred().await()
    }
    private fun sendUserMessage (target : PID, message : Any) {
        if (_senderMiddleware != null) {
            if (message is MessageEnvelope) {
                _senderMiddleware.invoke(this, target, message)
            } else {
                _senderMiddleware.invoke(this, target, MessageEnvelope(message, null, null))
            }
        } else {
            target.tell(message)
        }
    }
    private fun incarnateActor () {
        _state = ContextState.Alive
        actor = _producer()
    }
    suspend private fun handleRestartAsync () {
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
            w.watcher.sendSystemMessage(Terminated(self,false)) //TODO: init
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
    suspend private fun handleTerminatedAsync (msg : Terminated)  {
        _children?.remove(msg.who)
        invokeUserMessageAsync(msg)
        tryRestartOrTerminateAsync()
    }
    private fun handleRootFailure (failure : Failure) {
        Supervision.defaultStrategy.handleFailure(this, failure.who, failure.restartStatistics, failure.reason)
    }
    suspend private fun handleStopAsync () {
        _state = ContextState.Stopping
        invokeUserMessageAsync(Stopping.Instance)
        val c = _children
        if (c != null) {
            for(child in c) child.stop()
        }
        tryRestartOrTerminateAsync()
    }
    suspend private fun tryRestartOrTerminateAsync () {
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
    suspend private fun stopAsync () {
        ProcessRegistry.remove(self)
        invokeUserMessageAsync(Stopped.Instance)
        val w = _watchers
        val terminated : Terminated = Terminated(self,false) //TODO: init message
        if (w != null) {
            for(watcher in w) watcher.sendSystemMessage(terminated)
        }

        parent?.sendSystemMessage(terminated)
    }
    suspend private fun restartAsync () {
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
    init {
        incarnateActor()
    }
}

class AsyncTimer(receiveTimeoutCallback: () -> Unit, _receiveTimeout: Duration) : Timer() {

}

