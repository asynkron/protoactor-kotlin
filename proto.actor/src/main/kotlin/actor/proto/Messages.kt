package actor.proto

import actor.proto.mailbox.SystemMessage

interface AutoReceiveMessage
typealias PoisonPill = Protos.PoisonPill
typealias Terminated = Protos.Terminated

fun Terminated(who : Protos.PID , addressTerminated : Boolean) : Terminated {
    val t = Terminated.newBuilder()
    t.who = who
    t.addressTerminated = addressTerminated
    return t.build()
}

object ReceiveTimeout : SystemMessage
object Stopped : AutoReceiveMessage
object Started : SystemMessage
object Stop : SystemMessage
object Restarting
object Stopping : AutoReceiveMessage

data class Failure(val who: Protos.PID, val reason: Exception, val restartStatistics: RestartStatistics) : SystemMessage
data class Watch(val watcher: Protos.PID) : SystemMessage
data class Unwatch(val watcher: Protos.PID) : SystemMessage
data class Restart(val reason: Exception) : SystemMessage
data class Continuation(val action: suspend () -> Unit, val message: Any) : SystemMessage
interface NotInfluenceReceiveTimeout

