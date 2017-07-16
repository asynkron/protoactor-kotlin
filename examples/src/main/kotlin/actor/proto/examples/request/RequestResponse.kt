package actor.proto.examples.request

import actor.proto.*
import kotlinx.coroutines.experimental.runBlocking

fun main(args: Array<String>) = runBlocking {
    val prop = fromFunc {
        when (message) {
            is String -> {
                respond("Hello!")
            }
        }
    }

    val pid = spawn(prop)
    val res = pid.requestAwait<String>("Proto.Actor")
    println("Got response " + res)
}