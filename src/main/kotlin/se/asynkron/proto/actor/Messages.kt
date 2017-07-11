package proto.actor

import proto.mailbox.SystemMessage

abstract class AutoReceiveMessage {
}

class PoisonPill { //TODO: this is proto
}

class Terminated(val who: PID, val addressWasTerminated: Boolean) : SystemMessage() {
    //PROTO
}

class ReceiveTimeout : SystemMessage() {
    companion object {
        val Instance: ReceiveTimeout = ReceiveTimeout()
    }
}

class Stopped : AutoReceiveMessage() {
    companion object {
        val Instance: Stopped = Stopped()
    }
}

class Started : SystemMessage() {
    companion object {
        val Instance: Started = Started()
    }
}

class Stop : SystemMessage() {
    companion object {
        val Instance: Stop = Stop()
    }
}

class Restarting {
    companion object {
        val Instance: Restarting = Restarting()
    }
}

class Stopping : AutoReceiveMessage() {
    companion object {
        val Instance: Stopping = Stopping()
    }
}

class Failure(val who: PID, val reason: Exception, val restartStatistics: RestartStatistics) : SystemMessage()
class Watch(val watcher: PID) : SystemMessage()
class Unwatch(val watcher: PID) : SystemMessage()
class Restart(val reason: Exception) : SystemMessage()
data class Continuation(val action: suspend () -> Unit, val message: Any) : SystemMessage()
interface INotInfluenceReceiveTimeout

