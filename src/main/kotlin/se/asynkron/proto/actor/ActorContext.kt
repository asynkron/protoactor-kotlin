package proto.actor

import proto.mailbox.MessageInvoker
import proto.mailbox.ResumeMailbox
import proto.mailbox.SuspendMailbox
import proto.mailbox.SystemMessage
import java.time.Duration
import java.util.*

object NullMessage

class ActorContext(private val producer: () -> Actor, private val supervisorStrategy: SupervisorStrategy, private val receiveMiddleware: ((Context) -> Unit)?, private val senderMiddleware: ((SenderContext, PID, MessageEnvelope) -> Unit)?, override val parent: PID?) : MessageInvoker, Context, SenderContext, Supervisor {
    private var _children: Set<PID> = setOf()
    private var watchers: Set<PID> = setOf()
    private var _receiveTimeoutTimer: AsyncTimer? = null
    private val stash: Stack<Any> by lazy(LazyThreadSafetyMode.NONE) { Stack<Any>() }
    private val restartStatistics: RestartStatistics by lazy(LazyThreadSafetyMode.NONE) { RestartStatistics(0, 0) }
    private var state: ContextState = ContextState.None
    override lateinit var actor: Actor
    override lateinit var self: PID
    private var _message: Any = NullMessage
    override val children: Set<PID>
        get() = _children

    override val message: Any
        get() {
            val m = _message
            return when (m) {
                is MessageEnvelope -> m.message
                else -> m
            }
        }

    override val sender: PID?
        get() {
            val m = _message
            return when (m) {
                is MessageEnvelope -> m.sender
                else -> null
            }
        }
    override val headers: MessageHeader?
        get() {
            val m = _message
            return when (m) {
                is MessageEnvelope -> m.header
                else -> null
            }
        }

    override fun stash() {
        stash.push(message)
    }

    override fun respond(message: Any) = sender!!.tell(message)

    override fun spawn(props: Props): PID = spawnNamed(props, ProcessRegistry.nextId())

    override fun spawnPrefix(props: Props, prefix: String): PID = spawnNamed(props, prefix + ProcessRegistry.nextId())

    override fun spawnNamed(props: Props, name: String): PID {
        val pid: PID = props.spawn("${self.id}/$name", self)
        _children += pid
        return pid
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
                receiveTimeout = duration
                cancelReceiveTimeout()
                _receiveTimeoutTimer = AsyncTimer({ self.tell(ReceiveTimeout) }, duration).apply { start() }
            }
        }
    }

    override fun cancelReceiveTimeout() {
        when (_receiveTimeoutTimer) {
            null -> return
            else -> {
                _receiveTimeoutTimer!!.stop()
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

    suspend override fun <T> requestAsync(target: PID, message: Any, timeout: Duration): T = requestAsync(target, message, FutureProcess(timeout))

    suspend override fun <T> requestAsync(target: PID, message: Any): T = requestAsync(target, message, FutureProcess())

    //    override fun reenterAfter (target : Task, action : (Task) -> Task) {
//        val msg : Any = _message!!
//        val cont : Continuation = Continuation({ -> action(target) }, msg)
//        target.continueWith{t ->
//            self.sendSystemMessage(cont)
//        }
//    }
    override fun escalateFailure(reason: Exception, who: PID) {
        val failure: Failure = Failure(who, reason, restartStatistics)
        when (parent) {
            null -> handleRootFailure(failure)
            else -> {
                self.sendSystemMessage(SuspendMailbox)
                parent.sendSystemMessage(failure)
            }
        }
    }


    override fun restartChildren(reason: Exception, vararg pids: PID) = pids.forEach { it.sendSystemMessage(Restart(reason)) }
    override fun stopChildren(vararg pids: PID) = pids.forEach { it.sendSystemMessage(Stop) }
    override fun resumeChildren(vararg pids: PID) = pids.forEach { it.sendSystemMessage(ResumeMailbox) }

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

    private suspend fun handleContinuation(msg: Continuation) {
        _message = msg.message
        msg.action()
    }

    suspend override fun invokeUserMessageAsync(msg: Any) {
        if (receiveTimeout > Duration.ZERO) {
            when (msg) {
                !is NotInfluenceReceiveTimeout -> when(_receiveTimeoutTimer){
                    null -> {}
                    else -> _receiveTimeoutTimer!!.reset()
                }
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
        invokeUserMessageAsync(Restarting)
        _children.forEach { it.stop() }
        tryRestartOrTerminateAsync()
    }

    private fun handleUnwatch(uw: Unwatch) {
        watchers -= uw.watcher
    }

    private fun handleWatch(w: Watch) {
        when (state) {
            ContextState.Stopping -> w.watcher.sendSystemMessage(Terminated(self, false))
            else -> watchers += w.watcher
        }
    }

    private fun handleFailure(msg: Failure) {
        val a = actor
        when (a) {
            is SupervisorStrategy -> {
                a.handleFailure(this, msg.who, msg.restartStatistics, msg.reason)
                return
            }
            else -> supervisorStrategy.handleFailure(this, msg.who, msg.restartStatistics, msg.reason)
        }
    }

    suspend private fun handleTerminatedAsync(msg: Terminated) {
        _children -= msg.who
        invokeUserMessageAsync(msg)
        tryRestartOrTerminateAsync()
    }

    private fun handleRootFailure(failure: Failure) {
        Supervision.defaultStrategy.handleFailure(this, failure.who, failure.restartStatistics, failure.reason)
    }

    suspend private fun handleStopAsync() {
        state = ContextState.Stopping
        invokeUserMessageAsync(Stopping)
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
        invokeUserMessageAsync(Stopped)

        val terminated: Terminated = Terminated(self, false) //TODO: init message
        watchers.orEmpty().forEach { it.sendSystemMessage(terminated) }
        parent?.sendSystemMessage(terminated)
    }

    suspend private fun restartAsync() {
        incarnateActor()
        self.sendSystemMessage(ResumeMailbox)
        invokeUserMessageAsync(Started)
        while (stash.isNotEmpty()) {
            val msg: Any = stash.pop()
            invokeUserMessageAsync(msg)
        }
    }


    init {
        incarnateActor()
    }
}

