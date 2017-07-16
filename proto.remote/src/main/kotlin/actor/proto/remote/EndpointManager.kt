package proto.remote

import actor.proto.*
import actor.proto.remote.*

data class Endpoint(val writer : PID,val watcher : PID)


class EndpointManager(config: RemoteConfig) : Actor, SupervisorStrategy {
    companion object {
        private fun spawnWatcher (address : String, context : Context) : PID {
            val watcherProps : Props = fromProducer{ -> EndpointWatcher(address)}
            val watcher : PID = context.spawnChild(watcherProps)
            return watcher
        }
    }
    private val _config : RemoteConfig = config
    private val _connections : Dictionary<String, Endpoint> = Dictionary<String, Endpoint>()
    suspend override fun receiveAsync (context : Context) {
        val msg = context.message
        when (msg) {
            is Started -> println("Started EndpointManager")
            is EndpointTerminatedEvent -> {
                val endpoint : Endpoint = ensureConnected(msg.address, context)
                endpoint.watcher.tell(msg)
            }
            is RemoteTerminate -> {
                val endpoint : Endpoint = ensureConnected(msg.watchee.address, context)
                endpoint.watcher.tell(msg)
            }
            is RemoteWatch -> {
                val endpoint : Endpoint = ensureConnected(msg.watchee.address, context)
                endpoint.watcher.tell(msg)
            }
            is RemoteUnwatch -> {
                val endpoint : Endpoint = ensureConnected(msg.watchee.address, context)
                endpoint.watcher.tell(msg)
            }
            is RemoteDeliver -> {
                val endpoint : Endpoint = ensureConnected(msg.target.address, context)
                endpoint.writer.tell(msg)
            }
            else -> {
            }
        }
    }
    override fun handleFailure (supervisor : Supervisor, child : PID, rs : RestartStatistics, reason: Exception) {
        supervisor.restartChildren(reason, child)
    }
    private fun ensureConnected (address : String, context : Context) : Endpoint {
        val ok : Boolean = _connections.tryGetValue(address, var endpoint)
        if (!ok) {
            val writer : PID = spawnWriter(address, context)
            val watcher : PID = spawnWatcher(address, context)
            endpoint = Endpoint(writer, watcher)
            _connections.add(address, endpoint)
        }
        return endpoint
    }
    private fun spawnWriter (address : String, context : Context) : PID {
        val writerProps : Props = fromProducer{ EndpointWriter(address, _config.channelOptions, _config.callOptions, _config.channelCredentials)}.withMailbox{ -> EndpointWriterMailbox(_config.endpointWriterBatchSize)}
        val writer : PID = context.spawnChild(writerProps)
        return writer
    }
}

