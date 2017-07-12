package proto.router

import proto.actor.*
import proto.router.messages.*
import proto.router.routers.IRouterConfig
import proto.router.routers.RouterState

open class RouterActor(private val routeeProps: Props, private val config: IRouterConfig, private val routerState: RouterState, private val wg: AutoResetEvent) : Actor {
    suspend override fun receiveAsync(context: IContext) {
        val message = context.message
        when (message) {
            is Started -> {
                config.onStarted(context, routeeProps, routerState)
                wg.set()
                return
            }
            is RouterAddRoutee -> {
                val r: Set<PID> = routerState.getRoutees()
                if (r.contains(message.pID)) {
                    return
                }
                context.watch(message.pID)
                routerState.setRoutees(r + message.pID)
                return
            }
            is RouterRemoveRoutee -> {
                val r: Set<PID> = routerState.getRoutees()
                if (!r.contains(message.pID)) {
                    return
                }
                context.unwatch(message.pID)
                routerState.setRoutees(r - message.pID)
                return
            }
            is RouterBroadcastMessage -> {
                routerState.getRoutees().forEach { it.request(message, context.sender!!) }
                return
            }
            is RouterGetRoutees -> {
                context.sender!!.tell(Routees(routerState.getRoutees()))
            }
        }
    }
}

