package actor.proto.remote

import actor.proto.*

class EndpointWatcher(address: String) : Actor {
    private val behavior: Behavior = Behavior({ connectedAsync(it) })
    private val watched: HashMap<String, PID> = HashMap()
    private var _address: String = address
    suspend override fun Context.receive(message : Any) = behavior.receive(this)
    private suspend fun connectedAsync(context: Context) {
        val msg = context.message
        when (msg) {
            is RemoteTerminate -> {
                watched.remove(msg.watcher.id)
                sendSystemMessage(msg.watcher,Terminated(msg.watchee, true))
            }
            is EndpointTerminatedEvent -> {
                for ((id, pid) in watched) {
                    val watcher: PID = PID(ProcessRegistry.address, id)
                    sendSystemMessage(watcher,Terminated(pid, true))
                }
                behavior.become({ terminatedAsync(it) })
            }
            is RemoteUnwatch -> {
                watched.remove(msg.watcher.id)
                Remote.sendMessage(msg.watchee, Unwatch(msg.watcher), -1)
            }
            is RemoteWatch -> {
                watched.put(msg.watcher.id, msg.watchee)
                Remote.sendMessage(msg.watchee, Watch(msg.watcher), -1)
            }
        }
    }

    private suspend fun terminatedAsync(context: Context) {
        val msg = context.message
        when (msg) {
            is RemoteWatch -> sendSystemMessage(msg.watcher,Terminated(msg.watchee, true))
            else -> {
            }
        }
    }
}

