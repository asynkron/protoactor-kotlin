package actor.proto

interface Actor {
    suspend fun Context.receive(message : Any)
    suspend fun autoReceive(context: Context) {
        val message = context.message
        when (context.message) {
            is PoisonPill -> stop(context.self)
            else -> return context.receive(message)
        }
    }
}
