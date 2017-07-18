package actor.proto

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
internal typealias ProcessMap = HashedConcurrentDictionary
object ProcessRegistry {
    private val NoHost: String = "nonhost"
    private val hostResolvers: MutableList<(PID) -> Process> = mutableListOf()
    private val processLookup: ProcessMap = ProcessMap()
    private val sequenceId: AtomicInteger = AtomicInteger(0)
    var address: String = NoHost

    fun registerHostResolver(resolver: (PID) -> Process) {
        hostResolvers.add(resolver)
    }

    fun get(id:String):Process = processLookup.getOrDefault(id, DeadLetterProcess)
    fun get(pid: PID): Process {
        if (pid.address != NoHost && pid.address != address) {
            hostResolvers
                    .mapNotNull { it(pid) }
                    .forEach { return it }

            throw Exception("Unknown host")
        }
        return processLookup.getOrDefault(pid.id, DeadLetterProcess)
        //return processLookup.tryGetValue(pid.id) ?: DeadLetterProcess
    }

    fun add(id: String, process: Process): PID {
        val pid = PID(address, id)
        pid.cachedProcess_ = process //we know what pid points to what process here
        processLookup.put(pid.id, process)
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

