package proto

import java.time.Duration
import java.util.*

internal enum class ContextState {
    None, Alive, Restarting, Stopping
}

class LocalContext(producer: () -> IActor, supervisorStrategy: ISupervisorStrategy, receiveMiddleware: (IContext) -> Task, senderMiddleware: (ISenderContext, PID, MessageEnvelope) -> Task, override val parent: PID) : IMessageInvoker(), IContext, ISupervisor {
    companion object {
        internal fun defaultReceive (context : IContext) : Task {
            val c : LocalContext = context as LocalContext
            if (c.message is PoisonPill) {
                c.self.stop()
                return Actor.Done
            }
            return c.actor.receiveAsync(context)
        }
        internal fun defaultSender (context : ISenderContext, target : PID, envelope : MessageEnvelope) : Task {
            target.ref.sendUserMessage(target, envelope)
            return Task.fromResult(0)
        }
    }
    val EmptyChildren : Collection<PID> = listOf()
    private val _producer : () -> IActor = producer
    private val _receiveMiddleware : (IContext) -> Task = receiveMiddleware
    private val _senderMiddleware : (ISenderContext, PID, MessageEnvelope) -> Task = senderMiddleware
    private val _supervisorStrategy : ISupervisorStrategy = supervisorStrategy
    private var _children : MutableSet<PID>? = null
    private var _message : Any? = null
    private var _receiveTimeoutTimer : Timer? = null
    private var _restartStatistics : RestartStatistics? = null
    private var _stash : Stack<Any>? = null
    private var _state : ContextState = ContextState.None
    private var _watchers : MutableSet<PID>? = null
    override val children : Collection<PID>
        get() = _children?.toList() ?: EmptyChildren
    override var actor : IActor? = null
    override lateinit var self : PID
    val message : Any = ""
    override val sender : PID
        get() = _message as MessageEnvelope?.sender
    override val headers : MessageHeader
    override var receiveTimeout : Duration
    override fun stash () {
        if (_stash == null) {
            _stash = Stack<Any>()
        }
        _stash.push(message)
    }
    override fun respond (message : Any) {
        sender.tell(message)
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
    override fun requestAsync (target : PID, message : Any, timeout : Duration) : Task<T> = requestAsync(target, message, FutureProcess<T>(timeout))
    override fun requestAsync (target : PID, message : Any) : Task<T> = requestAsync(target, message, FutureProcess<T>())
    override fun reenterAfter (target : Task<T>, action : (Task<T>) -> Task) {
        val msg : Any = _message!!
        val cont : Continuation = Continuation({ -> action(target)}, msg)
        target.continueWith{t -> 
            self.sendSystemMessage(cont)
        }

    }
    override fun escalateFailure (reason : Exception, who : PID) {
        if (_restartStatistics == null) {
            _restartStatistics = RestartStatistics(0, null)
        }
        var failure : Failure = Failure(who, reason, _restartStatistics)
        if (parent == null) {
            handleRootFailure(failure)
        } else {
            self.sendSystemMessage(SuspendMailbox.Instance)
            parent.sendSystemMessage(failure)
        }
    }
    override fun restartChildren (reason : Exception, pids : Array<PID>) {
        for(pid in pids) {
            pid.sendSystemMessage(Restart(reason))
        }
    }
    override fun stopChildren (pids : Array<PID>) {
        for(pid in pids) {
            pid.sendSystemMessage(Stop.Instance)
        }
    }
    override fun resumeChildren (pids : Array<PID>) {
        for(pid in pids) {
            pid.sendSystemMessage(ResumeMailbox.Instance)
        }
    }
    override fun invokeSystemMessageAsync (msg : Any)  {
        try  {
            val tmp = msg
            when (tmp) {
                is Started -> {
                    val s = tmp
                    invokeUserMessageAsync(s)
                    return
                }
                is Stop -> {
                    handleStopAsync()
                    return
                }
                is Terminated -> {
                    val t = tmp
                    handleTerminatedAsync(t)
                    return
                }
                is Watch -> {
                    val w = tmp
                    handleWatch(w)
                    return
                }
                is Unwatch -> {
                    val uw = tmp
                    handleUnwatch(uw)
                    return
                }
                is Failure -> {
                    val f = tmp
                    handleFailure(f)
                    return
                }
                is Restart -> {
                    handleRestartAsync()
                    return
                }
                is SuspendMailbox -> {
                    return
                }
                is ResumeMailbox -> {
                    return
                }
                is Continuation -> {
                    val cont = tmp
                    _message = cont.message
                    cont.action()
                    return
                }
            }
        }
        catch (x : Exception) {
           // logger.logError("Error handling SystemMessage {0}", x)
            throw x
        }
    }
    override fun invokeUserMessageAsync (msg : Any) : Task {
        if (receiveTimeout > Duration.ZERO) {
            if (msg !is INotInfluenceReceiveTimeout) {
                stopReceiveTimeout()
            }
        }
        processMessageAsync(msg)

        return Task()
    }
    override fun escalateFailure (reason : Exception, message : Any) {
        escalateFailure(reason, self)
    }
    private fun processMessageAsync (msg : Any) : Task {
        _message = msg
        return if (_receiveMiddleware != null) _receiveMiddleware(this) else defaultReceive(this)
    }
    private fun requestAsync (target : PID, message : Any, future : FutureProcess<T>) : Task<T> {
        var messageEnvelope : MessageEnvelope = MessageEnvelope(message, future.pid, null)
        sendUserMessage(target, messageEnvelope)
        return future.task
    }
    private fun sendUserMessage (target : PID, message : Any) {
        if (_senderMiddleware != null) {
            if (messageMessageEnvelope) {
                _senderMiddleware(this, target, messageEnvelope)
            } else {
                _senderMiddleware(this, target, MessageEnvelope(message, null, null))
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
            for(child in _children) {
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
            w.watcher.sendSystemMessage(Terminated)
        } else {
            if (_watchers == null) {
                _watchers = mutableSetOf()
            }
            _watchers.add(w.watcher)
        }
    }
    private fun handleFailure (msg : Failure) {
        if (actorISupervisorStrategy) {
            supervisor.handleFailure(this, msg.who, msg.restartStatistics, msg.reason)
            return 
        }
        _supervisorStrategy.handleFailure(this, msg.who, msg.restartStatistics, msg.reason)
    }
    private fun handleTerminatedAsync (msg : Terminated) : Task {
        _children?.remove(msg.who)
        invokeUserMessageAsync(msg)
        tryRestartOrTerminateAsync()
    }
    private fun handleRootFailure (failure : Failure) {
        Supervision.defaultStrategy.handleFailure(this, failure.who, failure.restartStatistics, failure.reason)
    }
    private fun handleStopAsync () : Task {
        _state = ContextState.Stopping
        invokeUserMessageAsync(Stopping.Instance)
        if (_children != null) {
            for(child in _children) {
                child.stop()
            }
        }
        tryRestartOrTerminateAsync()
    }
    private fun tryRestartOrTerminateAsync () : Task {
        cancelReceiveTimeout()
        if (_children?.count > 0) {
            return 
        }
        val tmp = _state
        when (tmp) {
        }
    }
    private fun stopAsync () : Task {
        ProcessRegistry.instance.remove(self)
        invokeUserMessageAsync(Stopped.Instance)
        disposeActorIfDisposable()
        if (_watchers != null) {
            var terminated : Terminated = Terminated
            for(watcher in _watchers) {
                watcher.sendSystemMessage(terminated)
            }
        }
        if (parent != null) {
            var terminated : Terminated = Terminated
            parent.sendSystemMessage(terminated)
        }
    }
    private fun restartAsync () : Task {
        disposeActorIfDisposable()
        incarnateActor()
        self.sendSystemMessage(ResumeMailbox.Instance)
        invokeUserMessageAsync(Started.Instance)
        if (_stash != null) {
            while (_stash!!.any()) {
                val msg : Any = _stash!!.pop()
                invokeUserMessageAsync(msg)
            }
        }
    }
    private fun disposeActorIfDisposable () {
        if (actorIDisposable) {
            disposableActor.dispose()
        }
    }
    private fun resetReceiveTimeout () {
        _receiveTimeoutTimer?.change(receiveTimeout, receiveTimeout)
    }
    private fun stopReceiveTimeout () {
        _receiveTimeoutTimer?.change(1, 1)
    }
    private fun receiveTimeoutCallback (state : Any) {
        self.request(Proto.ReceiveTimeout.Instance, null)
    }

    init {
        incarnateActor()
    }
}

