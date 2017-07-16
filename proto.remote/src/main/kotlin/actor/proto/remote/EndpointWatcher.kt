package actor.proto.remote

import actor.proto.*

class EndpointWatcher(address: String) : Actor {
    private val _behavior: Behavior
    private val _watched: HashMap<String, PID> = HashMap()
    private var _address: String? = address
    suspend override fun receiveAsync(context: Context) = _behavior.receiveAsync(context)
    private suspend fun connectedAsync(context: Context) {
        val msg = context.message
        when (msg) {
            is RemoteTerminate -> {
                _watched.remove(msg.watcher.id)
                val t = Terminated(msg.watchee, true)
                msg.watcher.sendSystemMessage(t)
            }
            is EndpointTerminatedEvent -> {
                for ((id, pid) in _watched) {
                    val t = Terminated(pid, true)
                    val watcher: PID = PID(ProcessRegistry.address, id)
                    watcher.sendSystemMessage(t)
                }
                _behavior.become({ terminatedAsync(it) })
            }
            is RemoteUnwatch -> {
                _watched.remove(msg.watcher.id)
                val w: Unwatch = Unwatch(msg.watcher)
                Remote.sendMessage(msg.watchee, w, -1)
            }
            is RemoteWatch -> {
                _watched.put(msg.watcher.id, msg.watchee)
                val w: Watch = Watch(msg.watcher)
                Remote.sendMessage(msg.watchee, w, -1)
            }
        }
    }

    private suspend fun terminatedAsync(context: Context) {
        val msg = context.message
        when (msg) {
            is RemoteWatch -> msg.watcher.sendSystemMessage(Terminated(msg.watchee, true))
            is RemoteUnwatch,
            is EndpointTerminatedEvent,
            is RemoteTerminate -> {
            }
            else -> {
            }
        }
    }

    init {
        _behavior = Behavior({ connectedAsync(it) })
    }
}

