package actor.proto.router

import actor.proto.PID
import actor.proto.tell
import java.util.*


internal class RandomRouterState(seed: Long?) : RouterState() {
    private val random: Random = if (seed != null) Random(seed) else Random()
    private lateinit var routees: Set<PID>
    private var values: Array<PID> = arrayOf()
    override fun getRoutees(): Set<PID> {
        return routees
    }

    override fun setRoutees(routees: Set<PID>) {
        this.routees = routees
        values = routees.toTypedArray()
    }

    override fun routeMessage(message: Any) {
        val i = random.nextInt(values.count())
        val pid = values[i]
        pid.tell(message)
    }

}

