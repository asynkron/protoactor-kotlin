package proto.router

import proto.actor.*
import proto.mailbox.Dispatcher
import proto.mailbox.Mailbox
import proto.router.routers.*

object Router {
    fun newBroadcastGroup (routees : Array<PID>) : Props {
        return Props().withSpawner(spawner(BroadcastGroupRouterConfig(routees)))
    }
    fun newConsistentHashGroup (routees : Array<PID>) : Props {
        return Props().withSpawner(spawner(ConsistentHashGroupRouterConfig(MD5Hasher::hash, 100, routees)))
    }
    fun newConsistentHashGroup (hash : (String) -> Int, replicaCount : Int, routees : Array<PID>) : Props {
        return Props().withSpawner(spawner(ConsistentHashGroupRouterConfig(hash, replicaCount, routees)))
    }
    fun newRandomGroup (routees : Array<PID>) : Props {
        return Props().withSpawner(spawner(RandomGroupRouterConfig(routees)))
    }
    fun newRandomGroup (seed : Int, routees : Array<PID>) : Props {
        return Props().withSpawner(spawner(RandomGroupRouterConfig(seed, routees)))
    }
    fun newRoundRobinGroup (routees : Array<PID>) : Props {
        return Props().withSpawner(spawner(RoundRobinGroupRouterConfig(routees)))
    }
    fun newBroadcastPool (props : Props, poolSize : Int) : Props {
        return props.withSpawner(spawner(BroadcastPoolRouterConfig(poolSize)))
    }
    fun newConsistentHashPool (props : Props, poolSize : Int, hash : (String) -> Int, replicaCount : Int) : Props {
        return props.withSpawner(spawner(ConsistentHashPoolRouterConfig(poolSize, hash , replicaCount)))
    }
    fun newRandomPool (props : Props, poolSize : Int, seed : Int) : Props {
        return props.withSpawner(spawner(RandomPoolRouterConfig(poolSize, seed)))
    }
    fun newRoundRobinPool (props : Props, poolSize : Int) : Props {
        return props.withSpawner(spawner(RoundRobinPoolRouterConfig(poolSize)))
    }
    fun spawner (config : IRouterConfig) : (String, Props, PID?) -> PID {
        fun spawnRouterProcess (name : String, props : Props, parent : PID?) : PID {
            val routeeProps : Props = props.withSpawner(null)
            val routerState : RouterState = config.createRouterState()
            val wg : AutoResetEvent = AutoResetEvent(false)
            val routerProps : Props = fromProducer{ -> RouterActor(routeeProps, config, routerState, wg)}.withMailbox(props.mailboxProducer)
            val ctx : Context = Context(routerProps.producer!!, props.supervisorStrategy, props.receiveMiddlewareChain, props.senderMiddlewareChain, parent)
            val mailbox : Mailbox = routerProps.mailboxProducer()
            val dispatcher : Dispatcher = routerProps.dispatcher
            val reff : Process = RouterProcess(routerState, mailbox)
            val (pid, absent) = ProcessRegistry.tryAdd(name, reff)
            if (!absent) {
                throw ProcessNameExistException(name)
            }
            ctx.self = pid
            mailbox.registerHandlers(ctx, dispatcher)
            mailbox.postSystemMessage(Started)
            mailbox.start()
            wg.waitOne()
            return pid
        }
        return ::spawnRouterProcess
    }
}

