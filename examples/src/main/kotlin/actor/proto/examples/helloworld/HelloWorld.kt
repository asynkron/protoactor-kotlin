package actor.proto.examples.helloworld

import actor.proto.*

fun main(args: Array<String>) {
    val prop = fromFunc {
        when (message) {
            is Started -> println("started")
            is String -> {
                println("Hello " + message)
                self.stop()
            }
            is Stopping -> println("Stopping")
            is Stopped -> println("Stopped")
            else -> println("unknown " + message.toString())
        }
    }

    val pid = spawn(prop)
    pid.tell("Proto.Actor")
    readLine()
}