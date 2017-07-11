package proto.actor

import proto.mailbox.IMessageInvoker
import proto.mailbox.ResumeMailbox
import proto.mailbox.SuspendMailbox
import proto.mailbox.SystemMessage
import java.time.Duration
import java.util.*

class Context(private val producer: () -> IActor, private val supervisorStrategy: ISupervisorStrategy, private val receiveMiddleware: ((IContext) -> Unit)?, private val senderMiddleware: ((ISenderContext, PID, MessageEnvelope) -> Unit)?, override val parent: PID?) : IMessageInvoker, IContext, ISenderContext, ISupervisor {
    val EmptyChildren: Collection<PID> = listOf()
    private var _children: MutableSet<PID>? = null
    private var _receiveTimeoutTimer: Timer? = null
    private var restartStatistics: RestartStatistics? = null
    private var stash: Stack<Any>? = null
    private var state: ContextState = ContextState.None

    private var watchers: MutableSet<PID>? = null
    override lateinit var actor: IActor
    override lateinit var self: PID
    var _message : Any = Any()
    override val children: Collection<PID>
        get() = _children.orEmpty()

    override val message: Any
        get() {
            val (m, _, _) = MessageEnvelope.unwrap(_message)
            return m
        }

    override val sender: PID?
        get() {
            val (_, sender, _) = MessageEnvelope.unwrap(_message)
            return sender
        }
    override val headers: MessageHeader? = null

    override fun stash() {
        ensureStash().push(message)
    }

    private fun ensureStash() : Stack<Any>{
        stash = stash ?: Stack<Any>()
        return stash!!
    }

    override fun respond(message: Any) {
        sender!!.tell(message)
    }

    override fun spawn(props: Props): PID = spawnNamed(props, ProcessRegistry.nextId())

    override fun spawnPrefix(props: Props, prefix: String): PID = spawnNamed(props, prefix + ProcessRegistry.nextId())

    override fun spawnNamed(props: Props, name: String): PID {
        val pid: PID = props.spawn("${self.id}/$name", self)
        ensureChildren().add(pid)
        return pid
    }

    private fun ensureChildren() : MutableSet<PID> {
        _children = _children ?: mutableSetOf()
        return _children!!
    }

    override fun watch(pid: PID) = pid.sendSystemMessage(Watch(self))
    override fun unwatch(pid: PID) = pid.sendSystemMessage(Unwatch(self))
    private var receiveTimeout: Duration = Duration.ZERO
    override fun getReceiveTimeout(): Duration = receiveTimeout

    override fun setReceiveTimeout(duration: Duration) {
        when {
            duration <= Duration.ZERO -> throw IllegalArgumentException("duration")
            duration == receiveTimeout -> return
            else -> {
                stopReceiveTimeout()
                receiveTimeout = duration
                when (_receiveTimeoutTimer) {
                    null -> _receiveTimeoutTimer = AsyncTimer({ -> self.tell(ReceiveTimeout.Instance) }, receiveTimeout)
                    else -> resetReceiveTimeout()
                }
            }
        }
    }

    override fun cancelReceiveTimeout() {
        when (_receiveTimeoutTimer) {
            null -> return
            else -> {
                stopReceiveTimeout()
                _receiveTimeoutTimer = null
                receiveTimeout = Duration.ZERO
            }
        }
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
        val failure: Failure = Failure(who, reason, ensureRestartStatistics())
        when (parent) {
            null -> handleRootFailure(failure)
            else -> {
                self.sendSystemMessage(SuspendMailbox.Instance)
                parent.sendSystemMessage(failure)
            }
        }
    }

    private fun ensureRestartStatistics() : RestartStatistics {
        restartStatistics = restartStatistics ?: RestartStatistics(0, 0)
        return restartStatistics!!
    }

    override fun restartChildren(reason: Exception, vararg pids: PID) = pids.forEach { it.sendSystemMessage(Restart(reason)) }
    override fun stopChildren(vararg pids: PID) = pids.forEach { it.sendSystemMessage(Stop.Instance) }
    override fun resumeChildren(vararg pids: PID) = pids.forEach { it.sendSystemMessage(ResumeMailbox.Instance) }

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
        _message = msg.message
        msg.action()
    }

    suspend override fun invokeUserMessageAsync(msg: Any) {
        if (receiveTimeout > Duration.ZERO) {
            when (msg) {
                !is INotInfluenceReceiveTimeout -> stopReceiveTimeout()
            }
        }
        processMessageAsync(msg)
    }

    suspend override fun escalateFailure(reason: Exception, message: Any) = escalateFailure(reason, self)

    suspend private fun processMessageAsync(msg: Any): Unit {
        _message = msg
        return when {
            receiveMiddleware != null -> receiveMiddleware.invoke(this)
            else -> ContextHelper.defaultReceive(this)
        }
    }

    suspend private fun <T> requestAsync(target: PID, message: Any, future: FutureProcess<T>): T {
        val messageEnvelope: MessageEnvelope = MessageEnvelope(message, future.pid, null)
        sendUserMessage(target, messageEnvelope)
        return future.deferred().await()
    }

    private fun sendUserMessage(target: PID, message: Any) {
        when (senderMiddleware) {
            null -> target.tell(message)
            else -> when (message) {
                is MessageEnvelope -> senderMiddleware.invoke(this, target, message)
                else -> senderMiddleware.invoke(this, target, MessageEnvelope(message, null, null))
            }
        }
    }

    private fun incarnateActor() {
        state = ContextState.Alive
        actor = producer()
    }

    suspend private fun handleRestartAsync() {
        state = ContextState.Restarting
        invokeUserMessageAsync(Restarting.Instance)
        _children.orEmpty().forEach { it.stop() }
        tryRestartOrTerminateAsync()
    }

    private fun handleUnwatch(uw: Unwatch) {
        watchers?.remove(uw.watcher)
    }

    private fun handleWatch(w: Watch) {
        when (state) {
            ContextState.Stopping -> w.watcher.sendSystemMessage(Terminated(self, false)) //TODO: init
            else -> ensureWatchers().add(w.watcher)
        }
    }

    private fun ensureWatchers() : MutableSet<PID> {
        watchers = watchers ?: mutableSetOf()
        return watchers!!
    }

    private fun handleFailure(msg: Failure) {
        val a = actor
        when (a) {
            is ISupervisorStrategy -> {
                a.handleFailure(this, msg.who, msg.restartStatistics, msg.reason)
                return
            }
            else -> supervisorStrategy.handleFailure(this, msg.who, msg.restartStatistics, msg.reason)
        }
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
        _children.orEmpty().forEach { it.stop() }
        tryRestartOrTerminateAsync()
    }

    suspend private fun tryRestartOrTerminateAsync() {
        cancelReceiveTimeout()
        when {
            _children.orEmpty().any() -> return
            else -> when (state) {
                ContextState.Restarting -> restartAsync()
                ContextState.Stopping -> stopAsync()
                else -> {
                }
            }
        }
    }

    suspend private fun stopAsync() {
        ProcessRegistry.remove(self)
        invokeUserMessageAsync(Stopped.Instance)

        val terminated: Terminated = Terminated(self, false) //TODO: init message
        watchers.orEmpty().forEach { it.sendSystemMessage(terminated) }
        parent?.sendSystemMessage(terminated)
    }

    suspend private fun restartAsync() {
        incarnateActor()
        self.sendSystemMessage(ResumeMailbox.Instance)
        invokeUserMessageAsync(Started.Instance)
        val s = stash
        while (s != null && s.isNotEmpty()) {
            val msg: Any = s.pop()
            invokeUserMessageAsync(msg)
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

