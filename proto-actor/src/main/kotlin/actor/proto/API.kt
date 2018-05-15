@file:JvmName("Actors")
@file:JvmMultifileClass

package actor.proto

import actor.proto.mailbox.SystemMessage

@JvmSynthetic
fun fromProducer(producer: () -> Actor): Props = Props().withProducer(producer)

@JvmSynthetic
fun fromFunc(receive: suspend Context.(msg: Any) -> Unit): Props = fromProducer {
    object : Actor {
        override suspend fun Context.receive(msg: Any) = receive(this, msg)
    }
}

fun spawn(props: Props): PID {
    val name = ProcessRegistry.nextId()
    return spawnNamed(props, name)
}

fun spawnPrefix(props: Props, prefix: String): PID {
    val name = prefix + ProcessRegistry.nextId()
    return spawnNamed(props, name)
}

fun spawnNamed(props: Props, name: String): PID = props.spawn(name, null)

fun stop(pid: PID) {
    val process = pid.cachedProcess() ?: ProcessRegistry.get(pid)
    process.stop(pid)
}

fun sendSystemMessage(pid: PID, sys: SystemMessage) {
    val process: Process = pid.cachedProcess() ?: ProcessRegistry.get(pid)
    process.sendSystemMessage(pid, sys)
}
