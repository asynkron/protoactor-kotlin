package actor.proto.router

data class HashEntry(val hashKey: Int, val node: String)

class HashRing(nodes: Set<String>, private val hash: (String) -> Int, private val replicaCount: Int) {
    private val ring: Array<HashEntry>

    init {
        ring = nodes
                .flatMap { n -> (0 until replicaCount).map { i -> HashEntry(hash("$i$n"), n) } }
                .sortedBy { it.hashKey }
                .toTypedArray()

    }

    fun getNode(key: String): String = ring.first { it.hashKey > hash(key) }.node
}

