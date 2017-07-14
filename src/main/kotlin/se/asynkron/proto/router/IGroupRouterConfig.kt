package proto.router.routers

import proto.actor.Context

interface IGroupRouterConfig : IRouterConfig {
    fun onStarted(context: Context, router: RouterState)
}

