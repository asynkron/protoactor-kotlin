package proto.router

data class HashEntry(val hashKey: Int, val node : String)

 class HashRing (nodes: Set<String>,val hash: (String) -> Int,val replicaCount: Int) {
     private val ring: Array<HashEntry>

     init {
         ring = nodes
                 .flatMap { n -> (0..replicaCount).map { i -> HashEntry(hash("$i$n"), n) } }
                 .sortedBy { it.hashKey }
                 .toTypedArray()

     }
     fun getNode(key: String): String = ring.first { it.hashKey > hash(key) }.node
 }

