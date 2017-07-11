package proto.actor

interface Actor {
    suspend fun receiveAsync(context: IContext)
}
