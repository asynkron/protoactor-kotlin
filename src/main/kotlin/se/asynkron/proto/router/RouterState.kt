package proto.router.routers

import proto.actor.PID

abstract class RouterState {
    abstract fun getRoutees () : Set<PID>
    abstract fun setRoutees (routees : Set<PID>)
    abstract fun routeMessage (message : Any)
}

