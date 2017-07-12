package proto.router.routers

import proto.actor.PID

internal class BroadcastRouterState : RouterState() {
    private lateinit var routees: Set<PID>
    override fun getRoutees(): Set<PID> = routees
    override fun setRoutees(routees: Set<PID>) {
        this.routees = routees
    }

    override fun routeMessage(message: Any) {
        routees.forEach { it.tell(message) }
    }
}

