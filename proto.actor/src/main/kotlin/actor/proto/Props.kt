package actor.proto

import actor.proto.mailbox.Dispatcher
import actor.proto.mailbox.unboundedMailbox
import actor.proto.mailbox.Mailbox

data class Props(
        private val spawner: (name: String, props: Props, parent: PID?) -> PID = ::defaultSpawner,
        val producer: (() -> Actor)? = null,
        val mailboxProducer: () -> Mailbox = { unboundedMailbox() },
        val supervisorStrategy: SupervisorStrategy = Supervision.defaultStrategy,
        val dispatcher: Dispatcher = actor.proto.mailbox.Dispatchers.DEFAULT_DISPATCHER,
        private val receiveMiddleware: List<((Context) -> Unit) -> (Context) -> Unit> = listOf(),
        private val senderMiddleware: List<((SenderContext, PID, MessageEnvelope) -> Unit) -> (SenderContext, PID, MessageEnvelope) -> Unit> = listOf(),
        val receiveMiddlewareChain: ((Context) -> Unit)? = null,
        val senderMiddlewareChain: ((SenderContext, PID, MessageEnvelope) -> Unit)? = null
) {
    fun withProducer(producer: () -> Actor): Props = copy(producer = producer)
    fun withDispatcher(dispatcher: Dispatcher): Props = copy(dispatcher = dispatcher)
    fun withMailbox(mailboxProducer: () -> Mailbox): Props = copy(mailboxProducer = mailboxProducer)
    fun withChildSupervisorStrategy(supervisorStrategy: SupervisorStrategy): Props = copy(supervisorStrategy = supervisorStrategy)
    fun withReceiveMiddleware(middleware: Array<((Context) -> Unit) -> (Context) -> Unit>): Props = copy(
            receiveMiddleware = (middleware).toList())
    // props.receiveMiddlewareChain = (ReceiveLocalContext.defaultReceive, { inner, outer -> outer(inner) })


    fun withSenderMiddleware(middleware: Array<((SenderContext, PID, MessageEnvelope) -> Unit) -> (SenderContext, PID, MessageEnvelope) -> Unit>): Props = copy(
            senderMiddleware = (middleware).toList())
    //props.senderMiddlewareChain = (SenderLocalContext.defaultSender, { inner, outer -> outer(inner) })


    fun withSpawner(spawner: (String, Props, PID?) -> PID): Props = copy(spawner = spawner)
    internal fun spawn(name: String, parent: PID?): PID = spawner(name, this, parent)
}

fun defaultSpawner(name: String, props: Props, parent: PID?): PID {
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


