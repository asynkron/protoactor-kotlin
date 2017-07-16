package actor.proto.remote

import actor.proto.*


class Activator : Actor {
    suspend override fun receiveAsync(context: Context) {
        val msg = context.message
        when (msg) {
            is RemoteProtos.ActorPidRequest -> {
                val props: Props = Remote.getKnownKind(msg.kind)
                val name: String = when {
                    msg.name.isEmpty() -> msg.name
                    else -> ProcessRegistry.nextId()
                }
                val pid: PID = spawnNamed(props, name)
                val res = ActorPidResponse(pid)
                context.respond(res)
            }
            else -> {
            }
        }
    }
}

