package proto.router.routers

import proto.actor.PID

internal class BroadcastGroupRouterConfig(routees: Array<PID>) : GroupRouterConfig() {
    override fun createRouterState () : RouterState {
        return BroadcastRouterState()
    }

    init {
        Routees = routees.toMutableSet()
    }
}

