package proto.router.routers

import proto.actor.PID

internal class BroadcastGroupRouterConfig(routees: Set<PID>) : GroupRouterConfig(routees) {
    override fun createRouterState(): RouterState = BroadcastRouterState()
}

