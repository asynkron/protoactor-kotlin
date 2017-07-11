package proto.actor

import java.util.concurrent.atomic.AtomicInteger

object ProcessRegistry {
    private val NoHost: String = "nonhost"
    private val _hostResolvers: MutableList<(PID) -> Process> = mutableListOf()
    private val _localActorRefs: HashedConcurrentDictionary = HashedConcurrentDictionary()
    private val _sequenceId: AtomicInteger = AtomicInteger(0)
    var address: String = NoHost

    fun registerHostResolver(resolver: (PID) -> Process) {
        _hostResolvers.add(resolver)
    }

    fun get(pid: PID): Process {
        if (pid.address != NoHost && pid.address != address) {
            _hostResolvers
                    .mapNotNull { it(pid) }
                    .forEach { return it }

            throw Exception("Unknown host")
        }
        return _localActorRefs.tryGetValue(pid.id) ?: DeadLetterProcess.Instance
    }

    fun tryAdd(id: String, aref: Process): Pair<PID, Boolean> {
        val pid: PID = PID(address, id)
        val ok: Boolean = _localActorRefs.tryAdd(pid.id, aref)
        return Pair(pid, ok)
    }

    fun remove(pid: PID) {
        _localActorRefs.remove(pid.id)
    }

    fun nextId(): String {
        val counter: Int = _sequenceId.incrementAndGet()
        return "$" + counter
    }
}

