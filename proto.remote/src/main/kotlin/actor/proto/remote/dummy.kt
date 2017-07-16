package actor.proto.remote

import actor.proto.PID

fun main(args : Array<String>) {
    val p = PID("abc","def")
    val bytes = Serialization.serialize(p,0)
    val manifest = Serialization.getTypeName(p,0)
    val res = Serialization.deserialize(manifest,bytes,0)
}