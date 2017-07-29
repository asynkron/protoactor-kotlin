package actor.proto.tests

import actor.proto.*
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert
import org.junit.Test
import java.time.Duration
import kotlin.test.assertEquals

class BehaviorTests {
    fun spawnActorFromFunc(receive: suspend (Context) -> Unit): PID = spawn(fromFunc(receive))
    @Test fun `can change states`() = runBlocking {
        val testActorProps: Props = fromProducer { LightBulb() }
        val actor: PID = spawn(testActorProps)
        Assert.assertEquals("Turning on", requestAwait<String>(actor, PressSwitch, Duration.ofMillis(200)))
        Assert.assertEquals("Hot!", requestAwait<String>(actor, Touch, Duration.ofMillis(200)))
        Assert.assertEquals("Turning off", requestAwait<String>(actor, PressSwitch, Duration.ofMillis(200)))
        Assert.assertEquals("Cold", requestAwait<String>(actor, Touch, Duration.ofMillis(200)))
    }

    @Test fun `can use global behaviour`() = runBlocking {
        val testActorProps: Props = fromProducer { LightBulb() }
        val actor: PID = spawn(testActorProps)
        assertEquals("Turning on", requestAwait(actor, PressSwitch, Duration.ofMillis(200)))
        assertEquals("Smashed!", requestAwait(actor, HitWithHammer, Duration.ofMillis(200)))
        assertEquals("Broken", requestAwait(actor, PressSwitch, Duration.ofMillis(200)))
        assertEquals("OW!", requestAwait(actor, Touch, Duration.ofMillis(200)))
    }

    @Test fun `pop behavior should restore pushed behavior`() = runBlocking {
        val behavior: Behavior = Behavior()
        behavior.become { ctx ->
            if (ctx.message is String) {
                behavior.becomeStacked { ctx2 ->
                    ctx2.respond(42)
                    behavior.unbecomeStacked()
                }

                ctx.respond(ctx.message)
            }
        }

        val timeout = Duration.ofMillis(200)
        val pid: PID = spawnActorFromFunc({ behavior.receive(it) })
        assertEquals("number", requestAwait(pid,"number", timeout))
        assertEquals(42, requestAwait(pid,123, timeout))
        assertEquals("answertolifetheuniverseandeverything", requestAwait(pid,"answertolifetheuniverseandeverything", timeout))
    }
}


class LightBulb : Actor {
    private val _behavior: Behavior = Behavior()
    private var _smashed: Boolean = false
    private suspend fun Context.off() {
        when (message) {
            is PressSwitch -> {
                respond("Turning on")
                _behavior.become { on() }
            }
            is Touch -> {
                respond("Cold")
            }
        }
    }

    private suspend fun Context.on() {
        when (message) {
            is PressSwitch -> {
                respond("Turning off")
                _behavior.become { off() }
            }
            is Touch -> {
                respond("Hot!")
            }
        }
    }

    suspend override fun Context.receive() {
        when (message) {
            is Started -> {
                _behavior.become { off() }
            }
            is HitWithHammer -> {
                respond("Smashed!")
                _smashed = true
                return
            }
            is PressSwitch -> if (_smashed) {
                respond("Broken")
                return
            }
            is Touch -> if (_smashed) {
                respond("OW!")
                return
            }
        }
        _behavior.receive(this)
    }
}

object PressSwitch
object Touch
object HitWithHammer