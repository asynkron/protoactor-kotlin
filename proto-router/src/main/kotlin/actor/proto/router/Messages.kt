package actor.proto.router

import actor.proto.PID


sealed class RouterManagementMessage
data class RouterRemoveRoutee(val pid: PID) : RouterManagementMessage()
data class RouterAddRoutee(val pid: PID) : RouterManagementMessage()
data class RouterBroadcastMessage(val message: Any) : RouterManagementMessage()
data class Routees(val pids: Set<PID>)
object RouterGetRoutees : RouterManagementMessage()