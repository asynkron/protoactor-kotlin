package actor.proto.java

import actor.proto.*
import kotlinx.coroutines.experimental.future.await
import java.util.concurrent.CompletableFuture

object Actor {
    private val done :  CompletableFuture<Void> = CompletableFuture.completedFuture(null)
    @JvmStatic fun done() : CompletableFuture<Void> = done

    @JvmStatic fun fromProducer(producer: () -> FutureActor): Props {
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

    @JvmStatic fun spawn(props : Props) : PID = actor.proto.spawn(props)

    @JvmStatic fun send(pid: PID,message: Any) = pid.send(message)
}