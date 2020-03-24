package actor.proto.router

import actor.proto.Context
import actor.proto.Props

abstract class PoolRouterConfig(private val poolSize: Int, private val routeeProps:Props) : RouterConfig() {
    override fun onStarted(context: Context, router: RouterState) {
        val routees = (0 until poolSize).map { context.spawnChild(routeeProps) }.toSet()
        router.setRoutees(routees)
    }
}

