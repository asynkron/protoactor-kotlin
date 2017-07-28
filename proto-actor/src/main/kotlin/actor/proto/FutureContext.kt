package actor.proto

import java.time.Duration
import java.util.concurrent.CompletableFuture

interface FutureContext {
    fun parent(): PID?
    fun self(): PID
    fun sender(): PID?
    fun actor(): FutureActor
    fun children(): Set<PID>

    fun message() : Any
    fun stash()
    fun spawnChild(props: Props): PID
    fun spawnPrefixChild(props: Props, prefix: String): PID
    fun spawnNamedChild(props: Props, name: String): PID
    fun watch(pid: PID)
    fun unwatch(pid: PID)
    fun setReceiveTimeout(duration: Duration)
    fun getReceiveTimeout(): Duration
    fun cancelReceiveTimeout()
    fun receive(message: Any): Unit
    fun send(target: PID, message: Any)
    fun request(target: PID, message: Any)
    fun respond(message: Any)

    fun <T> requestAwait(target: PID, message: Any, timeout: Duration): CompletableFuture<T>
    fun <T> requestAwait(target: PID, message: Any): CompletableFuture<T>
    // fun reenterAfter (target : Task, action : (Task) -> Task)
}