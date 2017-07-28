package actor.proto.remote

import actor.proto.*

class EndpointManager(private val config: RemoteConfig) : Actor, SupervisorStrategy {
    companion object {
        private fun spawnWatcher(address: String, context: Context): PID {
            val watcherProps: Props = fromProducer { EndpointWatcher(address) }
            val watcher: PID = context.spawnChild(watcherProps)
            return watcher
        }
    }

    private val _connections: HashMap<String, Endpoint> = HashMap()
    suspend override fun receive(context: Context) {
        val msg = context.message
        when (msg) {
            is Started -> println("Started EndpointManager")
            is EndpointTerminatedEvent ->  ensureConnected(msg.address, context).watcher.let { context.send(it,msg) }
            is RemoteTerminate -> ensureConnected(msg.watchee.address, context).watcher.let { context.send(it,msg) }
            is RemoteWatch -> ensureConnected(msg.watchee.address, context).watcher.let { context.send(it,msg) }
            is RemoteUnwatch -> ensureConnected(msg.watchee.address, context).watcher.let { context.send(it,msg) }
            is RemoteDeliver -> ensureConnected(msg.target.address, context).writer.let { context.send(it,msg) }
            else -> {
            }
        }
    }

    override fun handleFailure(supervisor: Supervisor, child: PID, rs: RestartStatistics, reason: Exception) {
        supervisor.restartChildren(reason, child)
    }

    private fun ensureConnected(address: String, context: Context): Endpoint = _connections.getOrPut(address, {
        val writer: PID = spawnWriter(address, context)
        val watcher: PID = spawnWatcher(address, context)
        Endpoint(writer, watcher)
    })

    private fun spawnWriter(address: String, context: Context): PID {
        val writerProps: Props = fromProducer { EndpointWriter(address) }.withMailbox { EndpointWriterMailbox(config.endpointWriterBatchSize) }
        val writer: PID = context.spawnChild(writerProps)
        return writer
    }
}

