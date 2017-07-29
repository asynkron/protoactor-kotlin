package actor.proto.examples.hellorouter

import actor.proto.*
import actor.proto.router.newRoundRobinPool

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