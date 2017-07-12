package proto.router.routers

import proto.actor.PID
import java.util.concurrent.atomic.AtomicInteger

open internal class RoundRobinRouterState : RouterState() {
    private var _currentIndex : AtomicInteger = AtomicInteger(0)
    private lateinit var _routees : Set<PID>
    private lateinit var _values : Array<PID>
    override fun getRoutees () : Set<PID> {
        return _routees
    }
    override fun setRoutees (routees : Set<PID>) {
        _routees = routees
        _values = routees.toTypedArray()
    }
    override fun routeMessage (message : Any) {
        val i : Int = _currentIndex.getAndIncrement() % _values.count()
        val pid : PID = _values[i]
        pid.tell(message)
    }
}

