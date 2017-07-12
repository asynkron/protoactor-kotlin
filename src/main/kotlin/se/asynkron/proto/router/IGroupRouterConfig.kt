package proto.router.routers

import proto.actor.IContext
import proto.actor.Props

interface IGroupRouterConfig : IRouterConfig {
    fun onStarted(context: IContext, router: RouterState)
}

