package actor.proto.remote

import actor.proto.*


class Activator : Actor {
    suspend override fun Context.receive(message: Any) {
        when (message) {
            is RemoteProtos.ActorPidRequest -> {
                val props: Props = Remote.getKnownKind(message.kind)
                val name: String = when {
                    message.name.isEmpty() -> message.name
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

