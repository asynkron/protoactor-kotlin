package actor.proto.remote

import actor.proto.*
import com.google.protobuf.ByteString
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import java.util.concurrent.TimeUnit
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
class EndpointWriter(private val address : String, private val config : RemoteConfig) : Actor {
    private var serializerId: Int = 0
    private lateinit var channel: ManagedChannel
    private lateinit var client: RemotingGrpc.RemotingStub
    private lateinit var streamWriter: StreamObserver<RemoteProtos.MessageBatch>
    suspend override fun Context.receive(msg: Any) {
        when (msg) {
            is Started -> started()
            is Stopped -> stopped()
            is Restarting -> restarting()
            is MutableList<*> -> {
                @Suppress("UNCHECKED_CAST")
                val m = msg as MutableList<RemoteDeliver>
                val envelopes: MutableList<RemoteProtos.MessageEnvelope> = mutableListOf()
                val typeNames: HashMap<String, Int> = HashMap()
                val targetNames: HashMap<String, Int> = HashMap()
                val typeNameList: MutableList<String> = mutableListOf()
                val targetNameList: MutableList<String> = mutableListOf()
                for ((message, target, sender, explicitSerializerId) in m) {
                    val targetName = target.id
                    val serializerId = if (explicitSerializerId == -1) serializerId else explicitSerializerId

                    val targetId = targetNames.getOrPut(targetName) {
                        targetNameList.add(targetName)
                        targetNames.count()
                    }

                    val typeName = Serialization.getTypeName(message, serializerId)
                    val typeId = typeNames.getOrPut(typeName) {
                        typeNameList.add(typeName)
                        typeNames.count()
                    }

                    val bytes: ByteString = Serialization.serialize(message, serializerId)
                    val envelope = MessageEnvelope(bytes, sender, targetId, typeId, serializerId)
                    envelopes.add(envelope)
                }
                val batch = RemoteProtos.MessageBatch
                        .newBuilder()
                        .addAllTargetNames(targetNameList)
                        .addAllTypeNames(typeNameList)
                        .addAllEnvelopes(envelopes)
                        .build()
                sendEnvelopesAsync(batch)
            }
        }
    }

    private suspend fun Context.sendEnvelopesAsync(batch: RemoteProtos.MessageBatch) {
        try {
            streamWriter.onNext(batch)
        } catch (x: Exception) {
            stash()
            logger.error("gRPC Failed to send to address $address, reason ${x.message}")
            throw  x
        }
    }

    private suspend fun restarting() = channel.shutdownNow()
    private suspend fun stopped() = channel.shutdownNow()
    private suspend fun started() {
        logger.info("Connecting to address $address")
        val (host, port) = parseAddress(address)
        var channelBuilder = ManagedChannelBuilder
                .forAddress(host, port)
        if (config.usePlainText) channelBuilder.usePlaintext(true)
        config.idleTimeout?.let { channelBuilder.idleTimeout(it, TimeUnit.MILLISECONDS) }
        config.keepAliveTime?.let { channelBuilder.keepAliveTime(it, TimeUnit.MILLISECONDS) }
        config.keepAliveTimeout?.let { channelBuilder.keepAliveTimeout(it, TimeUnit.MILLISECONDS) }
        config.keepAliveWithoutCalls?.let { channelBuilder.keepAliveWithoutCalls(it) }
        channel = channelBuilder.build()
        client = RemotingGrpc.newStub(channel)
        val blockingClient = RemotingGrpc.newBlockingStub(channel)
        val res = blockingClient.connect(ConnectRequest())
        serializerId = res.defaultSerializerId
        streamWriter = client.receive(object : StreamObserver<RemoteProtos.Unit> {
            override fun onNext(value: RemoteProtos.Unit?) {
                //never called
            }
            override fun onCompleted() {
                //never called
            }
            override fun onError(t: Throwable?) {
                //According to gRPC docs any call to error is the final call and signals termination
                //val status = Status.fromThrowable(t)
                val terminated: EndpointTerminatedEvent = EndpointTerminatedEvent(address)
                EventStream.publish(terminated)
                println("Lost connection to address $address")
            }
        })

        logger.info("Connected to address $address")
    }
}

