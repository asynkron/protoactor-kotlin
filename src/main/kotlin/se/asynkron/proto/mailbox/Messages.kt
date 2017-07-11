package proto.mailbox

abstract class SystemMessage

object SuspendMailbox : SystemMessage()
object ResumeMailbox : SystemMessage()
