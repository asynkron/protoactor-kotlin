package actor.proto.router

import actor.proto.MessageEnvelope
import actor.proto.PID

internal class ConsistentHashRouterState(private val hash: (String) -> Int, private var replicaCount: Int) : RouterState() {
    private lateinit var hashRing: HashRing
    private lateinit var routeeMap: Map<String, PID>
    override fun getRoutees(): Set<PID> {
        return routeeMap.values.toSet()
    }

    override fun setRoutees(routees: Set<PID>) {
        routeeMap = mapOf()
        var nodes: Set<String> = setOf()
        for (pid in routees) {
            val nodeName: String = pid.toShortString()
            nodes += nodeName
            routeeMap += Pair(nodeName, pid)
        }
        hashRing = HashRing(nodes, hash, replicaCount)
    }

    override fun routeMessage(message: Any) {

        val msg = when (message) {
            is MessageEnvelope -> message.message
            else -> message
        }

        when (msg) {
            is IHashable -> {
                val key = msg.hashBy()
                val node = hashRing.getNode(key)
                val routee = routeeMap[node]!!
                routee.tell(message)
            }
            else -> throw Exception("Message is not hashable")
        }
    }
}

