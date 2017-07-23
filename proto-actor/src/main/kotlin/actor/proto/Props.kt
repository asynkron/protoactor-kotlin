package actor.proto

import actor.proto.mailbox.Dispatcher
import actor.proto.mailbox.Mailbox
import actor.proto.mailbox.newUnboundedMailbox

typealias Receive = suspend (Context) -> Unit
typealias Send = suspend (SenderContext, PID, MessageEnvelope) -> Unit

typealias ReceiveMiddleware = (Receive) -> Receive
typealias SenderMiddleware = (Send) -> Send
data class Props(
        private val spawner: (name: String, props: Props, parent: PID?) -> PID = ::defaultSpawner,
        val producer: (() -> Actor)? = null,
        val mailboxProducer: () -> Mailbox = { newUnboundedMailbox() },
        val supervisorStrategy: SupervisorStrategy = Supervision.defaultStrategy,
        val dispatcher: Dispatcher = actor.proto.mailbox.Dispatchers.DEFAULT_DISPATCHER,
        val receiveMiddleware: List<ReceiveMiddleware> = listOf(),
        val senderMiddleware: List<SenderMiddleware> = listOf()
) {
    fun withProducer(producer: () -> Actor): Props = copy(producer = producer)
    fun withDispatcher(dispatcher: Dispatcher): Props = copy(dispatcher = dispatcher)
    fun withMailbox(mailboxProducer: () -> Mailbox): Props = copy(mailboxProducer = mailboxProducer)
    fun withChildSupervisorStrategy(supervisorStrategy: SupervisorStrategy): Props = copy(supervisorStrategy = supervisorStrategy)
    fun withReceiveMiddleware (vararg middleware : ReceiveMiddleware) : Props = copy(receiveMiddleware = middleware.toList())
    fun withSenderMiddleware (vararg middleware : SenderMiddleware) : Props = copy (senderMiddleware = middleware.toList())
    fun withSpawner(spawner: (String, Props, PID?) -> PID): Props = copy(spawner = spawner)
    internal fun spawn(name: String, parent: PID?): PID = spawner(name, this, parent)
}

fun defaultSpawner(name: String, props: Props, parent: PID?): PID {
    val ctx = ActorContext(props.producer!!, props.supervisorStrategy, props.receiveMiddleware, props.senderMiddleware, parent)
    val mailbox = props.mailboxProducer()
    val dispatcher = props.dispatcher
    val process = LocalProcess(mailbox)
    val pid = ProcessRegistry.add(name, process)
    ctx.self = pid
    mailbox.registerHandlers(ctx, dispatcher)
    mailbox.postSystemMessage(Started)
    mailbox.start()
    return pid
}


