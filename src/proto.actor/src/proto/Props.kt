package proto

class Props {
    companion object {
        val empty : Props = Props()
        private fun produceDefaultMailbox () : IMailbox = UnboundedMailbox.create()
        fun defaultSpawner (name : String, props : Props, parent : PID) : PID {
            var ctx : LocalContext = LocalContext(props.producer, props.supervisorStrategy, props.receiveMiddlewareChain, props.senderMiddlewareChain, parent)
            var mailbox : IMailbox = props.mailboxProducer()
            var dispatcher : IDispatcher = props.dispatcher
            var reff : LocalProcess = LocalProcess(mailbox)
            var (pid, absent) = ProcessRegistry.instance.tryAdd(name, reff)
            if (absent) {
                throw ProcessNameExistException(name)
            }
            ctx.self = pid
            mailbox.registerHandlers(ctx, dispatcher)
            mailbox.postSystemMessage(Started.Instance)
            mailbox.start()
            return pid
        }
    }
    private var _spawner : (String, Props, PID) -> PID? = null
    var producer : () -> IActor = {->NullActor()}
    var mailboxProducer : () -> IMailbox = produceDefaultMailbox
    var supervisorStrategy : ISupervisorStrategy = Supervision.defaultStrategy
    var dispatcher : IDispatcher = Dispatchers.defaultDispatcher
    var receiveMiddleware : List<((IContext) -> Task) -> (IContext) -> Task> = mutableListOf()
    var senderMiddleware : List<((ISenderContext, PID, MessageEnvelope) -> Task) -> (ISenderContext, PID, MessageEnvelope) -> Task> = mutableListOf()
    var receiveMiddlewareChain : (IContext) -> Task
    var senderMiddlewareChain : (ISenderContext, PID, MessageEnvelope) -> Task
    var spawner : (String, Props, PID) -> PID = {_,_,_-> NullPid }
    fun withProducer (producer : () -> IActor) : Props = copy{ props -> props.producer = producer}
    fun withDispatcher (dispatcher : IDispatcher) : Props = copy{props -> props.dispatcher = dispatcher}
    fun withMailbox (mailboxProducer : () -> IMailbox) : Props = copy{props -> props.mailboxProducer = mailboxProducer}
    fun withChildSupervisorStrategy (supervisorStrategy : ISupervisorStrategy) : Props = copy{ props -> props.supervisorStrategy = supervisorStrategy}
    fun withReceiveMiddleware (middleware : Array<((IContext) -> Task) -> (IContext) -> Task>) : Props = copy{props -> 
        props.receiveMiddleware = (middleware).toList()
        props.receiveMiddlewareChain = (ReceiveLocalContext.defaultReceive, {inner, outer -> outer(inner)})
    }

    fun withSenderMiddleware (middleware : Array<((ISenderContext, PID, MessageEnvelope) -> Task) -> (ISenderContext, PID, MessageEnvelope) -> Task>) : Props = copy{props -> 
        props.senderMiddleware = (middleware).toList()
        props.senderMiddlewareChain = (SenderLocalContext.defaultSender, {inner, outer -> outer(inner)})
    }

    fun withSpawner (spawner : (String, Props, PID) -> PID) : Props = copy{props -> props.spawner = spawner}

    private fun copy (mutator : (Props) -> Unit) : Props {
        val props : Props = Props
        mutator(props)
        return props
    }
    internal fun spawn (name : String, parent : PID) : PID = spawner(name, this, parent)
}


