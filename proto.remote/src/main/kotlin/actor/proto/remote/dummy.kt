package actor.proto.remote

import actor.proto.*

fun main(args : Array<String>) {
    Remote.start("localhost",8080)
    val props = fromFunc {
        val m = message
        when(m) {
            is Started -> println("Starting actor")
            is PID -> {
                println("Got message " + m.id)
                m.tell(PID("HELLO","WORLD"))
            }
        }
    }
    spawnNamed(props,"kotlin")
    readLine()
//    val p = PID("abc","def")
//    val bytes = Serialization.serialize(p,0)
//    val manifest = Serialization.getTypeName(p,0)
//    val res = Serialization.deserialize(manifest,bytes,0)
}