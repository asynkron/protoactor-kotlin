package proto.router.routers

import proto.actor.PID
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
        val i: Int = currentIndex.getAndIncrement() % values.count()
        val pid: PID = values[i]
        pid.tell(message)
    }
}

