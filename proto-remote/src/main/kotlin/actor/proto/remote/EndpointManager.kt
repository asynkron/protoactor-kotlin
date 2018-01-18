package actor.proto.remote

import actor.proto.*

class EndpointManager(private val config: RemoteConfig) : Actor, SupervisorStrategy {


    private val _connections: HashMap<String, Endpoint> = HashMap()
    suspend override fun Context.receive(msg: Any) {
        when (msg) {
            is Started -> println("Started EndpointManager")
            is EndpointTerminatedEvent ->  ensureConnected(msg.address).watcher.let { send(it,msg) }
            is RemoteTerminate -> ensureConnected(msg.watchee.address).watcher.let {send(it,msg) }
            is RemoteWatch -> ensureConnected(msg.watchee.address).watcher.let { send(it,msg) }
            is RemoteUnwatch -> ensureConnected(msg.watchee.address).watcher.let { send(it,msg) }
            is RemoteDeliver -> ensureConnected(msg.target.address).writer.let { send(it,msg) }
            else -> {
            }
        }
    }

    override fun handleFailure(supervisor: Supervisor, child: PID, rs: RestartStatistics, reason: Exception) {
        supervisor.restartChildren(reason, child)
    }

    private fun Context.ensureConnected(address: String): Endpoint = _connections.getOrPut(address, {
        val writer: PID = spawnWriter(address)
        val watcher: PID = spawnWatcher(address)
        Endpoint(writer, watcher)
    })

    private fun Context.spawnWriter(address: String): PID {
        val writerProps: Props = fromProducer { EndpointWriter(address, config) }.withMailbox { EndpointWriterMailbox(config.endpointWriterBatchSize) }
        val writer: PID = spawnChild(writerProps)
        return writer
    }

    private fun Context.spawnWatcher(address: String): PID {
        val watcherProps: Props = fromProducer { EndpointWatcher(address) }
        val watcher: PID = spawnChild(watcherProps)
        return watcher
    }
}

