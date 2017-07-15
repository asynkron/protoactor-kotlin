package actor.proto.router

import actor.proto.PID

internal class RandomGroupRouterConfig(private val seed: Long, routees: Set<PID>) : GroupRouterConfig(routees) {
    override fun createRouterState(): RouterState = RandomRouterState(seed)
}

