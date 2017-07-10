package proto.actor

import kotlinx.coroutines.experimental.Deferred
import java.time.Duration

interface IContext {
    val parent : PID?
    val self : PID
    val sender : PID?
    val actor : IActor
    val receiveTimeout : Duration
    val children : Collection<PID>
    fun respond (message : Any)
    fun stash ()
    fun spawn (props : Props) : PID
    fun spawnPrefix (props : Props, prefix : String) : PID
    fun spawnNamed (props : Props, name : String) : PID
    fun watch (pid : PID)
    fun unwatch (pid : PID)
    fun setReceiveTimeout (duration : Duration)
    fun cancelReceiveTimeout ()
    suspend fun receiveAsync (message : Any) : Task
    fun tell (target : PID, message : Any)
    fun request (target : PID, message : Any)

    fun <T> requestAsync (target : PID, message : Any, timeout : Duration) : Deferred<T>
    fun <T> requestAsync (target : PID, message : Any) : Deferred<T>
   // fun reenterAfter (target : Task, action : (Task) -> Task)
}

