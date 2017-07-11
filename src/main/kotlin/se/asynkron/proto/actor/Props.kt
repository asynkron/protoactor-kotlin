package proto.actor

import proto.mailbox.Dispatchers
import proto.mailbox.Dispatcher
import proto.mailbox.Mailbox
import proto.mailbox.UnboundedMailbox

class Props {
    private fun produceDefaultMailbox(): Mailbox = UnboundedMailbox.create()
    private fun defaultSpawner(name: String, props: Props, parent: PID?): PID {
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
        mailbox.postSystemMessage(Started.Instance)
        mailbox.start()
        return pid
    }

    private var spawner: (String, Props, PID?) -> PID = this::defaultSpawner
    private var producer: (() -> IActor)? = null
    private var mailboxProducer: () -> Mailbox = { -> produceDefaultMailbox() }
    private var supervisorStrategy: ISupervisorStrategy = Supervision.defaultStrategy
    private var dispatcher: Dispatcher = Dispatchers.DEFAULT_DISPATCHER
    private var receiveMiddleware: List<((IContext) -> Unit) -> (IContext) -> Unit> = mutableListOf()
    private var senderMiddleware: List<((ISenderContext, PID, MessageEnvelope) -> Unit) -> (ISenderContext, PID, MessageEnvelope) -> Unit> = mutableListOf()
    private var receiveMiddlewareChain: ((IContext) -> Unit)? = null
    private var senderMiddlewareChain: ((ISenderContext, PID, MessageEnvelope) -> Unit)? = null

    fun withProducer(producer: () -> IActor): Props = copy { it.producer = producer }
    fun withDispatcher(dispatcher: Dispatcher): Props = copy { it.dispatcher = dispatcher }
    fun withMailbox(mailboxProducer: () -> Mailbox): Props = copy { it.mailboxProducer = mailboxProducer }
    fun withChildSupervisorStrategy(supervisorStrategy: ISupervisorStrategy): Props = copy { it.supervisorStrategy = supervisorStrategy }
    fun withReceiveMiddleware(middleware: Array<((IContext) -> Unit) -> (IContext) -> Unit>): Props = copy {
        it.receiveMiddleware = (middleware).toList()
        // props.receiveMiddlewareChain = (ReceiveLocalContext.defaultReceive, { inner, outer -> outer(inner) })
    }

    fun withSenderMiddleware(middleware: Array<((ISenderContext, PID, MessageEnvelope) -> Unit) -> (ISenderContext, PID, MessageEnvelope) -> Unit>): Props = copy {
        it.senderMiddleware = (middleware).toList()
        //props.senderMiddlewareChain = (SenderLocalContext.defaultSender, { inner, outer -> outer(inner) })
    }

    fun withSpawner(spawner: (String, Props, PID?) -> PID): Props = copy { it.spawner = spawner }

    private fun copy(mutator: (Props) -> Unit): Props {
        val props: Props = Props().apply {
            dispatcher = this.dispatcher
            mailboxProducer = this.mailboxProducer
            producer = this.producer
            receiveMiddleware = this.receiveMiddleware
            //  receiveMiddlewareChain = this.receiveMiddlewareChain
            senderMiddleware = this.senderMiddleware
            //  senderMiddlewareChain = this.senderMiddlewareChain
            spawner = this.spawner
            supervisorStrategy = this.supervisorStrategy
        }
        mutator(props)
        return props
    }

    internal fun spawn(name: String, parent: PID?): PID = spawner(name, this, parent)
}


