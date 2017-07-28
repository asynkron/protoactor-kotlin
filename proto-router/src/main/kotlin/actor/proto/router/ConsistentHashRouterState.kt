package actor.proto.router

import actor.proto.MessageEnvelope
import actor.proto.PID
import actor.proto.send
import actor.proto.toShortString

class ConsistentHashRouterRoutees (val hashRing: HashRing,val routeeMap:Map<String, PID>)

internal class ConsistentHashRouterState(private val hash: (String) -> Int, private var replicaCount: Int) : RouterState() {
    private lateinit var routees : ConsistentHashRouterRoutees
    override fun getRoutees(): Set<PID> {
        return routees.routeeMap.values.toSet()
    }

    override fun setRoutees(routees: Set<PID>) {
        var routeeMap = mapOf<String,PID>()
        var nodes = setOf<String>()
        for (pid in routees) {
            val nodeName: String = pid.toShortString()
            nodes += nodeName
            routeeMap += Pair(nodeName, pid)
        }
        val hashRing = HashRing(nodes, hash, replicaCount)

       this.routees  = ConsistentHashRouterRoutees(hashRing,routeeMap)
    }

    override fun routeMessage(message: Any) {

        val msg = when (message) {
            is MessageEnvelope -> message.message
            else -> message
        }

        val r = routees
        when (msg) {
            is Hashable -> {
                val key = msg.hashBy()
                val node = r.hashRing.getNode(key)
                val routee = r.routeeMap[node]!!
                send(routee, message)
            }
            else -> throw Exception("Message is not hashable")
        }
    }
}

