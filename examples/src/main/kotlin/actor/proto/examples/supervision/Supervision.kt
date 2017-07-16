package actor.proto.examples.supervision

import actor.proto.*

fun main(args: Array<String>) {
    val decide = { pid: PID, reason: Exception ->
        println("Handling failure from ${pid.toShortString()} reason:$reason")
        when (reason) {
            is RecoverableException -> SupervisorDirective.Restart
            is FatalException -> SupervisorDirective.Stop
            else -> SupervisorDirective.Escalate
        }
    }

    val props = fromProducer { ParentActor() }.withChildSupervisorStrategy(OneForOneStrategy(decide, 10))

    val actor = spawn(props)
    actor.send(Hello("ProtoActor"))
    actor.send(Recoverable)
    actor.send(Fatal)
    Thread.sleep(2000)
    actor.stop()
    readLine()
}

data class Hello(val who: String)
class RecoverableException : Exception()
class FatalException : Exception()
object Fatal
object Recoverable

class ParentActor : Actor {
    private lateinit var child: PID
    suspend override fun receive(context: Context) {
        val msg = context.message
        when (msg) {
            is Started -> child = context.spawnChild(fromProducer { ChildActor() })
            is Hello -> child.send(msg)
            is Recoverable -> child.send(msg)
            is Fatal -> child.send(msg)
            is Terminated -> println("Watched actor was Terminated ${msg.who.toShortString()}")
        }
    }
}

class ChildActor : Actor {
    suspend override fun receive(context: Context) {
        val msg = context.message
        when (msg) {
            is Hello -> println("Hello ${msg.who}")
            is Recoverable -> throw RecoverableException()
            is Fatal -> throw FatalException()
            is Started -> println("Started, initialize actor here")
            is Stopping -> println("Stopping, actor is about shut down")
            is Stopped -> println("Stopped, actor and it's children are stopped")
            is Restarting -> println("Restarting, actor is about restart")
        }
    }
}