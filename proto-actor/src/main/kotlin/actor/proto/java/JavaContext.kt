package actor.proto.java

import actor.proto.PID
import actor.proto.Props
import java.time.Duration
import java.util.concurrent.CompletableFuture

interface JavaContext {
    val parent: PID?
    val self: PID
    val sender: PID?
    val actor: FutureActor
    val children: Set<PID>

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