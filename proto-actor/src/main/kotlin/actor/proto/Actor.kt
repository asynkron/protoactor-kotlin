package actor.proto

interface Actor {
    suspend fun Context.receive(msg: Any)
    suspend fun autoReceive(context: Context) {
        val msg = context.message
        when (msg) {
            is PoisonPill -> stop(context.self)
            else -> return context.receive(msg)
        }
    }
}
