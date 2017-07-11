package proto.actor

interface IActor {
    suspend fun receiveAsync(context: IContext)
}
