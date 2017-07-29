@file:JvmName("Actors")
@file:JvmMultifileClass
package actor.proto

import actor.proto.mailbox.SystemMessage

@JvmSynthetic fun fromProducer(producer: () -> Actor): Props = Props().withProducer(producer)
@JvmSynthetic fun fromFunc(receive: suspend Context.() -> Unit): Props = fromProducer {
    object : Actor {
        suspend override fun Context.receive(msg: Any) = receive(this)
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

fun spawnNamed(props: Props, name: String): PID {
    return props.spawn(name, null)
}

fun stop(pid : PID) {
    val process = pid.cachedProcess() ?: ProcessRegistry.get(pid)
    process.stop(pid)
}

fun sendSystemMessage(pid : PID, sys: SystemMessage) {
    val process: Process = pid.cachedProcess() ?: ProcessRegistry.get(pid)
    process.sendSystemMessage(pid, sys)
}