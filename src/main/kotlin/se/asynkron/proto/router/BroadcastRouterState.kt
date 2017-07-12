package proto.router.routers

import proto.actor.PID

internal class BroadcastRouterState : RouterState() {
    private var _routees : Set<PID>? = null
    override fun getRoutees () : Set<PID> = _routees.orEmpty()
    override fun setRoutees (routees : Set<PID>) {
        _routees = routees
    }
    override fun routeMessage (message : Any) {
        _routees.orEmpty().forEach { it.tell(message) }
    }
}

