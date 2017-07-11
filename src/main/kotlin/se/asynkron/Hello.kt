package se.asynkron

import proto.actor.Actor
import proto.actor.Started

fun main(args: Array<String>) {
    val prop = Actor.fromFunc({ //this runs as a suspended func
        val m = it.message
        when (m) {
            is Started -> println("started")
            is String -> println("string " + m)
            else -> println("unknown " + m.toString())
        }
    })
    val pid = Actor.spawn(prop) //spawn the actor from props
    pid.tell("Roger") //send message to the actor

    //prevent app from exiting before async ops complete
    readLine()
}

