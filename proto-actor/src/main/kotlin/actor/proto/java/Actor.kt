@file:JvmName("Actor")

package actor.proto.java

import actor.proto.*
import kotlinx.coroutines.experimental.future.await
import java.util.concurrent.CompletableFuture

private val done: CompletableFuture<Void> = CompletableFuture.completedFuture(null)
fun done(): CompletableFuture<Void> = done

fun fromProducer(producer: () -> FutureActor): Props {
    return actor.proto.fromProducer {
        val actor = producer()
        val ctx = JavaContextImpl()

        val receive: suspend (Context) -> Unit = { innerCtx ->
            ctx.wrap(innerCtx, actor)

            actor.receive(ctx).await()
        }
        FunActor(receive)
    }
}

fun spawn(props: Props): PID = actor.proto.spawn(props)

fun send(pid: PID, message: Any) = pid.send(message)
