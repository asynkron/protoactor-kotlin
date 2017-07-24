package actor.proto.router

import actor.proto.Props

internal class RoundRobinPoolRouterConfig(poolSize: Int,routeeProps: Props) : PoolRouterConfig(poolSize,routeeProps) {
    override fun createRouterState(): RouterState = RoundRobinRouterState()
}

