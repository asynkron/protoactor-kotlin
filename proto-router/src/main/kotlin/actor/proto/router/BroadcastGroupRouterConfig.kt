package actor.proto.router

import actor.proto.PID

internal class BroadcastGroupRouterConfig(routees: Set<PID>) : GroupRouterConfig(routees) {
    override fun createRouterState(): RouterState = BroadcastRouterState()
}

