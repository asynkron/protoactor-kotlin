package actor.proto.examples.hellorouter

import actor.proto.*
import actor.proto.router.newRoundRobinPool


fun main(args: Array<String>) {
    val prop = fromFunc {
        when (message) {
            is Started -> println("Started ${self.toShortString()}")
            is String -> println("Got message ${self.toShortString()} $message")
        }
    }
    val config = newRoundRobinPool(prop, 5)
    val routerPid = spawn(config)
    repeat(10) {
        send(routerPid, "hello")
    }
    readLine()
}