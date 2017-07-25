package actor.proto

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

internal typealias ProcessMap = ConcurrentHashMap<String,Process>
object ProcessRegistry {
    val noHost: String = "nonhost"
    private val hostResolvers: MutableList<(PID) -> Process> = mutableListOf()
    private val processLookup: ProcessMap = ProcessMap()
    private val sequenceId: AtomicInteger = AtomicInteger(0)
    var address: String = noHost

    fun registerHostResolver(resolver: (PID) -> Process) {
        hostResolvers.add(resolver)
    }

    fun get(localId: String): Process = processLookup.getOrDefault(localId, DeadLetterProcess)
    fun get(pid: PID): Process = when {
        pid.isLocal() -> processLookup.getOrDefault(pid.id, DeadLetterProcess)
        else -> resolveRemoteProcess(pid)
    }

    private fun resolveRemoteProcess(pid: PID) : Process {
        hostResolvers
                .mapNotNull { it(pid) }
                .forEach { return it }

        throw Exception("Unknown host")
    }

    fun put(id: String, process: Process): PID {
        val pid = PID(address, id)
        pid.cachedProcess_ = process //we know what pid points to what process here
        if (processLookup.put(pid.id, process) != null) {
            throw ProcessNameExistException(id)
        }
        return pid
    }

    fun remove(pid: PID) {
        processLookup.remove(pid.id)
    }

    fun nextId(): String {
        val counter: Int = sequenceId.incrementAndGet()
        return "$" + counter
    }
}

