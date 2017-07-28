@file:JvmName("Actors")
@file:JvmMultifileClass

package actor.proto

import kotlinx.coroutines.experimental.future.await
import java.util.concurrent.CompletableFuture

@JvmSynthetic fun fromProducer(producer: () -> Actor): Props = Props().withProducer(producer)
@JvmSynthetic fun fromFunc(receive: suspend Context.() -> Unit): Props = fromProducer { FunActor(receive) }
fun spawn(props: Props): PID {
    val name = ProcessRegistry.nextId()
    return spawnNamed(props, name)
}

fun spawnPrefix(props: Props, prefix: String): PID {
    val name = prefix + ProcessRegistry.nextId()
    return spawnNamed(props, name)
}

fun spawnNamed(props: Props, name: String): PID {
    return props.spawn(name, null)
}

private val done: CompletableFuture<Void> = CompletableFuture.completedFuture(null)
fun done(): CompletableFuture<Void> = done

fun fromFutureProducer(producer: () -> FutureActor): Props {
    return fromProducer {
        val actor = producer()
        val ctx = FutureContextImpl()

        val receive: suspend (Context) -> Unit = { innerCtx ->
            ctx.wrap(innerCtx, actor)

            actor.receive(ctx).await()
        }
        FunActor(receive)
    }
}

fun send(pid: PID, message: Any) = pid.send(message)
