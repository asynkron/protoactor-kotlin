package actor.proto.router

import actor.proto.*
import java.util.concurrent.CountDownLatch

abstract class RouterConfig {
    abstract fun onStarted(context: Context, router: RouterState)
    abstract fun createRouterState(): RouterState

    fun spawner(): (String, Props, PID?) -> PID {
        return fun (name: String, props: Props, parent: PID?): PID {
            val routerState = createRouterState()
            val wg = CountDownLatch(1)
            val routerProps = props.withProducer { RouterActor(this, routerState, wg) }
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
    }
}

