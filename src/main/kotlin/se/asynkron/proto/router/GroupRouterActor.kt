package proto.router

import proto.actor.*
import proto.router.routers.IGroupRouterConfig
import proto.router.routers.IRouterConfig
import proto.router.routers.RouterState
import se.asynkron.proto.router.*
import java.util.concurrent.CountDownLatch

class GroupRouterActor(private val config: IGroupRouterConfig, private val routerState: RouterState, private val wg: CountDownLatch) : Actor {
    suspend override fun receiveAsync(context: IContext) {
        val message = context.message
        when (message) {
            is Started -> {
                config.onStarted(context, routerState)
                wg.countDown()
            }
            is RouterAddRoutee -> {
                val r: Set<PID> = routerState.getRoutees()
                if (!r.contains(message.pid)) {
                    context.watch(message.pid)
                    routerState.setRoutees(r + message.pid)
                }
            }
            is RouterRemoveRoutee -> {
                val r: Set<PID> = routerState.getRoutees()
                if (r.contains(message.pid)) {
                    context.unwatch(message.pid)
                    routerState.setRoutees(r - message.pid)
                }
            }
            is RouterBroadcastMessage -> routerState.getRoutees().forEach { it.request(message, context.sender!!) }
            is RouterGetRoutees -> context.sender!!.tell(Routees(routerState.getRoutees()))
        }
    }
}

