package actor.proto.router

import actor.proto.*
import java.util.concurrent.CountDownLatch

fun newBroadcastGroup(routees: Set<PID>): Props = Props().withSpawner(groupSpawner(BroadcastGroupRouterConfig(routees)))
fun newConsistentHashGroup(routees: Set<PID>): Props = Props().withSpawner(groupSpawner(ConsistentHashGroupRouterConfig(MD5Hasher::hash, 100, routees)))
fun newConsistentHashGroup(hash: (String) -> Int, replicaCount: Int, routees: Set<PID>): Props = Props().withSpawner(groupSpawner(ConsistentHashGroupRouterConfig(hash, replicaCount, routees)))
fun newRandomGroup(routees: Set<PID>): Props = Props().withSpawner(groupSpawner(RandomGroupRouterConfig(0, routees)))
fun newRandomGroup(seed: Long, routees: Set<PID>): Props = Props().withSpawner(groupSpawner(RandomGroupRouterConfig(seed, routees)))
fun newRoundRobinGroup(routees: Set<PID>): Props = Props().withSpawner(groupSpawner(RoundRobinGroupRouterConfig(routees)))
fun newBroadcastPool(props: Props, poolSize: Int): Props = Props().withSpawner(poolSpawner(BroadcastPoolRouterConfig(poolSize), props))
fun newConsistentHashPool(props: Props, poolSize: Int, hash: (String) -> Int, replicaCount: Int): Props = Props().withSpawner(poolSpawner(ConsistentHashPoolRouterConfig(poolSize, hash, replicaCount), props))
fun newRandomPool(props: Props, poolSize: Int, seed: Long): Props = Props().withSpawner(poolSpawner(RandomPoolRouterConfig(poolSize, seed), props))
fun newRoundRobinPool(props: Props, poolSize: Int): Props = Props().withSpawner(poolSpawner(RoundRobinPoolRouterConfig(poolSize), props))

private fun groupSpawner(config: GroupRouterConfig): (String, Props, PID?) -> PID {
    fun spawnRouterProcess(name: String, @Suppress("UNUSED_PARAMETER") props: Props, parent: PID?): PID {
        val routerState = config.createRouterState()
        val wg = CountDownLatch(1)
        val routerProps = fromProducer { -> GroupRouterActor(config, routerState, wg) }
        val ctx = ActorContext(routerProps.producer!!, routerProps.supervisorStrategy, routerProps.receiveMiddlewareChain, routerProps.senderMiddlewareChain, parent)
        val mailbox = routerProps.mailboxProducer()
        val dispatcher = routerProps.dispatcher
        val reff = RouterProcess(routerState, mailbox)
        val pid = ProcessRegistry.add(name, reff)
        ctx.self = pid
        mailbox.registerHandlers(ctx, dispatcher)
        mailbox.postSystemMessage(Started)
        mailbox.start()
        wg.await()
        return pid
    }
    return { name, props, parent -> spawnRouterProcess(name, props, parent) }
}

private fun poolSpawner(config: PoolRouterConfig, routeeProps: Props): (String, Props, PID?) -> PID {
    fun spawnRouterProcess(name: String, @Suppress("UNUSED_PARAMETER") props: Props, parent: PID?): PID {
        val routerState = config.createRouterState()
        val wg = CountDownLatch(1)
        val routerProps = fromProducer { -> PoolRouterActor(routeeProps, config, routerState, wg) }
        val ctx = ActorContext(routerProps.producer!!, routerProps.supervisorStrategy, routerProps.receiveMiddlewareChain, routerProps.senderMiddlewareChain, parent)
        val mailbox = routerProps.mailboxProducer()
        val dispatcher = routerProps.dispatcher
        val reff = RouterProcess(routerState, mailbox)
        val pid = ProcessRegistry.add(name, reff)
        ctx.self = pid
        mailbox.registerHandlers(ctx, dispatcher)
        mailbox.postSystemMessage(Started)
        mailbox.start()
        wg.await()
        return pid
    }
    return { name, props, parent -> spawnRouterProcess(name, props, parent) }
}
