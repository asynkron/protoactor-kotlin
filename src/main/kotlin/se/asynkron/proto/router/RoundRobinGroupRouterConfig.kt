package proto.router.routers

import proto.actor.PID

open internal class RoundRobinGroupRouterConfig(routees: Array<PID>) : GroupRouterConfig() {
    override fun createRouterState () : RouterState = RoundRobinRouterState()

    init {
        Routees = setOf(*routees)
    }
}

