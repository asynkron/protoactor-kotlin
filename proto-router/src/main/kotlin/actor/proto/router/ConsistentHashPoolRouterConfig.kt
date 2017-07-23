package actor.proto.router

internal class ConsistentHashPoolRouterConfig(poolSize: Int, private val hash: (String) -> Int, private val replicaCount: Int) : PoolRouterConfig(poolSize) {
    override fun createRouterState(): RouterState = ConsistentHashRouterState(hash, replicaCount)

    init {
        if (replicaCount <= 0) {
            throw IllegalArgumentException("ReplicaCount must be greater than 0")
        }
    }
}

