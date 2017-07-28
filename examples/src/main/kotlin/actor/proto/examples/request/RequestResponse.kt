package actor.proto.examples.request

import actor.proto.fromFunc
import actor.proto.requestAwait
import actor.proto.spawn
import kotlinx.coroutines.experimental.runBlocking
import java.time.Duration

fun main(args: Array<String>) = runBlocking {
    val prop = fromFunc {
        when (message) {
            is String -> {
                respond("Hello!")
            }
        }
    }

    val pid = spawn(prop)
    val res = pid.requestAwait<String>("Proto.Actor", Duration.ofMillis(200))
    println("Got response " + res)
}