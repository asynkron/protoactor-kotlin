package actor.proto

interface Actor {
    suspend fun Context.receive()
    suspend fun receiveInner(context: Context) {
        return context.receive()
    }
}
