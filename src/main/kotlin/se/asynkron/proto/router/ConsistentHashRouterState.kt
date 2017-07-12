package proto.router.routers

import proto.actor.MessageEnvelope
import proto.actor.PID
import proto.router.HashRing
import proto.router.IHashable

open internal class ConsistentHashRouterState(hash: (String) -> Int, replicaCount: Int) : RouterState() {
    private val _hash : (String) -> Int = hash
    private var _replicaCount : Int = replicaCount
    private var _hashRing : HashRing? = null
    private var _routeeMap : Map<String, PID>? = null
    override fun getRoutees () : Set<PID> {
        return setOf(*_routeeMap.values.toTypedArray())
    }
    fun setRoutees (routees : MutableSet<PID>) {
        _routeeMap = mapOf<String, PID>()
        val nodes : MutableList<String> = mutableListOf()
        for(pid in routees) {
            val nodeName : String = pid.toShortString()
            nodes.add(nodeName)
            _routeeMap[nodeName] = pid
        }
        _hashRing = HashRing(nodes, _hash, _replicaCount)
    }
    override fun routeMessage (message : Any) {

        val msg = when(message){
            is MessageEnvelope -> message.message
            else -> message
        }

        when (msg){
            is IHashable -> {
                val key: String = msg.hashBy()
                val node: String = _hashRing.getNode(key)
                val routee: PID = _routeeMap[node]
                routee.tell(message)
            }
            else -> throw Exception("Message is not hashable")
        }
    }
}

