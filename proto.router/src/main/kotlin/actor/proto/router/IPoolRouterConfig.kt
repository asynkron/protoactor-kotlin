package actor.proto.router

import actor.proto.Context
import actor.proto.Props

interface IPoolRouterConfig : IRouterConfig {
    fun onStarted(context: Context, props: Props, router: RouterState)
}

