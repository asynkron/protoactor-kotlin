package proto.router.routers

import proto.actor.PID
import java.util.*

internal class RandomRouterState(seed: Long?) : RouterState() {
    private val _random : Random = if (seed != null) Random(seed) else Random()
    private lateinit var _routees : Set<PID>
    private var _values : Array<PID> = arrayOf()
    override fun getRoutees () : Set<PID> {
        return _routees
    }
    override fun setRoutees (routees : Set<PID>) {
        _routees = routees
        _values = routees.toTypedArray()
    }
    override fun routeMessage (message : Any) {
        val i : Int = _random.nextInt(_values.count())
        val pid : PID = _values[i]
        pid.tell(message)
    }

}

