package actor.proto.router

import actor.proto.PID
import actor.proto.tell
import java.util.concurrent.atomic.AtomicInteger

internal class RoundRobinRouterState : RouterState() {
    private var currentIndex: AtomicInteger = AtomicInteger(0)
    private lateinit var routees: Set<PID>
    private lateinit var values: Array<PID>
    override fun getRoutees(): Set<PID> {
        return routees
    }

    override fun setRoutees(routees: Set<PID>) {
        this.routees = routees
        values = routees.toTypedArray()
    }

    override fun routeMessage(message: Any) {
        val i = currentIndex.getAndIncrement() % values.count()
        val pid = values[i]
        pid.tell(message)
    }
}

