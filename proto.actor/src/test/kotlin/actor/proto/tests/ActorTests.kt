package actor.proto.tests

import actor.proto.*
import actor.proto.fixture.EmptyReceive
import actor.proto.fixture.TestMailbox
import org.junit.Assert
import java.time.Duration
import java.util.*

fun spawnActorFromFunc(receive: suspend (Context) -> Unit): PID = spawn(fromFunc(receive))
suspend fun requestActorAsync() {
    val pid: PID = spawnActorFromFunc { ctx ->
        when (ctx.message) {
            is String -> ctx.respond("hey")
        }
    }

    val reply: Any = pid.requestAwait<Any>("hello")
    Assert.assertEquals("hey", reply)
}

suspend fun requestActorAsync_should_raise_TimeoutException_when_timeout_is_reached() {
    val pid: PID = spawnActorFromFunc(EmptyReceive)
    pid.requestAwait<Any>("", Duration.ofMillis(10))
}

//Assert.assertEquals("Request didn't receive any Response within the expected time.", timeoutEx.message)
//}
suspend fun requestActorAsync_should_not_raise_TimeoutException_when_result_is_first() {
    val pid: PID = spawnActorFromFunc { ctx ->
        when (ctx.message) {
            is String -> ctx.respond("hey")
        }
    }

    val reply: Any = pid.requestAwait<Any>("hello", Duration.ofMillis(100))
    Assert.assertEquals("hey", reply)
}

fun actorLifeCycle() {
    val messages: Queue<Any> = ArrayDeque<Any>()
    val prop = fromFunc {
        messages.offer(message)
    }.withMailbox { TestMailbox() }
    val pid: PID = spawn(prop)
    pid.send("hello")
    pid.stop()
    Assert.assertEquals(4, messages.count())
    val messageArr: Array<Any> = messages.toTypedArray()
    Assert.assertTrue(messageArr[0] is Started)
    Assert.assertTrue(messageArr[1] is String)
    Assert.assertTrue(messageArr[2] is Stopping)
    Assert.assertTrue(messageArr[3] is Stopped)
}



