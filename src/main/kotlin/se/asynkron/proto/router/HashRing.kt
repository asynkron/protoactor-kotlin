package proto.router

import sun.security.provider.MD5

object MD5Hasher {
    private val HashAlgorithm: MD5 = MD5()
    fun hash(hashKey: String): Int {
        val digest: Array<Byte> = HashAlgorithm.computeHash(UTF8.getBytes(hashKey))
        val hash: Int = BitConverter.toUInt32(digest, 0)
        return hash
    }
}

data class HashEntry(val hashKey: Int, val node : String)

 class HashRing (nodes: Set<String>,val hash: (String) -> Int,val replicaCount: Int) {
    private val ring: Array<HashEntry>

    fun getNode(key: String): String = ring.first {it.hashKey > hash(key)}.node

    init {
        val res = mutableListOf<HashEntry>()
        for(n in nodes){
            for(i in (0..replicaCount)){
                val hashKey = ""+i+n
                res.add(HashEntry(hash(hashKey),n))
            }
        }
        res.sortBy({ it.hashKey })
        ring = res.toTypedArray()
    }
}

