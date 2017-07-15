package actor.proto

import actor.proto.mailbox.Dispatcher
import actor.proto.mailbox.unboundedMailbox
import actor.proto.mailbox.Mailbox

data class Props(
        private val spawner: (name: String, props: Props, parent: Protos.PID?) -> Protos.PID = ::defaultSpawner,
        val producer: (() -> Actor)? = null,
        val mailboxProducer: () -> Mailbox = { unboundedMailbox() },
        val supervisorStrategy: SupervisorStrategy = Supervision.defaultStrategy,
        val dispatcher: Dispatcher = actor.proto.mailbox.Dispatchers.DEFAULT_DISPATCHER,
        private val receiveMiddleware: List<((Context) -> Unit) -> (Context) -> Unit> = listOf(),
        private val senderMiddleware: List<((SenderContext, Protos.PID, MessageEnvelope) -> Unit) -> (SenderContext, Protos.PID, MessageEnvelope) -> Unit> = listOf(),
        val receiveMiddlewareChain: ((Context) -> Unit)? = null,
        val senderMiddlewareChain: ((SenderContext, Protos.PID, MessageEnvelope) -> Unit)? = null
) {
    fun withProducer(producer: () -> Actor): Props = copy(producer = producer)
    fun withDispatcher(dispatcher: Dispatcher): Props = copy(dispatcher = dispatcher)
    fun withMailbox(mailboxProducer: () -> Mailbox): Props = copy(mailboxProducer = mailboxProducer)
    fun withChildSupervisorStrategy(supervisorStrategy: SupervisorStrategy): Props = copy(supervisorStrategy = supervisorStrategy)
    fun withReceiveMiddleware(middleware: Array<((Context) -> Unit) -> (Context) -> Unit>): Props = copy(
            receiveMiddleware = (middleware).toList())
    // props.receiveMiddlewareChain = (ReceiveLocalContext.defaultReceive, { inner, outer -> outer(inner) })


    fun withSenderMiddleware(middleware: Array<((SenderContext, Protos.PID, MessageEnvelope) -> Unit) -> (SenderContext, Protos.PID, MessageEnvelope) -> Unit>): Props = copy(
            senderMiddleware = (middleware).toList())
    //props.senderMiddlewareChain = (SenderLocalContext.defaultSender, { inner, outer -> outer(inner) })


    fun withSpawner(spawner: (String, Props, Protos.PID?) -> Protos.PID): Props = copy(spawner = spawner)
    internal fun spawn(name: String, parent: Protos.PID?): Protos.PID = spawner(name, this, parent)
}

fun defaultSpawner(name: String, props: Props, parent: Protos.PID?): Protos.PID {
    val ctx = ActorContext(props.producer!!, props.supervisorStrategy, props.receiveMiddlewareChain, props.senderMiddlewareChain, parent)
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


