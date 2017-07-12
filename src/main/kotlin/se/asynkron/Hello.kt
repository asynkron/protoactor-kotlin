package se.asynkron

import kotlinx.coroutines.experimental.CommonPool
import proto.actor.*

fun main(args: Array<String>) {
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
    //prevent app from exiting before async ops complete
    readLine()
}

