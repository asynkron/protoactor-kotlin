package actor.proto

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

fun PID.toShortString(): String {
    return "$address/$id"
}
