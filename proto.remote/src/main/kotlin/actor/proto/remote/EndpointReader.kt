package proto.remote

import actor.proto.remote.RemoteTerminate
import actor.proto.remote.Serialization

open class EndpointReader : RemotingBase {
    suspend fun connect (request : ConnectRequest, context : ServerCallContext) : ConnectResponse {
        return ConnectResponse()
    }
    suspend fun receive (requestStream : AsyncStreamReader, responseStream : ServerStreamWriter, context : ServerCallContext) {
        requestStream.forEachAsync{batch -> 
            val targetNames : MutableList<String> = mutableListOf(batch.targetNames)
            val typeNames : MutableList<String> = mutableListOf(batch.typeNames)
            for(envelope in batch.envelopes) {
                val targetName : String = targetNames[envelope.target]
                val target : PID = PID(ProcessRegistry.instance.address, targetName)
                val sender : PID = envelope.sender
                val typeName : String = typeNames[envelope.typeId]
                val message : Any = Serialization.deserialize(typeName, envelope.messageData, envelope.serializerId)
                if (message is Terminated /* msg  */) {
                    val rt : RemoteTerminate = RemoteTerminate(target, msg.who)
                    Remote.endpointManagerPid.tell(rt)
                } else if (message is SystemMessage /* sys  */) {
                    target.sendSystemMessage(sys)
                } else {
                    target.request(message, sender)
                }
            }
            return Actor.Done
        }

    }
}

