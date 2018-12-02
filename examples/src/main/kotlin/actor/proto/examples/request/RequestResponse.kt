package actor.proto.examples.request



import actor.proto.fromFunc
import actor.proto.requestAwait
import actor.proto.spawn
import kotlinx.coroutines.runBlocking

import java.time.Duration

fun main(args: Array<String>): Unit = runBlocking {
    val prop = fromFunc {
        when (message) {
            is String -> {
                respond("Hello!")
            }
        }
    }

    val pid = spawn(prop)
    val res = requestAwait<String>(pid,"Proto.Actor", Duration.ofMillis(200))
    println("Got response $res")
}
