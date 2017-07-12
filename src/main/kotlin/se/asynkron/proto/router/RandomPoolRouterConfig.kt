package proto.router.routers

internal class RandomPoolRouterConfig(poolSize: Int, private val seed: Long) : PoolRouterConfig(poolSize) {
    override fun createRouterState(): RouterState = RandomRouterState(seed)
}

