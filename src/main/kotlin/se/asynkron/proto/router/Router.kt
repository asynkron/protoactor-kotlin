package proto.router

import proto.actor.*
import proto.mailbox.Dispatcher
import proto.mailbox.Mailbox
import proto.router.routers.*
import java.util.concurrent.CountDownLatch

object Router {
    fun newBroadcastGroup(routees: Set<PID>): Props = Props().withSpawner(spawner(BroadcastGroupRouterConfig(routees)))
    fun newConsistentHashGroup(routees: Set<PID>): Props = Props().withSpawner(spawner(ConsistentHashGroupRouterConfig(MD5Hasher::hash, 100, routees)))
    fun newConsistentHashGroup(hash: (String) -> Int, replicaCount: Int, routees: Set<PID>): Props = Props().withSpawner(spawner(ConsistentHashGroupRouterConfig(hash, replicaCount, routees)))
    fun newRandomGroup(routees: Set<PID>): Props = Props().withSpawner(spawner(RandomGroupRouterConfig(0, routees)))
    fun newRandomGroup(seed: Long, routees: Set<PID>): Props = Props().withSpawner(spawner(RandomGroupRouterConfig(seed, routees)))
    fun newRoundRobinGroup(routees: Set<PID>): Props = Props().withSpawner(spawner(RoundRobinGroupRouterConfig(routees)))
    fun newBroadcastPool(props: Props, poolSize: Int): Props = props.withSpawner(spawner(BroadcastPoolRouterConfig(poolSize)))
    fun newConsistentHashPool(props: Props, poolSize: Int, hash: (String) -> Int, replicaCount: Int): Props = props.withSpawner(spawner(ConsistentHashPoolRouterConfig(poolSize, hash, replicaCount)))
    fun newRandomPool(props: Props, poolSize: Int, seed: Long): Props = props.withSpawner(spawner(RandomPoolRouterConfig(poolSize, seed)))
    fun newRoundRobinPool(props: Props, poolSize: Int): Props = props.withSpawner(spawner(RoundRobinPoolRouterConfig(poolSize)))

    fun spawner(config: IRouterConfig): (String, Props, PID?) -> PID {
        fun spawnRouterProcess(name: String, props: Props, parent: PID?): PID {
            val routeeProps: Props = props.withSpawner(::defaultSpawner)
            val routerState: RouterState = config.createRouterState()
            val wg: CountDownLatch = CountDownLatch(1)
            val routerProps: Props = fromProducer { -> RouterActor(routeeProps, config, routerState, wg) }.withMailbox(props.mailboxProducer)
            val ctx: Context = Context(routerProps.producer!!, props.supervisorStrategy, props.receiveMiddlewareChain, props.senderMiddlewareChain, parent)
            val mailbox: Mailbox = routerProps.mailboxProducer()
            val dispatcher: Dispatcher = routerProps.dispatcher
            val reff: Process = RouterProcess(routerState, mailbox)
            val (pid, absent) = ProcessRegistry.tryAdd(name, reff)
            if (!absent) {
                throw ProcessNameExistException(name)
            }
            ctx.self = pid
            mailbox.registerHandlers(ctx, dispatcher)
            mailbox.postSystemMessage(Started)
            mailbox.start()
            wg.await()
            return pid
        }
        return ::spawnRouterProcess
    }
}

