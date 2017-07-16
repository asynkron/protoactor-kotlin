package actor.proto.remote

import actor.proto.PID

data class EndpointTerminatedEvent(var address: String)
data class RemoteTerminate(val watcher: PID,val  watchee: PID)
data class RemoteWatch(val watcher: PID,val watchee: PID)
data class RemoteUnwatch(val watcher: PID,val watchee: PID)
data class RemoteDeliver(val message: Any,val target: PID, val sender: PID?,val serializerId: Int)
data class JsonMessage(val typeName : String,val json : String)
