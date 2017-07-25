package actor.proto.router

import actor.proto.*
import java.util.concurrent.CountDownLatch


class RouterActor(private val config: RouterConfig, private val routerState: RouterState, private val wg: CountDownLatch) : Actor {
    suspend override fun receive(context: Context) {
        val routerMessage = context.message
        when (routerMessage) {
            is Started -> {
                config.onStarted(context, routerState)
                wg.countDown()
            }
            is RouterAddRoutee -> {
                val r = routerState.getRoutees()
                if (!r.contains(routerMessage.pid)) {
                    context.watch(routerMessage.pid)
                    routerState.setRoutees(r + routerMessage.pid)
                }
            }
            is RouterRemoveRoutee -> {
                val r = routerState.getRoutees()
                if (r.contains(routerMessage.pid)) {
                    context.unwatch(routerMessage.pid)
                    routerState.setRoutees(r - routerMessage.pid)
                }
            }
            is RouterBroadcastMessage -> routerState.getRoutees().forEach {
                when(context.sender) {
                     null -> it.send(routerMessage.message)
                    else -> it.request(routerMessage.message, context.sender!!)
                }
            }
            is RouterGetRoutees -> context.sender!!.send(Routees(routerState.getRoutees()))
        }
    }
}

