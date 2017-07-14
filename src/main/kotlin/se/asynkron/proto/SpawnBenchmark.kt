package spawnbenchmark

import proto.actor.*

object Begin
data class Request(var div: Long, var num: Long, var size: Long, val respondTo : PID)

open internal class SpawnActor : Actor {
    companion object Foo{
        fun produce() = SpawnActor()
        val props: Props = fromProducer(SpawnActor.Foo::produce)
    }

    var replies: Long = 0
    lateinit var replyTo: PID
    var sum: Long = 0

    suspend override fun receiveAsync(context: Context) {
        val msg: Any = context.message
        when (msg) {
            is Request -> when {
                msg.size == 1L -> {
                    msg.respondTo.tell(msg.num)
                    context.self.stop()
                }
                else -> {
                    replies = msg.div
                    replyTo = msg.respondTo
                    for (i in 0 until msg.div) {
                        val child: PID = spawn(props)
                        val s = msg.size / msg.div
                        child.tell(Request(
                                msg.div,
                                msg.num + i * s,
                                s,
                                context.self))
                    }
                }
            }
            is Long -> {
                sum += msg
                replies--
                if (replies == 0L) replyTo.tell(sum)
            }
        }
    }
}

fun main(args: Array<String>) {
    var start: Long = 0
    val managerProps = fromFunc {
        val msg = message
        when (msg) {

            is Begin -> {
                val root = spawn(SpawnActor.props)
                start = System.currentTimeMillis()
                root.tell(Request(10, 0, 1000000, self))
            }
            is Long -> {
                val millis = System.currentTimeMillis() - start
                println("Elapsed " + millis)
                println("Result " + msg)
                println("done")
            }
        }
    }
    val managerPid: PID = spawn(managerProps)
    managerPid.tell(Begin)
    readLine()
    managerPid.tell(Begin)
    readLine()
    managerPid.tell(Begin)
    readLine()
}

