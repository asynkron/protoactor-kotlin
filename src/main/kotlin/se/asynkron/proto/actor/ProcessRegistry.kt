package proto.actor

import java.util.concurrent.atomic.AtomicInteger

object ProcessRegistry {
    private val NoHost: String = "nonhost"
    private val hostResolvers: MutableList<(PID) -> Process> = mutableListOf()
    private val localActorRefs: HashedConcurrentDictionary = HashedConcurrentDictionary()
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
        return localActorRefs.tryGetValue(pid.id) ?: DeadLetterProcess
    }

    fun tryAdd(id: String, aref: Process): Pair<PID, Boolean> {
        val pid: PID = PID(address, id)
        val ok: Boolean = localActorRefs.tryAdd(pid.id, aref)
        return Pair(pid, ok)
    }

    fun remove(pid: PID) {
        localActorRefs.remove(pid.id)
    }

    fun nextId(): String {
        val counter: Int = sequenceId.incrementAndGet()
        return "$" + counter
    }
}

