package actor.proto.remote

import actor.proto.*
import actor.proto.mailbox.SystemMessage

class RemoteProcess(private val pid: PID) : Process() {
    override fun sendUserMessage(pid: PID, message: Any) = send(message)
    override fun sendSystemMessage(pid: PID, message: SystemMessage) = send(message)
    private fun send(msg: Any) {
        when (msg) {
            is Watch -> Remote.endpointManagerPid.send(RemoteWatch(msg.watcher, pid))
            is Unwatch -> Remote.endpointManagerPid.send(RemoteUnwatch(msg.watcher, pid))
            else -> Remote.sendMessage(pid, msg, -1)
        }
    }
}

