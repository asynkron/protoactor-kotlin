package actor.proto

interface Actor {
    suspend fun receive(context: Context)
}
