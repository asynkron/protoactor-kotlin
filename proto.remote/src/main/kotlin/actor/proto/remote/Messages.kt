package actor.proto.remote

import actor.proto.PID
import com.google.protobuf.ByteString

data class EndpointTerminatedEvent(var address: String)
data class RemoteTerminate(val watcher: PID,val  watchee: PID)
data class RemoteWatch(val watcher: PID,val watchee: PID)
data class RemoteUnwatch(val watcher: PID,val watchee: PID)
data class RemoteDeliver(val message: Any,val target: PID, val sender: PID?,val serializerId: Int)
data class JsonMessage(val typeName : String,val json : String)

fun ActorPidRequest(kind: String, name: String): RemoteProtos.ActorPidRequest {
    val builder = RemoteProtos.ActorPidRequest.newBuilder()
    builder.kind = kind
    builder.name = name
    val req = builder.build()
    return req
}

fun MessageEnvelope(bytes: ByteString, sender: PID?, targetId: Int, typeId: Int, serializerId: Int): RemoteProtos.MessageEnvelope {
    val envelopeBuilder = RemoteProtos.MessageEnvelope.newBuilder()
    envelopeBuilder.messageData = bytes
    envelopeBuilder.sender = sender
    envelopeBuilder.target = targetId
    envelopeBuilder.typeId = typeId
    envelopeBuilder.serializerId = serializerId
    val envelope = envelopeBuilder.build()
    return envelope
}