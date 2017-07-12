package proto.router.routers

import proto.actor.IContext
import proto.actor.PID

abstract class GroupRouterConfig(protected var routees: Set<PID>) : IGroupRouterConfig {
    override fun onStarted(context: IContext, router: RouterState) {
        routees.forEach { context.watch(it) }
        router.setRoutees(routees)
    }
}

