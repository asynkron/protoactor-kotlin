package actor.proto

import actor.proto.mailbox.MessageInvoker
import actor.proto.mailbox.ResumeMailbox
import actor.proto.mailbox.SuspendMailbox
import actor.proto.mailbox.SystemMessage
import java.time.Duration
import java.util.*

class ActorContext(private val producer: () -> Actor, private val supervisorStrategy: SupervisorStrategy, private val receiveMiddleware: ((Context) -> Unit)?, private val senderMiddleware: ((SenderContext, PID, MessageEnvelope) -> Unit)?, override val parent: PID?) : MessageInvoker, Context, SenderContext, Supervisor {
    override var children: Set<PID> = setOf()
    private var watchers: Set<PID> = setOf()
    private var _receiveTimeoutTimer: AsyncTimer? = null
    private val stash: Stack<Any> by lazy(LazyThreadSafetyMode.NONE) { Stack<Any>() }
    private val restartStatistics: RestartStatistics by lazy(LazyThreadSafetyMode.NONE) { RestartStatistics(0, 0) }
    private var state: ContextState = ContextState.None
    override lateinit var actor: Actor
    override lateinit var self: PID
    private var _message: Any = NullMessage

    override val message: Any
        get() = _message.let {
            when (it) {
                is MessageEnvelope -> it.message
                else -> it
            }
        }

    override val sender: PID?
        get() = _message.let {
            when (it) {
                is MessageEnvelope -> it.sender
                else -> null
            }
        }

    override val headers: MessageHeader?
        get() = _message.let {
            when (it) {
                is MessageEnvelope -> it.header
                else -> null
            }
        }

    override fun stash() {
        stash.push(message)
    }

    override fun respond(message: Any) = sender!!.send(message)

    override fun spawnChild(props: Props): PID = spawnNamedChild(props, ProcessRegistry.nextId())

    override fun spawnPrefixChild(props: Props, prefix: String): PID = spawnNamedChild(props, prefix + ProcessRegistry.nextId())

    override fun spawnNamedChild(props: Props, name: String): PID {
        val pid = props.spawn("${self.id}/$name", self)
        children += pid
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
                _receiveTimeoutTimer = AsyncTimer({ self.send(ReceiveTimeout) }, duration).apply { start() }
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

    suspend override fun receive(message: Any): Unit = processMessage(message)

    override fun send(target: PID, message: Any) = sendUserMessage(target, message)

    override fun request(target: PID, message: Any) = sendUserMessage(target, MessageEnvelope(message, self, null))

    suspend override fun <T> requestAwait(target: PID, message: Any, timeout: Duration): T = requestAwait(target, message, FutureProcess(timeout))

    suspend override fun <T> requestAwait(target: PID, message: Any): T = requestAwait(target, message, FutureProcess())

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
    override fun stopChildren(vararg pids: PID) = pids.forEach { it.sendSystemMessage(StopInstance) }
    override fun resumeChildren(vararg pids: PID) = pids.forEach { it.sendSystemMessage(ResumeMailbox) }

    suspend override fun invokeSystemMessage(msg: SystemMessage): Unit {
        try {
            when (msg) {
                is Started -> invokeUserMessage(msg)
                is Stop -> handleStop()
                is Terminated -> handleTerminated(msg)
                is Watch -> handleWatch(msg)
                is Unwatch -> handleUnwatch(msg)
                is Failure -> handleFailure(msg)
                is Restart -> handleRestart()
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

    suspend override fun invokeUserMessage(msg: Any) {
        if (receiveTimeout > Duration.ZERO && msg !is NotInfluenceReceiveTimeout) {
            _receiveTimeoutTimer?.reset()
        }
        processMessage(msg)
    }

    suspend override fun escalateFailure(reason: Exception, message: Any) = escalateFailure(reason, self)

    suspend private fun processMessage(msg: Any): Unit {
        _message = msg
        if (receiveMiddleware != null) receiveMiddleware.invoke(this)
        else ContextHelper.defaultReceive(this)
    }

    suspend private fun <T> requestAwait(target: PID, message: Any, future: FutureProcess<T>): T {
        val messageEnvelope: MessageEnvelope = MessageEnvelope(message, future.pid, null)
        sendUserMessage(target, messageEnvelope)
        return future.future().get()
    }

    private fun sendUserMessage(target: PID, message: Any) {
        when (senderMiddleware) {
            null -> target.send(message)
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

    suspend private fun handleRestart() {
        state = ContextState.Restarting
        invokeUserMessage(Restarting)
        children.forEach { it.stop() }
        tryRestartOrTerminate()
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
            is SupervisorStrategy -> a
            else -> supervisorStrategy
        }.handleFailure(this, msg.who, msg.restartStatistics, msg.reason)
    }

    suspend private fun handleTerminated(msg: Terminated) {
        children -= msg.who
        invokeUserMessage(msg)
        tryRestartOrTerminate()
    }

    private fun handleRootFailure(failure: Failure) {
        println("Handling root failure for " + failure.who.toShortString())
        Supervision.defaultStrategy.handleFailure(this, failure.who, failure.restartStatistics, failure.reason)
    }

    suspend private fun handleStop() {
        state = ContextState.Stopping
        invokeUserMessage(Stopping)
        children.forEach { it.stop() }
        tryRestartOrTerminate()
    }

    suspend private fun tryRestartOrTerminate() {
        cancelReceiveTimeout()
        when {
            children.isNotEmpty() -> return
            else -> when (state) {
                ContextState.Restarting -> restart()
                ContextState.Stopping -> stop()
                else -> {
                }
            }
        }
    }

    suspend private fun stop() {
        ProcessRegistry.remove(self)
        invokeUserMessage(Stopped)
        val terminated = Terminated(self, false)
        watchers.forEach { it.sendSystemMessage(terminated) }
        parent?.sendSystemMessage(terminated)
    }

    suspend private fun restart() {
        incarnateActor()
        self.sendSystemMessage(ResumeMailbox)
        invokeUserMessage(Started)
        while (stash.isNotEmpty()) invokeUserMessage(stash.pop())
    }


    init {
        incarnateActor()
    }
}

