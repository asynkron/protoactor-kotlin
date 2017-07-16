package proto.remote

import actor.proto.PID
import actor.proto.ProcessRegistry
import actor.proto.Props
import actor.proto.remote.EndpointTerminatedEvent
import actor.proto.remote.RemoteDeliver
import actor.proto.remote.RemoteProcess

object Remote {
    private var _server : Server? = null
    private val Kinds : Dictionary<String, Props> = Dictionary<String, Props>()
    var endpointManagerPid : PID
    var activatorPid : PID
    fun getKnownKinds () : Array<String> {
        return 
    }
    fun registerKnownKind (kind : String, props : Props) {
        Kinds.add(kind, props)
    }
    fun getKnownKind (kind : String) : Props {
        if (Kinds.tryGetValue(kind, var props)) {
            return props
        }
        throw IllegalArgumentException("No Props found for kind '${kind}'")
    }
    fun start (hostname : String, port : Int) {
        start(hostname, port, RemoteConfig())
    }
    fun start (hostname : String, port : Int, config : RemoteConfig) {
        ProcessRegistry.registerHostResolver{ pid -> RemoteProcess(pid) }
        _server = Server
        _server.start()
        val boundPort : Int = .boundPort
        val boundAddr : String = "${hostname}:${boundPort}"
        val addr : String = "${config.advertisedHostname ?: hostname}:${config.advertisedPort ?: boundPort}"
        ProcessRegistry.instance.address = addr
        spawnEndpointManager(config)
        spawnActivator()
        Logger.logDebug("Starting Proto.Actor server on ${boundAddr} (${addr})")
    }
    private fun spawnActivator () {
        val props : Props = Actor.fromProducer{ -> Activator()}
        activatorPid = Actor.spawnNamed(props, "activator")
    }
    private fun spawnEndpointManager (config : RemoteConfig) {
        val props : Props = Actor.fromProducer{ -> EndpointManager(config)}
        endpointManagerPid = Actor.spawn(props)
        EventStream.Instance.subscribe<EndpointTerminatedEvent>(endpointManagerPid.tell)
    }
    fun activatorForAddress (address : String) : PID {
        return PID(address, "activator")
    }
    suspend fun spawnAsync (address : String, kind : String, timeout : Duration) : PID {
        return spawnNamedAsync(address, "", kind, timeout)
    }
    suspend fun spawnNamedAsync (address : String, name : String, kind : String, timeout : Duration) : PID {
        val activator : PID = activatorForAddress(address)
        val res : ActorPidResponse = activator.requestAsync<ActorPidResponse>(ActorPidRequest, timeout)
        return res.pid
    }
    fun sendMessage (pid : PID, msg : Any, serializerId : Int) {
        var (message, sender, _) = Proto.MessageEnvelope.unwrap(msg)
        val env : RemoteDeliver = RemoteDeliver(message, pid, sender, serializerId)
        endpointManagerPid.tell(env)
    }
}

