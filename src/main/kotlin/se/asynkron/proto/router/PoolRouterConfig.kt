package proto.router.routers

import proto.actor.Context
import proto.actor.PID
import proto.actor.Props

internal abstract class PoolRouterConfig(val poolSize: Int) : IPoolRouterConfig {
    override fun onStarted(context: Context, props: Props, router: RouterState) {
        val routees: Set<PID> = (0 until poolSize).map { context.spawn(props) }.toSet()
        router.setRoutees(routees)
    }
}

