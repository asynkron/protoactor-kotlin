package proto

open class FunActor(receive: (IContext) -> Task) : IActor {
    private val _receive : (IContext) -> Task = receive
    override fun receiveAsync (context : IContext) : Task = _receive(context)
}


