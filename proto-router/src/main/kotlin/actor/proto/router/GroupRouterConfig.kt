package actor.proto.router

import actor.proto.*
import java.util.concurrent.CountDownLatch

abstract class GroupRouterConfig(private var routees: Set<PID>) : RouterConfig {
    open fun onStarted(context: Context, router: RouterState) {
        routees.forEach { context.watch(it) }
        router.setRoutees(routees)
    }

    fun spawner(): (String, Props, PID?) -> PID {
        fun spawnRouterProcess(name: String, props: Props, parent: PID?): PID {
            val routerState = createRouterState()
            val wg = CountDownLatch(1)
            val routerProps = props.withProducer { GroupRouterActor(this, routerState, wg) }
            val ctx = ActorContext(routerProps.producer!!, routerProps.supervisorStrategy, routerProps.receiveMiddleware, routerProps.senderMiddleware, parent)
            val mailbox = routerProps.mailboxProducer()
            val dispatcher = routerProps.dispatcher
            val reff = RouterProcess(routerState, mailbox)
            val pid = ProcessRegistry.add(name, reff)
            ctx.self = pid
            mailbox.registerHandlers(ctx, dispatcher)
            mailbox.postSystemMessage(Started)
            mailbox.start()
            wg.await()
            return pid
        }
        return { name, props, parent -> spawnRouterProcess(name, props, parent) }
    }
}

