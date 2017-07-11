package proto.actor

class FunActor(val receive: suspend (IContext) -> Unit) : IActor {
    override suspend fun receiveAsync(context: IContext) {
        receive(context)
    }
}