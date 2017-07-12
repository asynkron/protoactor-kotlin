package proto.router.routers

import proto.actor.PID

open internal class RandomGroupRouterConfig : GroupRouterConfig {
    private val _seed : Int
    constructor(seed : Int, routees : Array<PID>)  {
        _seed = seed
        Routees = setOf(*routees)
    }
    constructor(routees : Array<PID>)  {
        _seed = 0
        Routees = setOf(*routees)
    }
    override fun createRouterState () : RouterState {
        return RandomRouterState(_seed)
    }
}

