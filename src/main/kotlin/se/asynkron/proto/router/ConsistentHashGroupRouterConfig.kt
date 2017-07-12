package proto.router.routers

import proto.actor.PID

open internal class ConsistentHashGroupRouterConfig(hash: (String) -> Int, replicaCount: Int, routees: Array<PID>) : GroupRouterConfig() {
    private val _hash : (String) -> Int = hash
    private var _replicaCount : Int = replicaCount
    override fun createRouterState () : RouterState {
        return ConsistentHashRouterState(_hash, _replicaCount)
    }

    init {
        if (replicaCount <= 0) {
            throw IllegalArgumentException("ReplicaCount must be greater than 0")
        }
        Routees = setOf(*routees)
    }
}

