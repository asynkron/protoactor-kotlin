package spawnbenchmark

import proto.actor.*

object Begin
data class Request(var div: Long, var num: Long, var size: Long)

open internal class SpawnActor : Actor {
    companion object {
        val props: Props = fromProducer({ SpawnActor() })
    }

    var replies: Long = 0
    var replyTo: PID? = null
    var sum: Long = 0

    suspend override fun receiveAsync(context: IContext) {
        val msg: Any = context.message
        when (msg) {
            is Request -> when {
                msg.size == 1L -> {
                    context.respond(msg.num)
                    context.self.stop()
                }
                else -> {
                    replies = msg.div
                    replyTo = context.sender
                    for (i in 0..msg.div) {
                        val child: PID = spawn(props)
                        child.request(Request(msg.div, msg.num + i * (msg.size / msg.div), msg.size / msg.div), context.self)
                    }
                }
            }
            is Long -> {
                sum += msg
                replies--
                if (replies == 0L) replyTo!!.tell(sum)
            }
        }
    }
}



fun main(args: Array<String>) {
    var start: Long = 0
    val managerProps = fromFunc {
        when (message) {
            is Begin -> {
                val root = spawn(SpawnActor.props)
                start = System.currentTimeMillis()
                root.request(Request(10, 0, 1000000), self)
            }
            is Long -> {
                val millis = System.currentTimeMillis() - start
                println(millis)
                println("done")
            }
        }
    }
    val managerPid: PID = spawn(managerProps)
    managerPid.tell(Begin)
    readLine()
}

