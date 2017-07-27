package actor.proto.java

import actor.proto.FunActor
import actor.proto.Props
import kotlinx.coroutines.experimental.future.future

object JavaAPI {
    fun fromProducer(producer: () -> actor.proto.java.JavaActor): Props {
        return actor.proto.fromProducer {
            val actor = producer()
            val ctx = JavaContextImpl()

            val receive: suspend (actor.proto.Context) -> Unit = { innerCtx ->
                ctx.wrap(innerCtx, actor)
                future { actor.receive(ctx) }
            }
            FunActor(receive)
        }
    }
}