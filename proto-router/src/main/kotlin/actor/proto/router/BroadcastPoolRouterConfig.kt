package actor.proto.router

internal class BroadcastPoolRouterConfig(poolSize: Int) : PoolRouterConfig(poolSize) {
    override fun createRouterState(): RouterState = BroadcastRouterState()
}

