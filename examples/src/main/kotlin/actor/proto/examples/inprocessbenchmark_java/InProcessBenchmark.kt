package actor.proto.examples.inprocessbenchmark_java



import actor.proto.PID
import actor.proto.java.*
import actor.proto.mailbox.DefaultDispatcher
import actor.proto.mailbox.newMpscUnboundedArrayMailbox
import actor.proto.stop
import java.lang.Runtime.getRuntime
import java.lang.System.nanoTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch


fun main(args: Array<String>) {
    repeat(10) {
        run()
        readLine()
    }
}

fun run() {
    val messageCount = 1_000_000
    val batchSize = 5
    println("Dispatcher\t\tElapsed\t\tMsg/sec")
    val tps = arrayOf(/*1,2,5,10,20,50,100,150,200,*/300, 400, 500, 600, 700, 800, 900)
    for (t in tps) {
        val d = DefaultDispatcher(throughput = t)
        val clientCount = getRuntime().availableProcessors() * 20

        val echoProps =
                fromProducer { EchoActor() }
                        .withDispatcher(d)
                        .withMailbox { newMpscUnboundedArrayMailbox(chunkSize = 4000) }

        val latch = CountDownLatch(clientCount)
        val clientProps =
                fromProducer { PingActor(latch, messageCount, batchSize) }
                        .withDispatcher(d)
                        .withMailbox { newMpscUnboundedArrayMailbox(chunkSize = 4000) }

        val pairs = (0 until clientCount)
                .map { Pair(spawn(clientProps), spawn(echoProps)) }
                .toTypedArray()

        val sw = nanoTime()
        for ((client, echo) in pairs) {
            send(client,Start(echo))
        }
        latch.await()

        val elapsedNanos = (nanoTime() - sw).toDouble()
        val elapsedMillis = (elapsedNanos / 1_000_000).toInt()
        val totalMessages = messageCount * 2 * clientCount
        val x = ((totalMessages.toDouble() / elapsedNanos * 1_000_000_000.0).toInt())
        println("$t\t\t\t\t$elapsedMillis\t\t\t$x")
        for ((client, echo) in pairs) {
            stop(client)
            stop(echo)
        }

        Thread.sleep(500)
    }
}

data class Msg(val sender: PID)
data class Start(val sender: PID)

class EchoActor : Actor {
    override fun receive(context: Context): CompletableFuture<*> {
        val msg = context.message()
        when (msg) {
            is Msg -> send(msg.sender,msg)
        }
        return done()
    }
}

class PingActor(private val latch: CountDownLatch, private var messageCount: Int, private val batchSize: Int, private var batch: Int = 0) : Actor {
    override fun receive(context: Context): CompletableFuture<*> {
        val msg = context.message()
        when (msg) {
            is Start -> sendBatch(context, msg.sender)
            is Msg -> {
                batch--
                if (batch > 0) return done()
                if (!sendBatch(context, msg.sender)) {
                    latch.countDown()
                }
            }
        }
        return done()
    }

    private fun sendBatch(context: Context, sender: PID): Boolean {
        return when (messageCount) {
            0 -> false
            else -> {
                val m = Msg(context.self())
                repeat(batchSize) { send(sender,m) }
                messageCount -= batchSize
                batch = batchSize
                true
            }
        }
    }
}

