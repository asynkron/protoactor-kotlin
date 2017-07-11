package proto.actor

class FunActor(val receive: suspend (IContext) -> Unit) : Actor {
    override suspend fun receiveAsync(context: IContext) = receive(context)
}