package actor.proto.router

import actor.proto.*
import java.util.concurrent.CountDownLatch


class RouterActor(private val config: RouterConfig, private val routerState: RouterState, private val wg: CountDownLatch) : Actor {
    suspend override fun Context.receive(message : Any) {
        val routerMessage = message
        when (routerMessage) {
            is Started -> {
                config.onStarted(this, routerState)
                wg.countDown()
            }
            is RouterAddRoutee -> {
                val r = routerState.getRoutees()
                if (!r.contains(routerMessage.pid)) {
                    watch(routerMessage.pid)
                    routerState.setRoutees(r + routerMessage.pid)
                }
            }
            is RouterRemoveRoutee -> {
                val r = routerState.getRoutees()
                if (r.contains(routerMessage.pid)) {
                    unwatch(routerMessage.pid)
                    routerState.setRoutees(r - routerMessage.pid)
                }
            }
            is RouterBroadcastMessage -> routerState.getRoutees().forEach {
                when (sender) {
                    null -> send(it, routerMessage.message)
                    else -> request(it, routerMessage.message, sender!!)
                }
            }
            is RouterGetRoutees -> respond(Routees(routerState.getRoutees()))
        }
    }
}

