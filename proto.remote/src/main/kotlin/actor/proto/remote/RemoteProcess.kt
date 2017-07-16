package actor.proto.remote

import actor.proto.PID
import actor.proto.Process
import actor.proto.Unwatch
import actor.proto.Watch
import actor.proto.mailbox.SystemMessage
import actor.proto.tell

class RemoteProcess(pid: PID) : Process() {
    private val _pid : PID = pid
    override fun sendUserMessage (pid : PID, message : Any) = send(message)
    override fun sendSystemMessage (pid : PID, message : SystemMessage) = send(message)
    private fun send (msg : Any) {
        when (msg) {
            is Watch -> Remote.endpointManagerPid.tell(RemoteWatch(msg.watcher, _pid))
            is Unwatch -> Remote.endpointManagerPid.tell(RemoteUnwatch(msg.watcher, _pid))
            else -> Remote.sendMessage(_pid, msg, -1)
        }
    }
}

