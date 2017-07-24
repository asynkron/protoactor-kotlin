package actor.proto.router

import actor.proto.*

fun newBroadcastGroup(routees: Set<PID>): Props = Props().withSpawner(BroadcastGroupRouterConfig(routees).spawner())
fun newConsistentHashGroup(routees: Set<PID>): Props = Props().withSpawner(ConsistentHashGroupRouterConfig(MD5Hasher::hash, 100, routees).spawner())
fun newConsistentHashGroup(hash: (String) -> Int, replicaCount: Int, routees: Set<PID>): Props = Props().withSpawner(ConsistentHashGroupRouterConfig(hash, replicaCount, routees).spawner())
fun newRandomGroup(routees: Set<PID>): Props = Props().withSpawner(RandomGroupRouterConfig(0, routees).spawner())
fun newRandomGroup(seed: Long, routees: Set<PID>): Props = Props().withSpawner(RandomGroupRouterConfig(seed, routees).spawner())
fun newRoundRobinGroup(routees: Set<PID>): Props = Props().withSpawner(RoundRobinGroupRouterConfig(routees).spawner())
fun newBroadcastPool(props: Props, poolSize: Int): Props = Props().withSpawner(BroadcastPoolRouterConfig(poolSize).spawner(props))
fun newConsistentHashPool(props: Props, poolSize: Int, hash: (String) -> Int, replicaCount: Int): Props = Props().withSpawner(ConsistentHashPoolRouterConfig(poolSize, hash, replicaCount).spawner(props))
fun newRandomPool(props: Props, poolSize: Int, seed: Long): Props = Props().withSpawner(RandomPoolRouterConfig(poolSize, seed).spawner(props))
fun newRoundRobinPool(props: Props, poolSize: Int): Props = Props().withSpawner(RoundRobinPoolRouterConfig(poolSize).spawner(props))


