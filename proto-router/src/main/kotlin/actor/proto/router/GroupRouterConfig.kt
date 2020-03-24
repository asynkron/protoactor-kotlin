package actor.proto.router

import actor.proto.Context
import actor.proto.PID

abstract class GroupRouterConfig(private var routees: Set<PID>) : RouterConfig() {
    override fun onStarted(context: Context, router: RouterState) = router.setRoutees(routees)
}

