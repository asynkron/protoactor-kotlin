package actor.proto.remote

import actor.proto.*
import com.google.protobuf.ByteString
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

class EndpointWriter(private val address: String, channelOptions: Enumerable, callOptions: CallOptions, channelCredentials: ChannelCredentials) : Actor {
    private var serializerId: Int = 0
    private val callOptions: CallOptions = callOptions
    private val channelCredentials: ChannelCredentials = channelCredentials
    private val channelOptions: Enumerable = channelOptions
    private var channel: Channel? = null
    private var client: RemotingClient? = null
    private var stream: AsyncDuplexStreamingCall<MessageBatch, Unit>? = null
    private var streamWriter: ClientStreamWriter? = null
    suspend override fun receiveAsync (context : Context) {
        val tmp = context.message
        when (tmp) {
            is Started -> startedAsync()
            is Stopped -> stoppedAsync()
            is Restarting -> restartingAsync()
            is MutableList<*> -> {
                val m = tmp as MutableList<RemoteDeliver>
                val envelopes : MutableList<proto.remote.MessageEnvelope> = mutableListOf()
                val typeNames : HashMap<String, Int> = HashMap()
                val targetNames : HashMap<String, Int> = HashMap()
                val typeNameList : MutableList<String> = mutableListOf()
                val targetNameList : MutableList<String> = mutableListOf()
                for((message, target, sender, explicitSerializerId) in m) {
                    val targetName : String = target.id
                    val serializerId : Int = if (explicitSerializerId == -1) serializerId else explicitSerializerId

                    val targetId = targetNames.getOrPut(targetName){
                        targetNameList.add(targetName)
                        targetNames.count()
                    }
                    val typeName : String = Serialization.getTypeName(message, serializerId)

                    val typeId = typeNames.getOrPut(typeName){
                        typeNameList.add(typeName)
                        typeNames.count()
                    }

                    val bytes : ByteString = Serialization.serialize(message, serializerId)
                    val envelope : proto.remote.MessageEnvelope = MessageEnvelope(bytes, sender,targetId,typeId,serializerId)
                    envelopes.add(envelope)
                }
                val batch : MessageBatch = MessageBatch()
                batch.targetNames.addRange(targetNameList)
                batch.typeNames.addRange(typeNameList)
                batch.envelopes.addRange(envelopes)
                sendEnvelopesAsync(batch, context)
            }
        }
    }
    private suspend fun sendEnvelopesAsync (batch : MessageBatch, context : Context) {
        try {
            streamWriter.writeAsync(batch)
        } catch (x: Exception) {
            context.stash()
            println("gRPC Failed to send to address $address, reason ${x.message}")
            throw  x
        }
    }
    private suspend fun restartingAsync () = channel.shutdownAsync()
    private suspend fun stoppedAsync () = channel.shutdownAsync()
    private suspend fun startedAsync () {
        println("Connecting to address $address")
        channel = Channel(address, channelCredentials, channelOptions)
        client = RemotingClient(channel)
        val res : ConnectResponse = client.connectAsync(ConnectRequest())
        serializerId = res.defaultSerializerId
        stream = client.receive(callOptions)
        launch(CommonPool){
            try  {
                stream.responseStream.forEachAsync{ i -> Actor.Done}
            }
            catch (x : Exception) {
                println("Lost connection to address ${address}, reason ${x.message}")
                val terminated : EndpointTerminatedEvent = EndpointTerminatedEvent
                EventStream.publish(terminated)
            }
        }


        streamWriter = stream.requestStream
        println("Connected to address $address")
    }
}

