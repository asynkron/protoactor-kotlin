package proto.router.routers

import proto.actor.IContext
import proto.actor.Props

interface IRouterConfig {

    fun createRouterState(): RouterState
}

