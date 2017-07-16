package actor.proto.examples.supervision

import actor.proto.*

fun main(args: Array<String>) {

    val decide = { _: PID, reason: Exception ->
        when (reason) {
            is RecoverableException -> SupervisorDirective.Restart
            is FatalException -> SupervisorDirective.Stop
            else -> SupervisorDirective.Escalate
        }
    }

    val props = fromFunc { ParentActor() }.withChildSupervisorStrategy(OneForOneStrategy(decide, 1))

    val actor = spawn(props)
    actor.tell(Hello("ProtoActor"))
    Thread.sleep(2000)
    actor.tell(Recoverable)
    actor.tell(Fatal)
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
            is Hello -> child.tell(msg)
            is Recoverable -> child.tell(msg)
            is Fatal -> child.tell(msg)
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

