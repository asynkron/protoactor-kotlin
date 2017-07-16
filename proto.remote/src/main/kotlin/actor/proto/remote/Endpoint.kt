package actor.proto.remote

import actor.proto.PID

data class Endpoint(val writer: PID, val watcher: PID)