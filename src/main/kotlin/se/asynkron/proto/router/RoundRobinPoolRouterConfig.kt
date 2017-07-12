package proto.router.routers

internal class RoundRobinPoolRouterConfig(poolSize: Int) : PoolRouterConfig(poolSize) {
    override fun createRouterState(): RouterState = RoundRobinRouterState()
}

