package se.asynkron

import proto.actor.Actor

fun main(args: Array<String>) {
    val prop = Actor.fromFunc({
        val m = it.message
        when (m) {
            is String -> print("Hello " + m)
        }
    })
    val pid = Actor.spawn(prop)
    pid.tell("Roger")
    readLine()
}

