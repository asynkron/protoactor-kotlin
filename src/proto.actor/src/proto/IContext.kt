package proto

import java.time.Duration

interface IContext {
    val parent : PID
    val self : PID
    val sender : PID
    val actor : IActor?
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
    fun receiveAsync (message : Any) : Task
    fun tell (target : PID, message : Any)
    fun request (target : PID, message : Any)

    fun requestAsync (target : PID, message : Any, timeout : Duration) : Task<T>
    fun requestAsync (target : PID, message : Any) : Task<T>
    fun reenterAfter (target : Task<T>, action : (Task<T>) -> Task)
}

