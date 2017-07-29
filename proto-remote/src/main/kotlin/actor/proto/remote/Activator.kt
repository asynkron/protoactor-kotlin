package actor.proto.remote

import actor.proto.*


class Activator : Actor {
    suspend override fun Context.receive() {
        val msg = message
        when (msg) {
            is RemoteProtos.ActorPidRequest -> {
                val props: Props = Remote.getKnownKind(msg.kind)
                val name: String = when {
                    msg.name.isEmpty() -> msg.name
                    else -> ProcessRegistry.nextId()
                }
                val pid: PID = spawnNamed(props, name)
                val res = ActorPidResponse(pid)
                respond(res)
            }
            else -> {
            }
        }
    }
}

