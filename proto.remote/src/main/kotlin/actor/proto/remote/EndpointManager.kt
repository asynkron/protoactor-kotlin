package actor.proto.remote

import actor.proto.*
import proto.remote.EndpointWatcher
import proto.remote.EndpointWriter
import proto.remote.RemoteConfig
import kotlin.collections.HashMap

data class Endpoint(val writer : PID,val watcher : PID)

class EndpointManager(config: RemoteConfig) : Actor, SupervisorStrategy {
    companion object {
        private fun spawnWatcher (address : String, context : Context) : PID {
            val watcherProps : Props = fromProducer{ EndpointWatcher(address) }
            val watcher : PID = context.spawnChild(watcherProps)
            return watcher
        }
    }
    private val _config : RemoteConfig = config
    private val _connections : HashMap<String, Endpoint> = HashMap()
    suspend override fun receiveAsync (context : Context) {
        val msg = context.message
        when (msg) {
            is Started -> println("Started EndpointManager")
            is EndpointTerminatedEvent -> ensureConnected(msg.address, context).watcher.tell(msg)
            is RemoteTerminate -> ensureConnected(msg.watchee.address, context).watcher.tell(msg)
            is RemoteWatch -> ensureConnected(msg.watchee.address, context).watcher.tell(msg)
            is RemoteUnwatch -> ensureConnected(msg.watchee.address, context).watcher.tell(msg)
            is RemoteDeliver -> ensureConnected(msg.target.address, context).writer.tell(msg)
            else -> {
            }
        }
    }
    override fun handleFailure (supervisor : Supervisor, child : PID, rs : RestartStatistics, reason: Exception) {
        supervisor.restartChildren(reason, child)
    }
    private fun ensureConnected (address : String, context : Context) : Endpoint = _connections.getOrPut(address,{
        val writer : PID = spawnWriter(address, context)
        val watcher : PID = spawnWatcher(address, context)
        Endpoint(writer, watcher)
    })
    private fun spawnWriter (address : String, context : Context) : PID {
        val writerProps : Props =
                fromProducer{ EndpointWriter(address, _config.channelOptions, _config.callOptions, _config.channelCredentials) }
                .withMailbox{ -> EndpointWriterMailbox(_config.endpointWriterBatchSize) }
        val writer : PID = context.spawnChild(writerProps)
        return writer
    }
}

