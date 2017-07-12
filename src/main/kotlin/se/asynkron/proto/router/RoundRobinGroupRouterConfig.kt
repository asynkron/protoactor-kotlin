package proto.router.routers

import proto.actor.PID

internal class RoundRobinGroupRouterConfig(routees: Set<PID>) : GroupRouterConfig(routees) {
    override fun createRouterState(): RouterState = RoundRobinRouterState()
}

