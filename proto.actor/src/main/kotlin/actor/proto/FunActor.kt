package actor.proto

class FunActor(private val r: suspend (Context) -> Unit) : Actor {
    override suspend fun receive(context: Context) = r(context)
}