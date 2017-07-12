package proto.actor

import proto.mailbox.SystemMessage

abstract class AutoReceiveMessage

class PoisonPill { //TODO: this is proto
}

class Terminated(val who: PID, val addressWasTerminated: Boolean) : SystemMessage() {
    //PROTO
}

object ReceiveTimeout : SystemMessage()
object Stopped : AutoReceiveMessage()
object Started : SystemMessage()
object Stop : SystemMessage()
object Restarting
object Stopping : AutoReceiveMessage()

data class Failure(val who: PID, val reason: Exception, val restartStatistics: RestartStatistics) : SystemMessage()
data class Watch(val watcher: PID) : SystemMessage()
data class Unwatch(val watcher: PID) : SystemMessage()
data class Restart(val reason: Exception) : SystemMessage()
data class Continuation(val action: suspend () -> Unit, val message: Any) : SystemMessage()
interface INotInfluenceReceiveTimeout

