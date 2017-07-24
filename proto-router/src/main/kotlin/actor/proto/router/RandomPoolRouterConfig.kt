package actor.proto.router

import actor.proto.Props

internal class RandomPoolRouterConfig(poolSize: Int, routeeProps: Props, private val seed: Long) : PoolRouterConfig(poolSize,routeeProps) {
    override fun createRouterState(): RouterState = RandomRouterState(seed)
}

