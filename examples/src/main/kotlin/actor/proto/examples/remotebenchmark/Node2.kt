package actor.proto.examples.remotebenchmark

import actor.proto.*
import actor.proto.examples.remotebenchmark.Messages.*
import actor.proto.remote.Remote.start
import actor.proto.remote.Serialization.registerFileDescriptor

val start = Start.newBuilder().build()!!
val pong = Pong.newBuilder().build()!!

fun main(args: Array<String>) {
    registerFileDescriptor(getDescriptor())
    start("127.0.0.1", 12000)
    spawnNamed(fromProducer { EchoActor() }, "remote")
    readLine()
}

class EchoActor : Actor {
    private lateinit var _sender: PID
    suspend override fun Context.receive() {
        val msg = message
        when (msg) {
            is Messages.StartRemote -> {
                println("Starting")
                _sender = msg.sender
                respond(start)
            }
            is Messages.Ping -> send(_sender,pong)
            else -> {
            }
        }
    }
}


