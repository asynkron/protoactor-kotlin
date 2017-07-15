package actor.proto

import actor.proto.mailbox.SystemMessage
import java.time.Duration


public typealias PID = Protos.PID
//typealias PID = Protos.PID
fun PID(address: String, id: String) : Protos.PID {
    val p = Protos.PID.newBuilder()
    p.address = address
    p.id = id
    return p.build()
}

internal fun Protos.PID.cachedProcess(): Process? {
    if (cachedProcess_ == null) {
        cachedProcess_ = ProcessRegistry.get(this)
    }
    return cachedProcess_
}

fun Protos.PID.sendSystemMessage(sys: SystemMessage) {
    val process: Process = cachedProcess() ?: ProcessRegistry.get(this)
    process.sendSystemMessage(this, sys)
}

fun Protos.PID.request(message: Any, sender: Protos.PID) {
    val process = cachedProcess() ?: ProcessRegistry.get(this)
    val messageEnvelope = MessageEnvelope(message, sender, null)
    process.sendUserMessage(this, messageEnvelope)
}

suspend fun <T> Protos.PID.requestAsync(message: Any, timeout: Duration): T {
    return requestAsync(message, FutureProcess(timeout))
}

suspend fun <T> Protos.PID.requestAsync(message: Any): T {
    return requestAsync(message, FutureProcess())
}

suspend private fun <T> Protos.PID.requestAsync(message: Any, future: FutureProcess<T>): T {
    request(message, future.pid)
    return future.deferred().await()
}

fun Protos.PID.toShortString(): String {
    return "$address/$id"
}

fun Protos.PID.stop() {
    val process = cachedProcess() ?: ProcessRegistry.get(this)
    process.stop(this)
}

fun Protos.PID.tell(message: Any) {
    val process: Process = cachedProcess() ?: ProcessRegistry.get(this)
    process.sendUserMessage(this, message)
}

