package proto.router.routers

import proto.actor.PID

open internal class RandomGroupRouterConfig(private val seed: Long, routees: Set<PID>) : GroupRouterConfig(routees) {
    override fun createRouterState(): RouterState = RandomRouterState(seed)
}

