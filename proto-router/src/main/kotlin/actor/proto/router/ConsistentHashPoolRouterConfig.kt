package actor.proto.router

import actor.proto.Props

internal class ConsistentHashPoolRouterConfig(poolSize: Int, routeeProps: Props, private val hash: (String) -> Int, private val replicaCount: Int) : PoolRouterConfig(poolSize,routeeProps) {
    override fun createRouterState(): RouterState = ConsistentHashRouterState(hash, replicaCount)

    init {
        if (replicaCount <= 0) {
            throw IllegalArgumentException("ReplicaCount must be greater than 0")
        }
    }
}

