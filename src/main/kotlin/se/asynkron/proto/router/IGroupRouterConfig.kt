package proto.router.routers

import proto.actor.IContext

interface IGroupRouterConfig : IRouterConfig {
    fun onStarted(context: IContext, router: RouterState)
}

