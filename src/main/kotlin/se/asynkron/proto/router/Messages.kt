package se.asynkron.proto.router

import proto.actor.PID

abstract class RouterManagementMessage
data class RouterRemoveRoutee(val pid: PID) : RouterManagementMessage()
data class RouterAddRoutee(val pid: PID) : RouterManagementMessage()
data class RouterBroadcastMessage(val message: Any) : RouterManagementMessage()
data class Routees(val pids: Set<PID>)
object RouterGetRoutees : RouterManagementMessage()