package actor.proto.examples.remotebenchmark

import actor.proto.Actor
import actor.proto.Context
import actor.proto.PID
import actor.proto.Started
import actor.proto.examples.remotebenchmark.Messages.Pong
import actor.proto.examples.remotebenchmark.Messages.Start
import actor.proto.examples.remotebenchmark.Messages.getDescriptor
import actor.proto.fromProducer
import actor.proto.remote.Remote
import actor.proto.remote.Serialization.registerFileDescriptor
import actor.proto.spawnNamed

private val start: Start = Start.newBuilder().build()
private val pong: Pong = Pong.newBuilder().build()

fun main(args: Array<String>) {
    registerFileDescriptor(getDescriptor())
    Remote.start("127.0.0.1", 12000)

    spawnNamed(fromProducer { EchoActor() }, "remote")
    readLine()
}

class EchoActor : Actor {
    private var _sender: PID? = null

    override suspend fun Context.receive(msg: Any) {
        when (msg) {
            is Started -> println("Started")
            is Messages.StartRemote -> {
                println("Start remote")
                this@EchoActor._sender = msg.sender
                respond(start)
            }
            is Messages.Ping -> {
                this@EchoActor._sender?.let {
                    send(it, pong)
                } ?: println("StartRemote must be received before Ping!")
            }
            else -> {
                println("Unknown $msg")
            }
        }
    }
}


