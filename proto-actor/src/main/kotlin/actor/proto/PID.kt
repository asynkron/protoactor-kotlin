package actor.proto

import actor.proto.mailbox.SystemMessage
import java.time.Duration

typealias PID = Protos.PID
fun PID(address: String, id: String): PID {
    val p = PID.newBuilder()
    p.address = address
    p.id = id
    return p.build()
}

fun PID.isLocal(): Boolean = address == ProcessRegistry.noHost || address == ProcessRegistry.address
internal fun PID.cachedProcess(): Process? {
    if (cachedProcess_ == null) {
        cachedProcess_ = ProcessRegistry.get(this)
    }
    return cachedProcess_
}

fun PID.sendSystemMessage(sys: SystemMessage) {
    val process: Process = cachedProcess() ?: ProcessRegistry.get(this)
    process.sendSystemMessage(this, sys)
}

fun PID.toShortString(): String {
    return "$address/$id"
}

fun PID.send(message: Any) {
    val process: Process = cachedProcess() ?: ProcessRegistry.get(this)
    process.sendUserMessage(this, message)
}

