package proto.router.routers

import proto.actor.PID

open internal class RoundRobinGroupRouterConfig(routees: Set<PID>) : GroupRouterConfig(routees) {
    override fun createRouterState(): RouterState = RoundRobinRouterState()
}

