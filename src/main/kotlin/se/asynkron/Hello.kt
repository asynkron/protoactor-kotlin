package se.asynkron

import proto.actor.Started
import proto.actor.fromFunc
import proto.actor.spawn
import proto.actor.tell

fun main(args: Array<String>) {
    val prop = fromFunc { ctx ->
        //this runs as a suspended func
        val m = ctx.message
        when (m) {
            is Started -> println("started")
            is String -> println("string " + m)
            else -> println("unknown " + m.toString())
        }
    }
    val pid = spawn(prop) //spawn the actor from props
    pid tell "hej"
    //prevent app from exiting before async ops complete
    readLine()
}

