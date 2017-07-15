package se.asynkron.proto

import proto.actor.*
import proto.mailbox.ThreadPoolDispatcher
import proto.mailbox.mpscMailbox
import java.lang.System.currentTimeMillis
import java.util.concurrent.CountDownLatch

fun main(args: Array<String>) {
    run()
    readLine()
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
            val tmp = message
            when (tmp) {
                is Msg -> {
                    val msg = tmp
                    msg.sender.tell(msg)
                }
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

        val sw = currentTimeMillis()
        for ((client, echo) in pairs) {
            client.tell(Start(echo))
        }
        latch.await()

        val elapsedMillis = (currentTimeMillis() - sw).toDouble()
        val totalMessages = messageCount * 2 * clientCount
        val x = ((totalMessages.toDouble() / elapsedMillis * 1000.0).toInt())
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
    suspend override fun receiveAsync(context: Context) {
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
                repeat(batchSize) { sender.tell(m) }
                messageCount -= batchSize
                batch = batchSize
                return true
            }
        }
    }
}

