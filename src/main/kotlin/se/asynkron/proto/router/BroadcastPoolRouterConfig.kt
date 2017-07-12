package proto.router.routers

internal class BroadcastPoolRouterConfig(poolSize: Int) : PoolRouterConfig(poolSize) {
    override fun createRouterState () : RouterState {
        return BroadcastRouterState()
    }
}

