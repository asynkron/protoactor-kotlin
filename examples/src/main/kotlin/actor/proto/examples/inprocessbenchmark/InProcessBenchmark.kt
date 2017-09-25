package actor.proto.examples.inprocessbenchmark

import actor.proto.*
import actor.proto.mailbox.DefaultDispatcher
import actor.proto.mailbox.newMpscArrayMailbox
import actor.proto.mailbox.newSpecifiedMailbox
import org.jctools.queues.spec.ConcurrentQueueSpec
import org.jctools.queues.spec.Ordering
import org.jctools.queues.spec.Preference
import java.lang.Runtime.getRuntime
import java.lang.System.nanoTime
import java.util.concurrent.CountDownLatch


fun main(args: Array<String>) {
    repeat(10) {
        run()
        readLine()
    }
}

fun run() {
    val mailboxSpec = ConcurrentQueueSpec(1,1,5000, Ordering.PRODUCER_FIFO , Preference.NONE)
    val messageCount = 1_000_000
    val batchSize = 400
    println("Dispatcher\t\tElapsed\t\tMsg/sec")
    val tps = arrayOf(/*1,2,5,10,20,50,100,150,200,*/300, 400, 500, 600, 700, 800, 900)
    for (t in tps) {
        val d = DefaultDispatcher(throughput = t)
        val clientCount = getRuntime().availableProcessors() * 20

        val echoProps =
                fromProducer { EchoActor() }
                .withDispatcher(d)
                .withMailbox { newSpecifiedMailbox(mailboxSpec) }

        val latch = CountDownLatch(clientCount)
        val clientProps =
                fromProducer { PingActor(latch, messageCount, batchSize) }
                .withDispatcher(d)
                .withMailbox {  newSpecifiedMailbox(mailboxSpec)}

        val pairs = (0 until clientCount)
                .map { Pair(spawn(clientProps), spawn(echoProps)) }
                .toTypedArray()

        val sw = nanoTime()
        for ((ping, pong) in pairs) {
            send(ping, Start(Msg(ping,pong)))
        }
        latch.await()
        val elapsedNanos = (nanoTime() - sw).toDouble()
        val elapsedMillis = (elapsedNanos / 1_000_000).toInt()
        val totalMessages = messageCount * 2 * clientCount
        val x = ((totalMessages.toDouble() / elapsedNanos * 1_000_000_000.0 ).toInt())
        println("$t\t\t\t\t$elapsedMillis\t\t\t$x")
        for ((client, echo) in pairs) {
            stop(client)
            stop(echo)
        }
        Thread.sleep(500)
    }
}

data class Msg(val ping: PID, val pong: PID)
data class Start(val msg : Msg)

class EchoActor : Actor {
    suspend override fun Context.receive(msg: Any) {
        when (msg) {
            is Msg -> send(msg.ping, msg)
        }
    }
}

class PingActor(private val latch: CountDownLatch, private var messageCount: Int, private val batchSize: Int, private var batch: Int = 0) : Actor {
    suspend override fun Context.receive(msg: Any) {
        when (msg) {
            is Start -> sendBatch(msg.msg)
            is Msg -> {
                batch--
                if (batch > 0) return
                if (!sendBatch(msg)) {
                    latch.countDown()
                }
            }
        }
    }

    private fun Context.sendBatch(msg : Msg): Boolean = when (messageCount) {
        0 -> false
        else -> {

            repeat(batchSize) { send(msg.pong, msg) }
            messageCount -= batchSize
            batch = batchSize
            true
        }
    }
}

