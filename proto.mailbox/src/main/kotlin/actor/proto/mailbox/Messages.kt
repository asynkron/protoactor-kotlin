package actor.proto.mailbox

interface SystemMessage

object SuspendMailbox : SystemMessage
object ResumeMailbox : SystemMessage
