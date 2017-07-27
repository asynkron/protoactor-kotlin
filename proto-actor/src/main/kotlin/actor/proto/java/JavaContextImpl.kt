package actor.proto.java

import actor.proto.PID
import actor.proto.Props
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.future.asCompletableFuture
import kotlinx.coroutines.experimental.runBlocking
import java.time.Duration
import java.util.concurrent.Future

class JavaContextImpl : JavaContext {

    fun wrap(ctx: actor.proto.Context, a: JavaActor) {
        this.ctx = ctx
        this.a = a
    }

    private lateinit var ctx: actor.proto.Context
    private lateinit var a: JavaActor

    override val parent: PID?
        get() = ctx.parent

    override val self: PID
        get() = ctx.self

    override val sender: PID?
        get() = ctx.sender

    override val actor: JavaActor
        get() = a //TODO: this is wrong?

    override val children: Set<PID>
        get() = ctx.children

    override fun message() : Any = ctx.message

    override fun stash() {
        ctx.stash()
    }

    override fun spawnChild(props: Props): PID = ctx.spawnChild(props)

    override fun spawnPrefixChild(props: Props, prefix: String): PID = ctx.spawnPrefixChild(props, prefix)

    override fun spawnNamedChild(props: Props, name: String): PID = ctx.spawnNamedChild(props, name)

    override fun watch(pid: PID) = ctx.watch(pid)

    override fun unwatch(pid: PID) = ctx.unwatch(pid)

    override fun setReceiveTimeout(duration: Duration) = ctx.setReceiveTimeout(duration)

    override fun getReceiveTimeout(): Duration = ctx.getReceiveTimeout()

    override fun cancelReceiveTimeout() = ctx.cancelReceiveTimeout()

    override fun receive(message: Any) = runBlocking { ctx.receive(message) }

    override fun send(target: PID, message: Any) = runBlocking { ctx.send(target, message) }

    override fun request(target: PID, message: Any) = runBlocking { ctx.request(target, message) }

    override fun respond(message: Any) = runBlocking { ctx.respond(message) }

    override fun <T> requestAwait(target: PID, message: Any, timeout: Duration): Future<T> {
        val d = async(CommonPool) {
            ctx.requestAwait<T>(target, message, timeout)
        }
        return d.asCompletableFuture()
    }

    override fun <T> requestAwait(target: PID, message: Any): Future<T> {
        val d = async(CommonPool) {
            ctx.requestAwait<T>(target, message)
        }
        return d.asCompletableFuture()
    }
}