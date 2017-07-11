package proto.actor

import proto.mailbox.IMessageInvoker
import proto.mailbox.ResumeMailbox
import proto.mailbox.SuspendMailbox
import proto.mailbox.SystemMessage
import java.time.Duration
import java.util.*

internal enum class ContextState {
    None, Alive, Restarting, Stopping
}

class LocalContext(private val producer: () -> IActor, private val supervisorStrategy: ISupervisorStrategy, private val receiveMiddleware: ((IContext) -> Unit)?, private val senderMiddleware: ((ISenderContext, PID, MessageEnvelope) -> Unit)?, override val parent: PID?) : IMessageInvoker, IContext, ISenderContext, ISupervisor {
    companion object {
        suspend internal fun defaultReceive(context: IContext) {
            val c: LocalContext = context as LocalContext
            if (c.message is PoisonPill) {
                c.self.stop()
                return
            }
            c.actor.receiveAsync(context)
        }

        internal fun defaultSender(ctx: ISenderContext, target: PID, envelope: MessageEnvelope) {
            target.ref()?.sendUserMessage(target, envelope)
        }
    }

    val EmptyChildren: Collection<PID> = listOf()
    private var _children: MutableSet<PID>? = null
    private var _receiveTimeoutTimer: Timer? = null
    private var restartStatistics: RestartStatistics? = null
    private var stash: Stack<Any>? = null
    private var state: ContextState = ContextState.None

    private var watchers: MutableSet<PID>? = null
    override lateinit var actor: IActor
    override lateinit var self: PID
    override var message: Any = Any()

    override val children: Collection<PID>
        get() = _children?.toList() ?: EmptyChildren
    override val sender: PID?
        get() {
            val (_, sender, _) = MessageEnvelope.unwrap(message)
            return sender
        }
    override val headers: MessageHeader? = null

    override fun stash() {
        if (stash == null) {
            stash = Stack<Any>()
        }
        stash!!.push(message)
    }

    override fun respond(message: Any) {
        sender!!.tell(message)
    }

    override fun spawn(props: Props): PID {
        val id: String = ProcessRegistry.nextId()
        return spawnNamed(props, id)
    }

    override fun spawnPrefix(props: Props, prefix: String): PID {
        val name: String = prefix + ProcessRegistry.nextId()
        return spawnNamed(props, name)
    }

    override fun spawnNamed(props: Props, name: String): PID {
        val pid: PID = props.spawn("${self.id}/$name", self)
        if (_children == null) {
            _children = mutableSetOf()
        }
        _children!!.add(pid)
        return pid
    }

    override fun watch(pid: PID) {
        pid.sendSystemMessage(Watch(self))
    }

    override fun unwatch(pid: PID) {
        pid.sendSystemMessage(Unwatch(self))
    }
    private var receiveTimeout: Duration = Duration.ZERO
    override fun getReceiveTimeout(): Duration = receiveTimeout

    override fun setReceiveTimeout(duration: Duration) {
        if (duration <= Duration.ZERO) {
            throw IllegalArgumentException("duration")
        }
        if (duration == receiveTimeout) {
            return
        }
        stopReceiveTimeout()
        receiveTimeout = duration
        if (_receiveTimeoutTimer == null) {
            _receiveTimeoutTimer = AsyncTimer({ -> self.tell(ReceiveTimeout.Instance) }, receiveTimeout)
        } else {
            resetReceiveTimeout()
        }
    }

    override fun cancelReceiveTimeout() {
        if (_receiveTimeoutTimer == null) {
            return
        }
        stopReceiveTimeout()
        _receiveTimeoutTimer = null
        receiveTimeout = Duration.ZERO
    }

    suspend override fun receiveAsync(message: Any): Unit {
        return processMessageAsync(message)
    }

    override fun tell(target: PID, message: Any) {
        sendUserMessage(target, message)
    }

    override fun request(target: PID, message: Any) {
        sendUserMessage(target, MessageEnvelope(message, self, null))
    }

    suspend override fun <T> requestAsync(target: PID, message: Any, timeout: Duration): T = requestAsync(target, message, FutureProcess<T>(timeout))

    suspend override fun <T> requestAsync(target: PID, message: Any): T = requestAsync(target, message, FutureProcess<T>())

    //    override fun reenterAfter (target : Task, action : (Task) -> Task) {
//        val msg : Any = _message!!
//        val cont : Continuation = Continuation({ -> action(target) }, msg)
//        target.continueWith{t ->
//            self.sendSystemMessage(cont)
//        }
//    }
    override fun escalateFailure(reason: Exception, who: PID) {
        if (restartStatistics == null) {
            restartStatistics = RestartStatistics(0, null)
        }
        val failure: Failure = Failure(who, reason, restartStatistics!!)
        if (parent == null) {
            handleRootFailure(failure)
        } else {
            self.sendSystemMessage(SuspendMailbox.Instance)
            parent.sendSystemMessage(failure)
        }
    }

    override fun restartChildren(reason: Exception, vararg pids: PID) {
        for (pid in pids) {
            pid.sendSystemMessage(Restart(reason))
        }
    }

    override fun stopChildren(vararg pids: PID) {
        for (pid in pids) {
            pid.sendSystemMessage(Stop.Instance)
        }
    }

    override fun resumeChildren(vararg pids: PID) {
        for (pid in pids) {
            pid.sendSystemMessage(ResumeMailbox.Instance)
        }
    }

    suspend override fun invokeSystemMessageAsync(msg: SystemMessage): Unit {
        try {
            when (msg) {
                is Started -> invokeUserMessageAsync(msg)
                is Stop -> handleStopAsync()
                is Terminated -> handleTerminatedAsync(msg)
                is Watch -> handleWatch(msg)
                is Unwatch -> handleUnwatch(msg)
                is Failure -> handleFailure(msg)
                is Restart -> handleRestartAsync()
                is SuspendMailbox -> {
                }
                is ResumeMailbox -> {
                }
                is Continuation -> handleContinuation(msg)
                else -> throw Exception("Unknown system message")
            }
        } catch (x: Exception) {
            // logger.logError("Error handling SystemMessage {0}", x)
            throw x
        }
    }

    suspend fun handleContinuation(msg: Continuation) {
        message = msg.message
        msg.action()
    }

    suspend override fun invokeUserMessageAsync(msg: Any) {
        if (receiveTimeout > Duration.ZERO) {
            if (msg !is INotInfluenceReceiveTimeout) {
                stopReceiveTimeout()
            }
        }
        processMessageAsync(msg)
    }

    suspend override fun escalateFailure(reason: Exception, message: Any) = escalateFailure(reason, self)

    suspend private fun processMessageAsync(msg: Any): Unit {
        message = msg
        return when {
            receiveMiddleware != null -> receiveMiddleware.invoke(this)
            else -> defaultReceive(this)
        }
    }

    suspend private fun <T> requestAsync(target: PID, message: Any, future: FutureProcess<T>): T {
        val messageEnvelope: MessageEnvelope = MessageEnvelope(message, future.pid, null)
        sendUserMessage(target, messageEnvelope)
        return future.deferred().await()
    }

    private fun sendUserMessage(target: PID, message: Any) {
        if (senderMiddleware != null) {
            if (message is MessageEnvelope) {
                senderMiddleware.invoke(this, target, message)
            } else {
                senderMiddleware.invoke(this, target, MessageEnvelope(message, null, null))
            }
        } else {
            target.tell(message)
        }
    }

    private fun incarnateActor() {
        state = ContextState.Alive
        actor = producer()
    }

    suspend private fun handleRestartAsync() {
        state = ContextState.Restarting
        invokeUserMessageAsync(Restarting.Instance)
        if (_children != null) {
            for (child in _children!!) {
                child.stop()
            }
        }
        tryRestartOrTerminateAsync()
    }

    private fun handleUnwatch(uw: Unwatch) {
        watchers?.remove(uw.watcher)
    }

    private fun handleWatch(w: Watch) {
        if (state == ContextState.Stopping) {
            w.watcher.sendSystemMessage(Terminated(self, false)) //TODO: init
        } else {
            if (watchers == null) {
                watchers = mutableSetOf()
            }
            watchers!!.add(w.watcher)
        }
    }

    private fun handleFailure(msg: Failure) {
        val a = actor
        if (a is ISupervisorStrategy) {
            a.handleFailure(this, msg.who, msg.restartStatistics, msg.reason)
            return
        }
        supervisorStrategy.handleFailure(this, msg.who, msg.restartStatistics, msg.reason)
    }

    suspend private fun handleTerminatedAsync(msg: Terminated) {
        _children?.remove(msg.who)
        invokeUserMessageAsync(msg)
        tryRestartOrTerminateAsync()
    }

    private fun handleRootFailure(failure: Failure) {
        Supervision.defaultStrategy.handleFailure(this, failure.who, failure.restartStatistics, failure.reason)
    }

    suspend private fun handleStopAsync() {
        state = ContextState.Stopping
        invokeUserMessageAsync(Stopping.Instance)
        val c = _children
        if (c != null) {
            for (child in c) child.stop()
        }
        tryRestartOrTerminateAsync()
    }

    suspend private fun tryRestartOrTerminateAsync() {
        cancelReceiveTimeout()
        val c = _children
        if (c != null && c.count() > 0) {
            return
        }
        when (state) {
            ContextState.Restarting ->
                restartAsync()
            ContextState.Stopping ->
                stopAsync()
            else -> {
            }
        }
    }

    suspend private fun stopAsync() {
        ProcessRegistry.remove(self)
        invokeUserMessageAsync(Stopped.Instance)
        val w = watchers
        val terminated: Terminated = Terminated(self, false) //TODO: init message
        if (w != null) {
            for (watcher in w) watcher.sendSystemMessage(terminated)
        }

        parent?.sendSystemMessage(terminated)
    }

    suspend private fun restartAsync() {
        incarnateActor()
        self.sendSystemMessage(ResumeMailbox.Instance)
        invokeUserMessageAsync(Started.Instance)
        val s = stash
        if (s != null) {
            while (s.any()) {
                val msg: Any = s.pop()
                invokeUserMessageAsync(msg)
            }
        }
    }

    private fun resetReceiveTimeout() {
        //  _receiveTimeoutTimer?.change(receiveTimeout, receiveTimeout)
    }

    private fun stopReceiveTimeout() {
        //  _receiveTimeoutTimer?.change(1, 1)
    }

    init {
        incarnateActor()
    }
}

class AsyncTimer(receiveTimeoutCallback: () -> Unit, tick: Duration) : Timer() {

}

