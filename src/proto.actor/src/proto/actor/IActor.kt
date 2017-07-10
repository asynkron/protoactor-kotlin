package proto.actor

interface IActor {
    fun receiveAsync (context : IContext) : Task
}

class NullActor : IActor {
    override fun receiveAsync(context: IContext): Task {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}