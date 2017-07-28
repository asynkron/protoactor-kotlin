@file:JvmName("Router")
package actor.proto.router

import actor.proto.*

fun newBroadcastGroup(routees: Set<PID>): Props = BroadcastGroupRouterConfig(routees).props
fun newConsistentHashGroup(routees: Set<PID>): Props = ConsistentHashGroupRouterConfig(MD5Hasher::hash, 100, routees).props
fun newConsistentHashGroup(hash: (String) -> Int, replicaCount: Int, routees: Set<PID>): Props = ConsistentHashGroupRouterConfig(hash, replicaCount, routees).props
fun newRandomGroup(routees: Set<PID>): Props = RandomGroupRouterConfig(0, routees).props
fun newRandomGroup(seed: Long, routees: Set<PID>): Props = RandomGroupRouterConfig(seed, routees).props
fun newRoundRobinGroup(routees: Set<PID>): Props = RoundRobinGroupRouterConfig(routees).props
fun newBroadcastPool(props: Props, poolSize: Int): Props = BroadcastPoolRouterConfig(poolSize,props).props
fun newConsistentHashPool(props: Props, poolSize: Int, hash: (String) -> Int, replicaCount: Int): Props = ConsistentHashPoolRouterConfig(poolSize, props, hash, replicaCount).props
fun newRandomPool(props: Props, poolSize: Int, seed: Long): Props = RandomPoolRouterConfig(poolSize, props, seed).props
fun newRoundRobinPool(props: Props, poolSize: Int): Props = RoundRobinPoolRouterConfig(poolSize,props).props


