package actor.proto

import java.util.concurrent.ConcurrentHashMap

typealias Partition = ConcurrentHashMap<String, Process>

internal class HashedConcurrentDictionary {
    companion object {
        private const val HashSize: Int = 1024
    }

    private val _partitions: Array<Partition> = Array(HashSize, { ConcurrentHashMap<String, Process>(10000) })
    private fun getPartition(key: String): Partition {
        val hash: Int = Math.abs(key.hashCode()) and (HashSize-1)
        val p = _partitions[hash]
        return p
    }

    fun put(key: String, reff: Process): Boolean {
        val p = getPartition(key)
        return p.putIfAbsent(key, reff) != null
    }

    fun tryGetValue(key: String): Process? {
        val p = getPartition(key)
        return p[key]
    }

    fun remove(key: String) {
        val p = getPartition(key)
        p.remove(key)
    }

    fun getOrDefault(key: String, default: Process): Process {
        return tryGetValue(key) ?: default
    }
}