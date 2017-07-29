package actor.proto

interface Actor {
    suspend fun Context.receive()
    suspend fun autoReceive(context: Context) {
        when (context.message) {
            is PoisonPill -> stop(context.self)
            else -> return context.receive()
        }
    }
}
