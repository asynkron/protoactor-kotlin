package actor.proto.fixture

import actor.proto.PID
import actor.proto.mailbox.SystemMessage

open class TestProcess : actor.proto.Process() {
    override fun sendUserMessage (pid : PID, message : Any) {
    }
    override fun sendSystemMessage (pid : PID, message : SystemMessage) {
    }
}

