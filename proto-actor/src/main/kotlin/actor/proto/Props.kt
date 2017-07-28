package actor.proto

import actor.proto.mailbox.Dispatcher
import actor.proto.mailbox.Mailbox
import actor.proto.mailbox.newUnboundedMailbox

typealias Receive = suspend (Context) -> Unit
typealias Send = suspend (SenderContext, PID, MessageEnvelope) -> Unit

typealias ReceiveMiddleware = (Receive) -> Receive
typealias SenderMiddleware = (Send) -> Send
data class Props(
        val spawner: (name: String, props: Props, parent: PID?) -> PID = ::defaultSpawner,
        val producer: (() -> Actor)? = null,
        val mailboxProducer: () -> Mailbox = { newUnboundedMailbox() },
        val supervisorStrategy: SupervisorStrategy = Supervision.defaultStrategy,
        val dispatcher: Dispatcher = actor.proto.mailbox.Dispatchers.DEFAULT_DISPATCHER,
        val receiveMiddleware: List<ReceiveMiddleware> = listOf(),
        val senderMiddleware: List<SenderMiddleware> = listOf()
) {
    fun withChildSupervisorStrategy(supervisorStrategy: SupervisorStrategy): Props = copy(supervisorStrategy = supervisorStrategy)
    fun withMailbox(mailboxProducer: () -> Mailbox): Props = copy(mailboxProducer = mailboxProducer)
    fun withDispatcher(dispatcher: Dispatcher): Props = copy(dispatcher = dispatcher)
}

internal fun Props.spawn(name: String, parent: PID?): PID = spawner(name, this, parent)


fun Props.withProducer(producer: () -> Actor): Props = copy(producer = producer)
fun Props.withSpawner(spawner: (String, Props, PID?) -> PID): Props = copy(spawner = spawner)
fun Props.withSenderMiddleware(vararg middleware: SenderMiddleware): Props = copy(senderMiddleware = middleware.toList())
fun Props.withReceiveMiddleware(vararg middleware: ReceiveMiddleware): Props = copy(receiveMiddleware = middleware.toList())

fun defaultSpawner(name: String, props: Props, parent: PID?): PID {
    val mailbox = props.mailboxProducer()
    val dispatcher = props.dispatcher
    val process = LocalProcess(mailbox)
    val self = ProcessRegistry.put(name, process)
    val ctx = ActorContext(props.producer!!, self, props.supervisorStrategy, props.receiveMiddleware, props.senderMiddleware, parent)
    mailbox.registerHandlers(ctx, dispatcher)
    mailbox.postSystemMessage(Started)
    mailbox.start()
    return self
}


