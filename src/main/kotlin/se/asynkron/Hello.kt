package se.asynkron

import kotlinx.coroutines.experimental.CommonPool
import proto.actor.*
import proto.router.newRoundRobinPool


fun main(args: Array<String>) {
    helloWorld()
    routers()
}

fun helloWorld(){
    val prop = fromFunc {
        //this runs as a suspended func
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

    val pid = spawn(prop) //spawn the actor from props
    pid tell "Proto.Actor"
    readLine()
}

fun routers(){
    val prop = fromFunc {
        //this runs as a suspended func
        when (message) {
            is Started -> println("Started " + self.toShortString())
            is String -> println("Got message " + self.toShortString() + " " + message)
        }
    }
    val config = newRoundRobinPool(prop,5)
    val routerPid = spawn(config)
    repeat(10) {
        routerPid.tell("hello")
    }
    readLine()
}

