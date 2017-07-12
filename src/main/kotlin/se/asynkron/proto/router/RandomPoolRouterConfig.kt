package proto.router.routers

open internal class RandomPoolRouterConfig(poolSize: Int, private val seed: Long) : PoolRouterConfig(poolSize) {
    override fun createRouterState(): RouterState = RandomRouterState(seed)
}

