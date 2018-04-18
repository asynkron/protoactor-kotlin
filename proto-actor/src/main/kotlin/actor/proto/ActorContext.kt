package actor.proto

import actor.proto.mailbox.MessageInvoker
import actor.proto.mailbox.ResumeMailbox
import actor.proto.mailbox.SuspendMailbox
import actor.proto.mailbox.SystemMessage
import kotlinx.coroutines.experimental.runBlocking
import mu.KotlinLogging
import java.time.Duration
import java.util.*
private val logger = KotlinLogging.logger {}
class ActorContext(private val producer: () -> Actor, override val self: PID, private val supervisorStrategy: SupervisorStrategy, receiveMiddleware: List<ReceiveMiddleware>, senderMiddleware: List<SenderMiddleware>, override val parent: PID?) : MessageInvoker, Context, SenderContext, Supervisor {
    override var children: Set<PID> = setOf()
    private var watchers: Set<PID> = setOf()
    private var _receiveTimeoutTimer: AsyncTimer? = null
    private val stash: Stack<Any> by lazy(LazyThreadSafetyMode.NONE) { Stack<Any>() }
    private val restartStatistics: RestartStatistics by lazy(LazyThreadSafetyMode.NONE) { RestartStatistics(0, 0) }
    private var state: ContextState = ContextState.None
    override lateinit var actor: Actor
    private var _message: Any = NullMessage
    private val receiveMiddleware: Receive? = when {
        receiveMiddleware.isEmpty() -> null
        else -> receiveMiddleware
                .reversed()
                .fold({ ctx -> ctx.actor.autoReceive(ctx) },
                        { inner, outer -> outer(inner!!) })
    }
    private val senderMiddleware: Send? = when {
        senderMiddleware.isEmpty() -> null
        else -> senderMiddleware
                .reversed()
                .fold({ ctx, targetPid, envelope -> ContextHelper.defaultSender(ctx, targetPid, envelope) },
                        { inner, outer -> outer(inner!!) })
    }

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

    override fun respond(message: Any) {
        sendUserMessage(sender!!, message)
    }

    override fun spawnChild(props: Props): PID = spawnNamedChild(props, ProcessRegistry.nextId())

    override fun spawnPrefixChild(props: Props, prefix: String): PID = spawnNamedChild(props, prefix + ProcessRegistry.nextId())

    override fun spawnNamedChild(props: Props, name: String): PID {
        val pid = props.spawn("${self.id}/$name", self)
        children += pid
        return pid
    }

    override fun watch(pid: PID) = sendSystemMessage(pid,Watch(self))
    override fun unwatch(pid: PID) = sendSystemMessage(pid,Unwatch(self))
    private var receiveTimeout: Duration = Duration.ZERO
    override fun getReceiveTimeout(): Duration = receiveTimeout

    override fun setReceiveTimeout(duration: Duration) {
        when {
            duration <= Duration.ZERO -> throw IllegalArgumentException("duration")
            duration == receiveTimeout -> return
            else -> {
                receiveTimeout = duration
                cancelReceiveTimeout()
                _receiveTimeoutTimer = AsyncTimer({ DefaultActorClient.send(self,ReceiveTimeout) }, duration).apply { start() }
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

    override fun send(target: PID, message: Any) = sendUserMessage(target, message)

    override fun request(target: PID, message: Any) = sendUserMessage(target, MessageEnvelope(message, self, null))

    suspend override fun <T> requestAwait(target: PID, message: Any, timeout: Duration): T = requestAwait(target, message, DeferredProcess(timeout))

    suspend override fun <T> requestAwait(target: PID, message: Any): T = requestAwait(target, message, DeferredProcess())

    //    override fun reenterAfter (target : Task, action : (Task) -> Task) {
//        val msg : Any = _message!!
//        val cont : Continuation = Continuation({ action(target) }, msg)
//        target.continueWith{t ->
//            self.sendSystemMessage(cont)
//        }
//    }
    override fun escalateFailure(reason: Exception, who: PID) {
        val failure = Failure(who, reason, restartStatistics)
        when (parent) {
            null -> handleRootFailure(failure)
            else -> {
                sendSystemMessage(self,SuspendMailbox)
                sendSystemMessage(parent,failure)
            }
        }
    }


    override fun restartChildren(reason: Exception, vararg pids: PID) = pids.forEach { sendSystemMessage(it,Restart(reason)) }
    override fun stopChildren(vararg pids: PID) = pids.forEach { sendSystemMessage(it,StopInstance) }
    override fun resumeChildren(vararg pids: PID) = pids.forEach { sendSystemMessage(it,ResumeMailbox) }

    suspend override fun invokeSystemMessage(msg: SystemMessage) {
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
        _message = msg
        return if (receiveMiddleware != null) receiveMiddleware.invoke(this)
        else actor.autoReceive(this)
    }

    suspend override fun escalateFailure(reason: Exception, message: Any) = escalateFailure(reason, self)


    suspend private fun <T> requestAwait(target: PID, message: Any, deferredProcess: DeferredProcess<T>): T {
        val messageEnvelope = MessageEnvelope(message, deferredProcess.pid, null)
        sendUserMessage(target, messageEnvelope)
        return deferredProcess.await()
    }

    private fun sendUserMessage(target: PID, message: Any) {
        when (senderMiddleware) {
            null -> {
                val process: Process = target.cachedProcess() ?: ProcessRegistry.get(target)
                process.sendUserMessage(target, message)
            }
            else -> {
                val c = this
                when (message) {
                    is MessageEnvelope -> runBlocking { senderMiddleware.invoke(c, target, message) }
                    else -> runBlocking { senderMiddleware.invoke(c, target, MessageEnvelope(message, null, null)) }
                }
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
        children.forEach { stop(it) }
        tryRestartOrTerminate()
    }

    private fun handleUnwatch(uw: Unwatch) {
        watchers -= uw.watcher
    }

    private fun handleWatch(w: Watch) {
        when (state) {
            ContextState.Stopping -> sendSystemMessage(w.watcher,Terminated(self, false))
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
        logger.warn("Handling root failure for " + failure.who.toShortString())
        Supervision.defaultStrategy.handleFailure(this, failure.who, failure.restartStatistics, failure.reason)
    }

    suspend private fun handleStop() {
        state = ContextState.Stopping
        invokeUserMessage(Stopping)
        children.forEach { stop(it) }
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
        watchers.forEach { sendSystemMessage(it, terminated) }

        if (parent != null) sendSystemMessage(parent, terminated)
    }

    suspend private fun restart() {
        incarnateActor()
        sendSystemMessage(self,ResumeMailbox)
        invokeUserMessage(Started)
        while (stash.isNotEmpty()) invokeUserMessage(stash.pop())
    }

    init {
        incarnateActor()
    }

}

