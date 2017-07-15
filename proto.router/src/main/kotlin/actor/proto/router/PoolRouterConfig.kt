package actor.proto.router

import actor.proto.Context
import actor.proto.Props

abstract class PoolRouterConfig(private val poolSize: Int) : RouterConfig {
    open fun onStarted(context: Context, props: Props, router: RouterState) {
        val routees = (0 until poolSize).map { context.spawn(props) }.toSet()
        router.setRoutees(routees)
    }
}

