package proto.router.routers

import proto.actor.Context
import proto.actor.PID

abstract class GroupRouterConfig(protected var routees: Set<PID>) : IGroupRouterConfig {
    override fun onStarted(context: Context, router: RouterState) {
        routees.forEach { context.watch(it) }
        router.setRoutees(routees)
    }
}

