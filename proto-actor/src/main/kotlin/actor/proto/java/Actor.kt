package actor.proto.java

import actor.proto.*
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.future.future
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

object Actor {
    private val done : Future<Void> = CompletableFuture.completedFuture(null)
    @JvmStatic fun done() : Future<Void> = done

    @JvmStatic fun fromProducer(producer: () -> JavaActor): Props {
        return actor.proto.fromProducer {
            val actor = producer()
            val ctx = JavaContextImpl()

            val receive: suspend (Context) -> Unit = { innerCtx ->
                ctx.wrap(innerCtx, actor)
                future { actor.receive(ctx) }.await()
            }
            FunActor(receive)
        }
    }

    @JvmStatic fun spawn(props : Props) : PID = actor.proto.spawn(props)

    @JvmStatic fun send(pid: PID,message: Any) = pid.send(message)
}