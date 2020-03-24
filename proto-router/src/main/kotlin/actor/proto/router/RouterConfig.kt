package actor.proto.router

import actor.proto.ActorContext
import actor.proto.Context
import actor.proto.ProcessRegistry
import actor.proto.Props
import actor.proto.Started
import actor.proto.withSpawner
import java.util.concurrent.CountDownLatch

abstract class RouterConfig {
    abstract fun onStarted(context: Context, router: RouterState)
    abstract fun createRouterState(): RouterState

    val props: Props = Props().withSpawner { name, props, parent ->
        val routerState = createRouterState()
        val mailbox = props.mailboxProducer()
        val wg = CountDownLatch(1)
        val self = ProcessRegistry.put(name, RouterProcess(routerState, mailbox))
        val ctx = ActorContext({ RouterActor(this, routerState, wg) }, self, props.supervisorStrategy, props.receiveMiddleware, props.senderMiddleware, parent)
        mailbox.registerHandlers(ctx, props.dispatcher)
        mailbox.postSystemMessage(Started)
        mailbox.start()
        wg.await()
        self
    }
}

