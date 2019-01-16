package actor.proto.examples.hellorouter

import actor.proto.Started
import actor.proto.fromFunc
import actor.proto.router.newRoundRobinPool
import actor.proto.send
import actor.proto.spawn
import actor.proto.toShortString

fun main(args: Array<String>) {
    val prop = fromFunc { msg ->
        when (msg) {
            is Started -> println("Started ${self.toShortString()}")
            is String -> println("Got message ${self.toShortString()} $msg")
        }
    }
    val config = newRoundRobinPool(prop, 5)
    val routerPid = spawn(config)
    repeat(10) {
        send(routerPid, "hello")
    }
    readLine()
}