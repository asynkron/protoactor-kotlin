package actor.proto.router

import actor.proto.Context


interface IGroupRouterConfig : IRouterConfig {
    fun onStarted(context: Context, router: RouterState)
}

