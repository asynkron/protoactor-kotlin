package actor.proto.router

import actor.proto.PID
import actor.proto.send
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
        val v = values
        val i = random.nextInt(v.count())
        val pid = v[i]
        send(pid,message)
    }

}

