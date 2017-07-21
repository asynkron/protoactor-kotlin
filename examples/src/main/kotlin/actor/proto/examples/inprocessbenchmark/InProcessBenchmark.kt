package actor.proto.examples.inprocessbenchmark


import actor.proto.*
import actor.proto.mailbox.ThreadPoolDispatcher
import actor.proto.mailbox.mpscMailbox
import java.lang.System.currentTimeMillis
import java.lang.System.nanoTime
import java.util.concurrent.CountDownLatch

fun main(args: Array<String>) {
    repeat(10) {
        run()
        readLine()
    }
}

fun run() {
    val messageCount = 1_000_000
    val batchSize = 500
    println("Dispatcher\t\tElapsed\t\tMsg/sec")
    val tps = arrayOf(300, 400, 500, 600, 700, 800, 900)
    for (t in tps) {
        val d = ThreadPoolDispatcher(throughput = t)
        val clientCount = Runtime.getRuntime().availableProcessors() * 2

        val echoProps = fromFunc {
            val msg = message
            when (msg) {
                is Msg -> msg.sender.send(msg)
            }
        }
                .withDispatcher(d)
                .withMailbox { mpscMailbox(capacity = 20000) }

        val latch = CountDownLatch(clientCount)
        val clientProps = fromProducer { PingActor(latch, messageCount, batchSize) }
                .withDispatcher(d)
                .withMailbox { mpscMailbox(capacity = 20000) }

        val pairs = (0 until clientCount)
                .map { Pair(spawn(clientProps), spawn(echoProps)) }
                .toTypedArray()

        val sw = nanoTime()
        for ((client, echo) in pairs) {
            client.send(Start(echo))
        }
        latch.await()

        val elapsedNanos = (nanoTime() - sw).toDouble()
        val elapsedMillis = (elapsedNanos / 1_000_000).toInt()
        val totalMessages = messageCount * 2 * clientCount
        val x = ((totalMessages.toDouble() / elapsedNanos * 1_000_000_000.0 ).toInt())
        println("$t\t\t\t\t$elapsedMillis\t\t\t$x")
        for ((client, echo) in pairs) {
            client.stop()
            echo.stop()
        }

        Thread.sleep(500)
    }
}

data class Msg(val sender: PID)
data class Start(val sender: PID)

class PingActor(private val latch: CountDownLatch, private var messageCount: Int, private val batchSize: Int, private var batch: Int = 0) : Actor {
    suspend override fun receive(context: Context) {
        val msg = context.message
        when (msg) {
            is Start -> sendBatch(context, msg.sender)
            is Msg -> {
                batch--
                if (batch > 0) return
                if (!sendBatch(context, msg.sender)) {
                    latch.countDown()
                }
            }
        }
    }

    private fun sendBatch(context: Context, sender: PID): Boolean {
        when (messageCount) {
            0 -> return false
            else -> {
                val m = Msg(context.self)
                repeat(batchSize) { sender.send(m) }
                messageCount -= batchSize
                batch = batchSize
                return true
            }
        }
    }
}

