package actor.proto.remote

import actor.proto.*
import actor.proto.mailbox.SystemMessage

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
                val target : PID = PID(ProcessRegistry.address, targetName)
                val sender : PID = envelope.sender
                val typeName : String = typeNames[envelope.typeId]
                val message : Any = Serialization.deserialize(typeName, envelope.messageData, envelope.serializerId)
                when (message) {
                    is Terminated -> {
                        val rt = RemoteTerminate(target, message.who)
                        Remote.endpointManagerPid.tell(rt)
                    }
                    is SystemMessage -> target.sendSystemMessage(message)
                    else -> target.request(message, sender)
                }
            }
        }
    }
}

