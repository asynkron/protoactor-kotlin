package proto.remote

import actor.proto.*
import actor.proto.MessageEnvelope
import actor.proto.remote.*
import java.time.Duration

object Remote {
    private var _server : Server? = null
    private val Kinds : HashMap<String, Props> = HashMap()
    lateinit var endpointManagerPid : PID
    lateinit var activatorPid : PID
    fun getKnownKinds () : Set<String> = Kinds.keys
    fun registerKnownKind (kind : String, props : Props) {
        Kinds.put(kind, props)
    }
    fun getKnownKind (kind : String) : Props {
        return Kinds.getOrElse(kind){
            throw IllegalArgumentException("No Props found for kind '$kind'")
        }
    }
    fun start (hostname : String, port : Int, config : RemoteConfig = RemoteConfig()) {
        ProcessRegistry.registerHostResolver{ pid -> RemoteProcess(pid) }
        _server = Server
        _server.start()
        val boundPort : Int = _server.boundPort
        val boundAddr : String = "$hostname:$boundPort"
        val addr : String = "${config.advertisedHostname ?: hostname}:${config.advertisedPort ?: boundPort}"
        ProcessRegistry.address = addr
        spawnEndpointManager(config)
        spawnActivator()
        println("Starting Proto.Actor server on $boundAddr ($addr)")
    }
    private fun spawnActivator () {
        val props = fromProducer{ Activator() }
        activatorPid = spawnNamed(props, "activator")
    }
    private fun spawnEndpointManager (config : RemoteConfig) {
        val props = fromProducer{ EndpointManager(config) }
        endpointManagerPid = spawn(props)
        EventStream.subscribe({
            if (it is EndpointTerminatedEvent) {
                endpointManagerPid.tell(it)
            }
        })
    }
    fun activatorForAddress (address : String) : PID {
        return PID(address, "activator")
    }
    suspend fun spawnAsync (address : String, kind : String, timeout : Duration) : PID {
        return spawnNamedAsync(address, "", kind, timeout)
    }
    suspend fun spawnNamedAsync (address : String, name : String, kind : String, timeout : Duration) : PID {
        val activator : PID = activatorForAddress(address)
        val res = activator.requestAsync<ActorPidResponse>(ActorPidRequest(kind,name), timeout)
        return res.pid
    }
    fun sendMessage (pid : PID, msg : Any, serializerId : Int) {
        val (message, sender) = when (msg) {
            is MessageEnvelope -> Pair(msg.message,msg.sender)
            else -> Pair(msg,null)
        }
        val env : RemoteDeliver = RemoteDeliver(message, pid, sender, serializerId)
        endpointManagerPid.tell(env)
    }
}

