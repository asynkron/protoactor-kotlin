package proto.actor

class FunActor(private val receive: suspend (Context) -> Unit) : Actor {
    override suspend fun receiveAsync(context: Context) = receive(context)
}