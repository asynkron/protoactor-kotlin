package proto.actor

class FunActor(val receive: suspend (Context) -> Unit) : Actor {
    override suspend fun receiveAsync(context: Context) = receive(context)
}