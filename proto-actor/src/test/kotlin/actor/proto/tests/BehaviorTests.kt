package actor.proto.tests

import actor.proto.*
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert
import org.junit.Test
import java.time.Duration
import kotlin.test.assertEquals

class BehaviorTests {
    private val timeout = Duration.ofMillis(200)

    @Test fun `can change states`() = runBlocking {
        val testActorProps: Props = fromProducer { LightBulb() }
        val actor: PID = spawn(testActorProps)
        Assert.assertEquals("Turning on", requestAwait<String>(actor, PressSwitch, timeout))
        Assert.assertEquals("Hot!", requestAwait<String>(actor, Touch, timeout))
        Assert.assertEquals("Turning off", requestAwait<String>(actor, PressSwitch, timeout))
        Assert.assertEquals("Cold", requestAwait<String>(actor, Touch, timeout))
    }

    @Test fun `can use global behaviour`() = runBlocking {
        val testActorProps: Props = fromProducer { LightBulb() }
        val actor: PID = spawn(testActorProps)
        assertEquals("Turning on", requestAwait(actor, PressSwitch, timeout))
        assertEquals("Smashed!", requestAwait(actor, HitWithHammer, timeout))
        assertEquals("Broken", requestAwait(actor, PressSwitch, timeout))
        assertEquals("OW!", requestAwait(actor, Touch, timeout))
    }

    @Test fun `pop behavior should restore pushed behavior`() = runBlocking {
        val behavior: Behavior = Behavior()
        behavior.become { msg ->
            if (msg is String) {
                behavior.becomeStacked {
                    respond(42)
                    behavior.unbecomeStacked()
                }

                respond(msg)
            }
        }

        val timeout = timeout
        val pid: PID = spawn(fromFunc({ msg -> behavior.receive(this,msg)}))
        assertEquals("number", requestAwait(pid,"number", timeout))
        assertEquals(42, requestAwait(pid,123, timeout))
        assertEquals("answertolifetheuniverseandeverything", requestAwait(pid,"answertolifetheuniverseandeverything", timeout))
    }
}


class LightBulb : Actor {
    private val behavior: Behavior = Behavior()
    private var smashed: Boolean = false
    private suspend fun Context.off() {
        when (message) {
            is PressSwitch -> {
                respond("Turning on")
                behavior.become { on() }
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
                behavior.become { off() }
            }
            is Touch -> {
                respond("Hot!")
            }
        }
    }

    suspend override fun Context.receive(msg: Any) {
        when (msg) {
            is HitWithHammer -> {
                respond("Smashed!")
                smashed = true
                return
            }
            is PressSwitch -> if (smashed) {
                respond("Broken")
                return
            }
            is Touch -> if (smashed) {
                respond("OW!")
                return
            }
        }
        behavior.receive(this,msg)
    }

    init{
        behavior.become { off() }
    }
}

object PressSwitch
object Touch
object HitWithHammer