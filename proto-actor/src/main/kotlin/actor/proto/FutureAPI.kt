@file:JvmName("Actors")
@file:JvmMultifileClass
package actor.proto

import kotlinx.coroutines.experimental.future.await
import java.util.concurrent.CompletableFuture


private val done: CompletableFuture<Void> = CompletableFuture.completedFuture(null)
fun done(): CompletableFuture<Void> = done

fun fromFutureProducer(producer: () -> FutureActor): Props {
    return fromProducer {
        val actor = producer()
        val ctx = FutureContextImpl(actor)
        object : Actor  {
            suspend override fun receive(context: Context) {
                actor.receive(ctx.wrap(context)).await()
            }
        }
    }
}

fun fromFutureFunc(receive : (FutureContext) -> CompletableFuture<*>) {
    val actor = object : FutureActor {
        override fun receive(context: FutureContext): CompletableFuture<*> = receive(context)
    }
    val ctx = FutureContextImpl(actor)
    object : Actor  {
        suspend override fun receive(context: Context) {
            actor.receive(ctx.wrap(context)).await()
        }
    }
}

fun send(pid: PID, message: Any) = pid.send(message)