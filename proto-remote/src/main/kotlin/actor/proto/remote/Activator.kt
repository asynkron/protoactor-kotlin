package actor.proto.remote

import actor.proto.Actor
import actor.proto.Context
import actor.proto.PID
import actor.proto.ProcessRegistry
import actor.proto.Props
import actor.proto.spawnNamed


class Activator : Actor {
    suspend override fun Context.receive(msg: Any) {
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

