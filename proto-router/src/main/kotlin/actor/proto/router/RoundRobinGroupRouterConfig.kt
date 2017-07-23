package actor.proto.router

import actor.proto.PID


internal class RoundRobinGroupRouterConfig(routees: Set<PID>) : GroupRouterConfig(routees) {
    override fun createRouterState(): RouterState = RoundRobinRouterState()
}

