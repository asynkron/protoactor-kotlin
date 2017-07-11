package proto.actor

import proto.mailbox.Dispatchers
import proto.mailbox.IDispatcher
import proto.mailbox.IMailbox
import proto.mailbox.UnboundedMailbox

class Props {
    private fun produceDefaultMailbox(): IMailbox = UnboundedMailbox.create()
    private fun defaultSpawner(name: String, props: Props, parent: PID?): PID {
        val ctx: LocalContext = LocalContext(props.producer!!, props.supervisorStrategy, props.receiveMiddlewareChain, props.senderMiddlewareChain, parent)
        val mailbox: IMailbox = props.mailboxProducer()
        val dispatcher: IDispatcher = props.dispatcher
        val reff: LocalProcess = LocalProcess(mailbox)
        val (pid, absent) = ProcessRegistry.tryAdd(name, reff)
        if (absent) {
            throw ProcessNameExistException(name)
        }
        ctx.self = pid
        mailbox.registerHandlers(ctx, dispatcher)
        mailbox.postSystemMessage(Started.Instance)
        mailbox.start()
        return pid
    }

    private var spawner: (String, Props, PID?) -> PID = this::defaultSpawner
    var producer: (() -> IActor)? = null
    var mailboxProducer: () -> IMailbox = { -> produceDefaultMailbox() }
    var supervisorStrategy: ISupervisorStrategy = Supervision.defaultStrategy
    var dispatcher: IDispatcher = Dispatchers.defaultDispatcher
    var receiveMiddleware: List<((IContext) -> Unit) -> (IContext) -> Unit> = mutableListOf()
    var senderMiddleware: List<((ISenderContext, PID, MessageEnvelope) -> Unit) -> (ISenderContext, PID, MessageEnvelope) -> Unit> = mutableListOf()
    var receiveMiddlewareChain: ((IContext) -> Unit)? = null
    var senderMiddlewareChain: ((ISenderContext, PID, MessageEnvelope) -> Unit)? = null

    fun withProducer(producer: () -> IActor): Props = copy { props -> props.producer = producer }
    fun withDispatcher(dispatcher: IDispatcher): Props = copy { props -> props.dispatcher = dispatcher }
    fun withMailbox(mailboxProducer: () -> IMailbox): Props = copy { props -> props.mailboxProducer = mailboxProducer }
    fun withChildSupervisorStrategy(supervisorStrategy: ISupervisorStrategy): Props = copy { props -> props.supervisorStrategy = supervisorStrategy }
    fun withReceiveMiddleware(middleware: Array<((IContext) -> Unit) -> (IContext) -> Unit>): Props = copy { props ->
        props.receiveMiddleware = (middleware).toList()
        // props.receiveMiddlewareChain = (ReceiveLocalContext.defaultReceive, { inner, outer -> outer(inner) })
    }

    fun withSenderMiddleware(middleware: Array<((ISenderContext, PID, MessageEnvelope) -> Unit) -> (ISenderContext, PID, MessageEnvelope) -> Unit>): Props = copy { props ->
        props.senderMiddleware = (middleware).toList()
        //props.senderMiddlewareChain = (SenderLocalContext.defaultSender, { inner, outer -> outer(inner) })
    }

    fun withSpawner(spawner: (String, Props, PID?) -> PID): Props = copy { props -> props.spawner = spawner }

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


