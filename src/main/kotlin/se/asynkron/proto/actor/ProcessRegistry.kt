package proto.actor

import com.sun.org.apache.xpath.internal.operations.Bool
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

object ProcessRegistry {
    private val NoHost: String = "nonhost"
    private val hostResolvers: MutableList<(PID) -> Process> = mutableListOf()
    private val processLookup: ConcurrentHashMap<String,Process> = ConcurrentHashMap<String,Process>()
    private val sequenceId: AtomicInteger = AtomicInteger(0)
    var address: String = NoHost

    fun registerHostResolver(resolver: (PID) -> Process) {
        hostResolvers.add(resolver)
    }

    fun get(pid: PID): Process {
        if (pid.address != NoHost && pid.address != address) {
            hostResolvers
                    .mapNotNull { it(pid) }
                    .forEach { return it }

            throw Exception("Unknown host")
        }
        return processLookup.getOrDefault(pid.id,DeadLetterProcess)
        //return processLookup.tryGetValue(pid.id) ?: DeadLetterProcess
    }

    fun tryAdd(id: String, process: Process): Pair<PID, Boolean> {
        val pid: PID = PID(address, id)
        pid._cachedProcess = process //we know what pid points to what process here
        val ok: Boolean = processLookup.putIfAbsent(pid.id,process) == null
        return Pair(pid, ok)
    }

    fun remove(pid: PID) {
        processLookup.remove(pid.id)
    }

    fun nextId(): String {
        val counter: Int = sequenceId.incrementAndGet()
        return "$" + counter
    }
}

