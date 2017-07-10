package proto.mailbox

abstract class SystemMessage

class SuspendMailbox : SystemMessage() {
    companion object {
        val Instance: SuspendMailbox = SuspendMailbox()
    }
}


class ResumeMailbox : SystemMessage() {
    companion object {
        val Instance: ResumeMailbox = ResumeMailbox()
    }
}

