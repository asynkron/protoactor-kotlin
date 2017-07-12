package proto.router.routers

import proto.actor.PID

open internal class ConsistentHashGroupRouterConfig(private val hash: (String) -> Int, private var replicaCount: Int, routees: Set<PID>) : GroupRouterConfig(routees) {
    override fun createRouterState(): RouterState = ConsistentHashRouterState(hash, replicaCount)

    init {
        if (replicaCount <= 0) {
            throw IllegalArgumentException("ReplicaCount must be greater than 0")
        }
    }
}

