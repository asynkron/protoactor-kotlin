package actor.proto.router

import actor.proto.PID


abstract class RouterState {
    abstract fun getRoutees(): Set<PID>
    abstract fun setRoutees(routees: Set<PID>)
    abstract fun routeMessage(message: Any)
}

