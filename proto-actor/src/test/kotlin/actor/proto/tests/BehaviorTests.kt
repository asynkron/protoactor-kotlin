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
    private suspend fun off(context: Context) {
        when (context.message) {
            is PressSwitch -> {
                context.respond("Turning on")
                _behavior.become { on(it) }
            }
            is Touch -> {
                context.respond("Cold")
            }
        }
    }

    private suspend fun on(context: Context) {
        when (context.message) {
            is PressSwitch -> {
                context.respond("Turning off")
                _behavior.become { off(it) }
            }
            is Touch -> {
                context.respond("Hot!")
            }
        }
    }

    suspend override fun receive(context: Context) {
        when (context.message) {
            is HitWithHammer -> {
                context.respond("Smashed!")
                _smashed = true
                return
            }
            is PressSwitch -> if (_smashed) {
                context.respond("Broken")
                return
            }
            is Touch -> if (_smashed) {
                context.respond("OW!")
                return
            }
        }
        _behavior.receive(context)
    }

    init {
        _behavior.become { off(it) }
    }
}

object PressSwitch
object Touch
object HitWithHammer