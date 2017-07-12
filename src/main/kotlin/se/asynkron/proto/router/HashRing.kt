package proto.router

import sun.security.provider.MD5

object MD5Hasher {
    private val HashAlgorithm : MD5 = MD5()
    fun hash (hashKey : String) : Int {
        val digest : Array<Byte> = HashAlgorithm.computeHash(UTF8.getBytes(hashKey))
        val hash : Int = BitConverter.toUInt32(digest, 0)
        return hash
    }
}


open class HashRing {
    private val _hash : (String) -> Int
    private val _ring : List<Pair<Int, String>>
    constructor(nodes : Iterable<String>, hash : (String) -> Int, replicaCount : Int)  {
        _hash = hash
        _ring = nodes.toList() //TODO fix
    }
    fun getNode (key : String) : String {
        return ( ?: ).item2
    }
}

