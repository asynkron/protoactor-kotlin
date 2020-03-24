package actor.proto.remote

import actor.proto.PID
import actor.proto.ProcessRegistry
import actor.proto.Terminated
import actor.proto.mailbox.SystemMessage
import actor.proto.request
import actor.proto.send
import actor.proto.sendSystemMessage
import io.grpc.stub.StreamObserver
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
class EndpointReader : RemotingGrpc.RemotingImplBase() {
    override fun connect(request: RemoteProtos.ConnectRequest, responseObserver: StreamObserver<RemoteProtos.ConnectResponse>) {
        responseObserver.onNext(ConnectResponse(Serialization.defaultSerializerId))
        responseObserver.onCompleted()
    }

    override fun receive(responseObserver: StreamObserver<RemoteProtos.Unit>): StreamObserver<RemoteProtos.MessageBatch> {
        return object : StreamObserver<RemoteProtos.MessageBatch> {
            override fun onCompleted() = responseObserver.onCompleted()
            override fun onError(err: Throwable): Unit = logger.error("Stream observer exception",err)
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
                is SystemMessage -> sendSystemMessage(target,message)
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

