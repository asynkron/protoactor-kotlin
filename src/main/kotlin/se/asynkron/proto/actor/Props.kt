package proto.actor

import proto.mailbox.Dispatchers
import proto.mailbox.Dispatcher
import proto.mailbox.Mailbox
import proto.mailbox.UnboundedMailbox

class Props {
    var spawner: (String, Props, PID?) -> PID = ::defaultSpawner
    var producer: (() -> Actor)? = null
    var mailboxProducer: () -> Mailbox = { -> UnboundedMailbox.create() }
    var supervisorStrategy: SupervisorStrategy = Supervision.defaultStrategy
    var dispatcher: Dispatcher = Dispatchers.DEFAULT_DISPATCHER
    var receiveMiddleware: List<((IContext) -> Unit) -> (IContext) -> Unit> = mutableListOf()
    var senderMiddleware: List<((SenderContext, PID, MessageEnvelope) -> Unit) -> (SenderContext, PID, MessageEnvelope) -> Unit> = mutableListOf()
    var receiveMiddlewareChain: ((IContext) -> Unit)? = null
    var senderMiddlewareChain: ((SenderContext, PID, MessageEnvelope) -> Unit)? = null

    fun withProducer(producer: () -> Actor): Props = copy { it.producer = producer }
    fun withDispatcher(dispatcher: Dispatcher): Props = copy { it.dispatcher = dispatcher }
    fun withMailbox(mailboxProducer: () -> Mailbox): Props = copy { it.mailboxProducer = mailboxProducer }
    fun withChildSupervisorStrategy(supervisorStrategy: SupervisorStrategy): Props = copy { it.supervisorStrategy = supervisorStrategy }
    fun withReceiveMiddleware(middleware: Array<((IContext) -> Unit) -> (IContext) -> Unit>): Props = copy {
        it.receiveMiddleware = (middleware).toList()
        // props.receiveMiddlewareChain = (ReceiveLocalContext.defaultReceive, { inner, outer -> outer(inner) })
    }

    fun withSenderMiddleware(middleware: Array<((SenderContext, PID, MessageEnvelope) -> Unit) -> (SenderContext, PID, MessageEnvelope) -> Unit>): Props = copy {
        it.senderMiddleware = (middleware).toList()
        //props.senderMiddlewareChain = (SenderLocalContext.defaultSender, { inner, outer -> outer(inner) })
    }

    fun withSpawner(spawner: (String, Props, PID?) -> PID): Props = copy { it.spawner = spawner }

    private fun copy(mutator: (Props) -> Unit): Props {
        val props: Props = Props()
        props.producer = this.producer
        props.dispatcher = this.dispatcher
        props.mailboxProducer = this.mailboxProducer
        props.receiveMiddleware = this.receiveMiddleware
        props.senderMiddleware = this.senderMiddleware
        props.spawner = this.spawner
        props.supervisorStrategy = this.supervisorStrategy
        mutator(props)
        return props
    }

    internal fun spawn(name: String, parent: PID?): PID = spawner(name, this, parent)
}

fun defaultSpawner(name: String, props: Props, parent: PID?): PID {
    val ctx: Context = Context(props.producer!!, props.supervisorStrategy, props.receiveMiddlewareChain, props.senderMiddlewareChain, parent)
    val mailbox: Mailbox = props.mailboxProducer()
    val dispatcher: Dispatcher = props.dispatcher
    val reff: LocalProcess = LocalProcess(mailbox)
    val (pid, ok) = ProcessRegistry.tryAdd(name, reff)
    if (!ok) {
        throw ProcessNameExistException(name)
    }
    ctx.self = pid
    mailbox.registerHandlers(ctx, dispatcher)
    mailbox.postSystemMessage(Started)
    mailbox.start()
    return pid
}


