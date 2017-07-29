package actor.proto.java

import actor.proto.PID
import actor.proto.Props
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.future.asCompletableFuture
import kotlinx.coroutines.experimental.runBlocking
import java.time.Duration
import java.util.concurrent.CompletableFuture

class ContextImpl(private val actor: Actor) : Context {

    fun wrap(ctx: actor.proto.Context) : Context {
        this.ctx = ctx
        return this
    }

    private lateinit var ctx: actor.proto.Context
    override fun parent(): PID? = ctx.parent
    override fun self(): PID = ctx.self
    override fun sender(): PID? = ctx.sender
    override fun actor(): Actor = actor
    override fun children(): Set<PID> = ctx.children
    override fun message() : Any = ctx.message
    override fun stash() = ctx.stash()
    override fun spawnChild(props: Props): PID = ctx.spawnChild(props)
    override fun spawnPrefixChild(props: Props, prefix: String): PID = ctx.spawnPrefixChild(props, prefix)
    override fun spawnNamedChild(props: Props, name: String): PID = ctx.spawnNamedChild(props, name)
    override fun watch(pid: PID) = ctx.watch(pid)
    override fun unwatch(pid: PID) = ctx.unwatch(pid)
    override fun setReceiveTimeout(duration: Duration) = ctx.setReceiveTimeout(duration)
    override fun getReceiveTimeout(): Duration = ctx.getReceiveTimeout()
    override fun cancelReceiveTimeout() = ctx.cancelReceiveTimeout()
    override fun send(target: PID, message: Any) = runBlocking { ctx.send(target, message) }
    override fun request(target: PID, message: Any) = runBlocking { ctx.request(target, message) }
    override fun respond(message: Any) = runBlocking { ctx.respond(message) }
    override fun <T> requestAwait(target: PID, message: Any, timeout: Duration): CompletableFuture<T> {
        val d = async(CommonPool) {
            ctx.requestAwait<T>(target, message, timeout)
        }
        return d.asCompletableFuture()
    }

    override fun <T> requestAwait(target: PID, message: Any): CompletableFuture<T> {
        val d = async(CommonPool) {
            ctx.requestAwait<T>(target, message)
        }
        return d.asCompletableFuture()
    }
}