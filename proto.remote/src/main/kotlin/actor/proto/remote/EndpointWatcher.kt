package proto.remote

import actor.proto.remote.EndpointTerminatedEvent
import actor.proto.remote.RemoteTerminate
import actor.proto.remote.RemoteUnwatch
import actor.proto.remote.RemoteWatch

open class EndpointWatcher : Actor {
    private val _behavior : Behavior
    private val _watched : Dictionary<String, PID> = Dictionary<String, PID>()
    private var _address : String? = null
    constructor(address : String)  {
        _address = address
        _behavior = Behavior(connectedAsync)
    }
    suspend override fun receiveAsync (context : Context) {
        return _behavior.receiveAsync(context)
    }
    suspend fun connectedAsync (context : Context) {
        val tmp = context.message
        when (tmp) {
            is RemoteTerminate -> {
                val msg = tmp
                _watched.remove(msg.watcher.id)
                val t : Terminated = Terminated
                msg.watcher.sendSystemMessage(t)
            }
            is EndpointTerminatedEvent -> {
var (id, pid)_watched {
                    val t : Terminated = Terminated
                    val watcher : PID = PID(ProcessRegistry.instance.address, id)
                    watcher.sendSystemMessage(t)
                }
                _behavior.become(terminatedAsync)
            }
            is RemoteUnwatch -> {
                val msg = tmp
                _watched[msg.watcher.id] = null
                val w : Unwatch = Unwatch(msg.watcher)
                Remote.sendMessage(msg.watchee, w, -1)
            }
            is RemoteWatch -> {
                val msg = tmp
                _watched[msg.watcher.id] = msg.watchee
                val w : Watch = Watch(msg.watcher)
                Remote.sendMessage(msg.watchee, w, -1)
            }
        }
        return Actor.Done
    }
    suspend fun terminatedAsync (context : Context) {
        val tmp = context.message
        when (tmp) {
            is RemoteWatch -> {
                val msg = tmp
                msg.watcher.sendSystemMessage(Terminated)
            }
            is RemoteUnwatch, EndpointTerminatedEvent, RemoteTerminate -> {
            }
            else -> {
            }
        }
        return Actor.Done
    }
}

