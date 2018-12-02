package actor.proto.examples.remotebenchmark

import actor.proto.*
import actor.proto.examples.remotebenchmark.Messages.Ping
import actor.proto.examples.remotebenchmark.Messages.Pong
import actor.proto.remote.Remote
import actor.proto.remote.Serialization.registerFileDescriptor
import kotlinx.coroutines.runBlocking
import java.lang.System.currentTimeMillis
import java.time.Duration
import java.util.concurrent.CountDownLatch


fun main(args: Array<String>) {

    registerFileDescriptor(Messages.getDescriptor())
    Remote.start("127.0.0.1", 0)
    run()
    readLine()
    run()
    readLine()
    run()
}

private fun run() {
    val messageCount = 1000000
    val wg = CountDownLatch(1)
    val props = fromProducer { LocalActor(0, messageCount, wg) }
    val pid: PID = spawn(props)
    val remote = PID("127.0.0.1:12000", "remote")
    val startRemote = Messages.StartRemote.newBuilder().setSender(pid).build()
    runBlocking {
        requestAwait<Messages.Start>(remote,startRemote, Duration.ofSeconds(2))
    }

    val start = currentTimeMillis()
    println("Starting to send")
    val msg: Ping = Ping.newBuilder().build()
    for (i in 0 until messageCount) {
        send(remote,msg)
    }
    wg.await()
    val elapsed = currentTimeMillis() - start
    println("Elapsed $elapsed")
    val t: Double = messageCount * 2.0 / elapsed * 1000
    println("Throughput $t msg / sec")
}



class LocalActor(count: Int, messageCount: Int, wg: CountDownLatch) : Actor {
    private var _count: Int = count
    private val _messageCount: Int = messageCount
    private val _wg: CountDownLatch = wg

    override suspend fun Context.receive(msg: Any) {
        when (msg) {
            is Pong -> {
                _count++
                if (_count % 50000 == 0) {
                    println(_count)
                }
                if (_count == _messageCount) {
                    _wg.countDown()
                }
            }
            else -> println(msg)
        }
    }
}
