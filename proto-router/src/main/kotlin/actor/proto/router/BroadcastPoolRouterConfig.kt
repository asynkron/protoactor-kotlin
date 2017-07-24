package actor.proto.router

import actor.proto.Props

internal class BroadcastPoolRouterConfig(poolSize: Int, routeeProps: Props) : PoolRouterConfig(poolSize,routeeProps) {
    override fun createRouterState(): RouterState = BroadcastRouterState()
}

