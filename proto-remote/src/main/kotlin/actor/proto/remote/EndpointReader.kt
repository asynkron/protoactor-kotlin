package actor.proto.remote

import actor.proto.*
import actor.proto.mailbox.SystemMessage
import io.grpc.stub.StreamObserver

class EndpointReader : RemotingGrpc.RemotingImplBase() {
    override fun connect(request: RemoteProtos.ConnectRequest, responseObserver: StreamObserver<RemoteProtos.ConnectResponse>) {
        responseObserver.onNext(ConnectResponse(Serialization.defaultSerializerId))
        responseObserver.onCompleted()
    }

    override fun receive(responseObserver: StreamObserver<RemoteProtos.Unit>): StreamObserver<RemoteProtos.MessageBatch> {
        return object : StreamObserver<RemoteProtos.MessageBatch> {
            override fun onCompleted() = responseObserver.onCompleted()
            override fun onError(err: Throwable): Unit = println(err)
            override fun onNext(batch: RemoteProtos.MessageBatch) = receiveBatch(batch)
        }
    }

    fun receiveBatch(batch: RemoteProtos.MessageBatch) {
        val targetNames = batch.targetNamesList
        val typeNames = batch.typeNamesList

        for (envelope in batch.envelopesList) {
            val targetName: String = targetNames[envelope.target]
            val target: PID = PID(ProcessRegistry.address, targetName)
            val typeName: String = typeNames[envelope.typeId]
            val message: Any = Serialization.deserialize(typeName, envelope.messageData, envelope.serializerId)
            when (message) {
                is Terminated -> send(Remote.endpointManagerPid, RemoteTerminate(target, message.who))
                is SystemMessage -> target.sendSystemMessage(message)
                else -> {
                    when {
                        envelope.hasSender() -> {
                            val sender: PID = envelope.sender
                            request(target, message, sender)
                        }
                        else -> send(target, message)
                    }
                }
            }
        }
    }
}

