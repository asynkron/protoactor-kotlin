package proto.router.routers

open internal class RoundRobinGroupRouterConfig : GroupRouterConfig {
    constructor(routees : Array<PID>)  {
        Routees = MutableSet<PID>(routees)
    }
    fun createRouterState () : RouterState {
        return RoundRobinRouterState()
    }
}

