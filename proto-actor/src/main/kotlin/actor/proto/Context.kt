package actor.proto

import java.time.Duration

interface Context {
    val parent: PID?
    val self: PID
    val sender: PID?
    val actor: Actor
    val children: Set<PID>
    val message: Any

    fun stash()
    fun spawnChild(props: Props): PID
    fun spawnPrefixChild(props: Props, prefix: String): PID
    fun spawnNamedChild(props: Props, name: String): PID
    fun watch(pid: PID)
    fun unwatch(pid: PID)
    fun setReceiveTimeout(duration: Duration)
    fun getReceiveTimeout(): Duration
    fun cancelReceiveTimeout()
    fun send(target: PID, message: Any)
    fun request(target: PID, message: Any)
    fun respond(message: Any)

    suspend fun <T> requestAwait(target: PID, message: Any, timeout: Duration): T
    suspend fun <T> requestAwait(target: PID, message: Any): T
    // fun reenterAfter (target : Task, action : (Task) -> Task)
}

