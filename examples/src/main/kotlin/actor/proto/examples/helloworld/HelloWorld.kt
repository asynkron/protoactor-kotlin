package actor.proto.examples.helloworld

import actor.proto.*

fun main(args: Array<String>) {
    val prop = fromFunc { msg ->
        when (msg) {
            is Started -> println("Started")
            is String -> {
                println("Hello $msg")
                stop(self)
            }
            is Stopping -> println("Stopping")
            is Stopped -> println("Stopped")
            else -> println("unknown " + msg.toString())
        }
    }

    val pid = spawn(prop)
    send(pid, "Proto.Actor")
    readLine()
}
