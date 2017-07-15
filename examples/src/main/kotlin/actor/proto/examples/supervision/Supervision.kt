package actor.proto.examples.supervision


import actor.proto.*

fun main(args: Array<String>) {
    val props = fromProducer { ParentActor() }
            .withChildSupervisorStrategy(OneForOneStrategy(::decide, 1, null))

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

fun decide(pid: PID, reason: Exception): SupervisorDirective {
    val tmp = reason
    when (tmp) {
        is RecoverableException -> return SupervisorDirective.Restart
        is FatalException -> return SupervisorDirective.Stop
        else -> return SupervisorDirective.Escalate
    }
}


open internal class ParentActor : Actor {
    private lateinit var _child: PID
    suspend override fun receiveAsync(context: Context) {
        val tmp = context.message
        when (tmp) {
            is Started -> {
                val props: Props = fromProducer { -> ChildActor() }
                _child = context.spawn(props)
            }
            is Hello -> _child.tell(tmp)
            is Recoverable -> _child.tell(tmp)
            is Fatal -> _child.tell(tmp)
            is Terminated -> {
                val r = tmp
                println("Watched actor was Terminated " + r.who.toShortString())
            }
        }
    }
}

open internal class ChildActor : Actor {
    suspend override fun receiveAsync(context: Context) {
        val tmp = context.message
        when (tmp) {
            is Hello -> {
                val r = tmp
                println("Hello ${r.who}")
            }
            is Recoverable -> throw RecoverableException()
            is Fatal -> throw FatalException()
            is Started -> println("Started, initialize actor here")
            is Stopping -> println("Stopping, actor is about shut down")
            is Stopped -> println("Stopped, actor and it's children are stopped")
            is Restarting -> println("Restarting, actor is about restart")
        }
    }
}

