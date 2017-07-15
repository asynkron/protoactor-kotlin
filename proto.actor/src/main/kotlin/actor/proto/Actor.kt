package actor.proto

interface Actor {
    suspend fun receiveAsync(context: Context)
}