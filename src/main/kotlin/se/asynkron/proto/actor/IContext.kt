package proto.actor

import java.time.Duration

interface IContext {
    val parent: PID?
    val self: PID
    val sender: PID?
    val actor: Actor
    val children: Set<PID>
    val message: Any
    fun respond(message: Any)
    fun stash()
    fun spawn(props: Props): PID
    fun spawnPrefix(props: Props, prefix: String): PID
    fun spawnNamed(props: Props, name: String): PID
    fun watch(pid: PID)
    fun unwatch(pid: PID)
    fun setReceiveTimeout(duration: Duration)
    fun getReceiveTimeout(): Duration
    fun cancelReceiveTimeout()
    suspend fun receiveAsync(message: Any): Unit
    fun tell(target: PID, message: Any)
    fun request(target: PID, message: Any)

    suspend fun <T> requestAsync(target: PID, message: Any, timeout: Duration): T
    suspend fun <T> requestAsync(target: PID, message: Any): T
    // fun reenterAfter (target : Task, action : (Task) -> Task)
}

