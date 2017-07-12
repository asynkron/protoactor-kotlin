package proto.router.routers

import proto.actor.IContext
import proto.actor.PID
import proto.actor.Props

abstract class GroupRouterConfig(protected var routees: Set<PID>) : IGroupRouterConfig {
    override fun onStarted(context: IContext, props: Props, router: RouterState) {
        routees.forEach { context.watch(it) }
        router.setRoutees(routees)
    }
}

