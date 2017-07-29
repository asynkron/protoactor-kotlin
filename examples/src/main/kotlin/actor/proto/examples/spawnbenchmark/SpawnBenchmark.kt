package actor.proto.examples.spawnbenchmark

import actor.proto.*
import java.util.concurrent.CountDownLatch


fun main(args: Array<String>) {
    repeat(10) {
        runOnce()
    }
    readLine()
}

private fun runOnce() {
    val (cd, managerPid: PID) = spawnManager()
    send(managerPid, Begin)
    cd.await()
    System.gc()
    System.runFinalization()
}

private fun spawnManager(): Pair<CountDownLatch, PID> {
    var start: Long = 0
    val cd = CountDownLatch(1)
    val managerProps = fromFunc {
        val msg = message
        when (msg) {

            is Begin -> {
                val root = spawn(SpawnActor.props)
                start = System.currentTimeMillis()
                send(root,Request(10, 0, 1_000_000, self))
            }
            is Long -> {
                val millis = System.currentTimeMillis() - start
                println("Elapsed " + millis)
                println("Result " + msg)
                cd.countDown()
            }
        }
    }
    val managerPid: PID = spawn(managerProps)
    return Pair(cd, managerPid)
}

object Begin
data class Request(val div: Long, val num: Long, val size: Long, val respondTo: PID)

class SpawnActor : Actor {
    companion object Foo {
        private fun produce() = SpawnActor()
        val props: Props = fromProducer(SpawnActor.Foo::produce)
    }

    private var replies: Long = 0
    private lateinit var replyTo: PID
    private var sum: Long = 0

    suspend override fun Context.receive(msg: Any) {
        when (msg) {
            is Request -> when {
                msg.size == 1L -> {
                     send(msg.respondTo, msg.num)
                    stop(self)
                }
                else -> {
                    replies = msg.div
                    replyTo = msg.respondTo
                    for (i in 0 until msg.div) {
                        val child: PID = spawn(props)
                        val s = msg.size / msg.div
                        send(child,Request(
                                msg.div,
                                msg.num + i * s,
                                s,
                                self))
                    }
                }
            }
            is Long -> {
                sum += msg
                replies--
                if (replies == 0L) send(replyTo,sum)
            }
        }
    }
}



