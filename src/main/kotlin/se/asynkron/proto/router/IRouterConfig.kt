package proto.router.routers

import proto.actor.IContext
import proto.actor.Props

interface IRouterConfig {
    fun onStarted(context: IContext, props: Props, router: RouterState)
    fun createRouterState(): RouterState
}

