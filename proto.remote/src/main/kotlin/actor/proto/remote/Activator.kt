package actor.proto.remote

import actor.proto.*


class Activator : Actor {
    suspend override fun receiveAsync (context : Context) {
        val msg = context.message
        when (msg) {
            is ActorPidRequest -> {
                val props : Props = Remote.getKnownKind(msg.kind)
                var name : String = msg.name
                if (name.isEmpty()) {
                    name = ProcessRegistry.nextId()
                }
                val pid : PID = spawnNamed(props, name)
                val response : ActorPidResponse = TODO("fix")
                context.respond(response)
            }
            else -> {            }
        }
    }
}

