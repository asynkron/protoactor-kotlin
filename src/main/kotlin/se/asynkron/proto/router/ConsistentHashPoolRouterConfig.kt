package proto.router.routers

open internal class ConsistentHashPoolRouterConfig(poolSize: Int, hash: (String) -> Int,val replicaCount: Int) : PoolRouterConfig(poolSize) {
    private val _hash : (String) -> Int = hash
    override fun createRouterState () : RouterState = ConsistentHashRouterState(_hash, replicaCount)

    init {
        if (replicaCount <= 0) {
            throw IllegalArgumentException("ReplicaCount must be greater than 0")
        }
    }
}

