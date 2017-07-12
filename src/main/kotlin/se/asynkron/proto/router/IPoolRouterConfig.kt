package proto.router.routers

import proto.actor.IContext
import proto.actor.Props

interface IPoolRouterConfig : IRouterConfig {
    fun onStarted(context: IContext, props: Props, router: RouterState)
}

