package actor.proto.router

object MD5Hasher {
    fun hash(hashKey: String): Int {
        return hashKey.hashCode()
    }
}