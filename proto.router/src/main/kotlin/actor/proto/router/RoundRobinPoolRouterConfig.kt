package actor.proto.router

internal class RoundRobinPoolRouterConfig(poolSize: Int) : PoolRouterConfig(poolSize) {
    override fun createRouterState(): RouterState = RoundRobinRouterState()
}

