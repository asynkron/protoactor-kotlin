package proto.router.routers

import proto.actor.IContext
import proto.actor.PID
import proto.actor.Props

abstract class GroupRouterConfig : IGroupRouterConfig {
    protected var Routees : Set<PID>? = null
    override fun onStarted (context : IContext, props : Props, router : RouterState) {
        Routees.orEmpty().forEach { context.watch(it) }
        router.setRoutees(Routees.orEmpty())
    }
    abstract fun createRouterState () : RouterState
}

