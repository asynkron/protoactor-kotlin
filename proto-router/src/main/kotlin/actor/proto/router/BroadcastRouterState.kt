package actor.proto.router

import actor.proto.PID
import actor.proto.send


internal class BroadcastRouterState : RouterState() {
    private lateinit var routees: Set<PID>
    override fun getRoutees(): Set<PID> = routees
    override fun setRoutees(routees: Set<PID>) {
        this.routees = routees
    }

    override fun routeMessage(message: Any) {
        routees.forEach { send(it,message) }
    }
}

