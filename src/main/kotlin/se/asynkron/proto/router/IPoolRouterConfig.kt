package proto.router.routers

import proto.actor.Context
import proto.actor.Props

interface IPoolRouterConfig : IRouterConfig {
    fun onStarted(context: Context, props: Props, router: RouterState)
}

