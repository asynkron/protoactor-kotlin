package proto.router

import sun.security.provider.MD5

object MD5Hasher {
    private val HashAlgorithm: MD5 = MD5()
    fun hash(hashKey: String): Int {
     //   val digest: Array<Byte> = HashAlgorithm.computeHash(UTF8.getBytes(hashKey))
        val hash: Int = hashKey.hashCode()// BitConverter.toUInt32(digest, 0)
        return hash
    }
}