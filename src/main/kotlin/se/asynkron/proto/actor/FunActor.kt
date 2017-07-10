package proto.actor

class FunActor(val receive: suspend (IContext) -> Task) : IActor {
    override suspend fun receiveAsync (context : IContext)  {
        receive(context)
    }
}