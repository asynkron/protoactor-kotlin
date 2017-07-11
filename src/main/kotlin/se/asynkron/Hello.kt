package se.asynkron

import kotlinx.coroutines.experimental.CommonPool
import proto.actor.*

fun main(args: Array<String>) {
    val prop = fromFunc { ctx ->
        //this runs as a suspended func
        val m = ctx.message
        when (m) {
            is Started -> println("started")
            is String -> {
                println("Hello " + m)
                ctx.self.stop()
            }
            is Stopping -> println("Stopping")
            is Stopped -> println("Stopped")
            else -> println("unknown " + m.toString())
        }
    }
    val pid = spawn(prop) //spawn the actor from props
    pid tell "Proto.Actor"
    //prevent app from exiting before async ops complete
    readLine()
}

