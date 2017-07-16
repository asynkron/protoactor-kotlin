package actor.proto.remote

import actor.proto.PID
import actor.proto.Process
import actor.proto.Unwatch
import actor.proto.Watch
import actor.proto.mailbox.SystemMessage
import actor.proto.tell

class RemoteProcess(private val pid: PID) : Process() {
    override fun sendUserMessage (pid : PID, message : Any) = send(message)
    override fun sendSystemMessage (pid : PID, message : SystemMessage) = send(message)
    private fun send (msg : Any) {
        when (msg) {
            is Watch -> Remote.endpointManagerPid.tell(RemoteWatch(msg.watcher, pid))
            is Unwatch -> Remote.endpointManagerPid.tell(RemoteUnwatch(msg.watcher, pid))
            else -> Remote.sendMessage(pid, msg, -1)
        }
    }
}

