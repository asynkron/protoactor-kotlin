package proto.router

import proto.actor.*
import proto.router.routers.IRouterConfig
import proto.router.routers.RouterState
import se.asynkron.proto.router.*
import java.util.concurrent.CountDownLatch

open class RouterActor(private val routeeProps: Props, private val config: IRouterConfig, private val routerState: RouterState, private val wg: CountDownLatch) : Actor {
    suspend override fun receiveAsync(context: IContext) {
        val message = context.message
        when (message) {
            is Started -> {
                config.onStarted(context, routeeProps, routerState)
                wg.countDown()
                return
            }
            is RouterAddRoutee -> {
                val r: Set<PID> = routerState.getRoutees()
                if (r.contains(message.pid)) {
                    return
                }
                context.watch(message.pid)
                routerState.setRoutees(r + message.pid)
                return
            }
            is RouterRemoveRoutee -> {
                val r: Set<PID> = routerState.getRoutees()
                if (!r.contains(message.pid)) {
                    return
                }
                context.unwatch(message.pid)
                routerState.setRoutees(r - message.pid)
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

