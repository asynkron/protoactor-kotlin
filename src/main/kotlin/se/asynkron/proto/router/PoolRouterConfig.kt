package proto.router.routers

import proto.actor.IContext
import proto.actor.PID
import proto.actor.Props

internal abstract class PoolRouterConfig(poolSize: Int) : IPoolRouterConfig {
    private var _poolSize: Int = poolSize
    override fun onStarted(context: IContext, props: Props, router: RouterState) {
        var routees: Iterable<PID> = .select.map{ x -> context.spawn(props) }
        router.setRoutees(routees)
    }
}

