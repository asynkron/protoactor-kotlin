package actor.proto.examples.helloworld

import actor.proto.Started
import actor.proto.Stopped
import actor.proto.Stopping
import actor.proto.fromFunc
import actor.proto.send
import actor.proto.spawn
import actor.proto.stop

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
