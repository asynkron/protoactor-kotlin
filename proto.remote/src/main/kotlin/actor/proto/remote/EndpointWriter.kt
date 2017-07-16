package proto.remote

import actor.proto.remote.EndpointTerminatedEvent

open class EndpointWriter : Actor {
    private var _serializerId : Int = 0
    private val _address : String
    private val _callOptions : CallOptions = CallOptions()
    private val _channelCredentials : ChannelCredentials
    private val _channelOptions : Enumerable
    private val _logger : Logger = Log.createLogger<EndpointWriter>()
    private var _channel : Channel? = null
    private var _client : RemotingClient? = null
    private var _stream : AsyncDuplexStreamingCall<MessageBatch, Unit>? = null
    private var _streamWriter : ClientStreamWriter? = null
    constructor(address : String, channelOptions : Enumerable, callOptions : CallOptions, channelCredentials : ChannelCredentials)  {
        _address = address
        _channelOptions = channelOptions
        _callOptions = callOptions
        _channelCredentials = channelCredentials
    }
    suspend override fun receiveAsync (context : Context) {
        val tmp = context.message
        when (tmp) {
            is Started -> {
                startedAsync()
            }
            is Stopped -> {
                stoppedAsync()
            }
            is Restarting -> {
                restartingAsync()
            }
            is Enumerable -> {
                val m = tmp
                val envelopes : MutableList<MessageEnvelope> = mutableListOf()
                val typeNames : Dictionary<String, Int> = Dictionary<String, Int>()
                val targetNames : Dictionary<String, Int> = Dictionary<String, Int>()
                val typeNameList : MutableList<String> = mutableListOf()
                val targetNameList : MutableList<String> = mutableListOf()
                for(rd in m) {
                    val targetName : String = rd.target.id
                    val serializerId : Int = if (rd.serializerId == -1) _serializerId else rd.serializerId
                    if (!targetNames.tryGetValue(targetName, var targetId)) {
                        targetId = targetNames[targetName] = targetNames.count
                        targetNameList.add(targetName)
                    }
                    val typeName : String = Serialization.getTypeName(rd.message, serializerId)
                    if (!typeNames.tryGetValue(typeName, var typeId)) {
                        typeId = typeNames[typeName] = typeNames.count
                        typeNameList.add(typeName)
                    }
                    val bytes : ByteString = Serialization.serialize(rd.message, serializerId)
                    val envelope : MessageEnvelope = MessageEnvelope
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
        try  {
            _streamWriter.writeAsync(batch)
        }
        catch (x : Exception) {
            context.stash()
            _logger.logError("gRPC Failed to send to address ${_address}, reason ${x.message}")
            throw 
        }
    }
    private suspend fun restartingAsync () = _channel.shutdownAsync()
    private suspend fun stoppedAsync () = _channel.shutdownAsync()
    private suspend fun startedAsync () {
        _logger.logDebug("Connecting to address ${_address}")
        _channel = Channel(_address, _channelCredentials, _channelOptions)
        _client = RemotingClient(_channel)
        val res : ConnectResponse = _client.connectAsync(ConnectRequest())
        _serializerId = res.defaultSerializerId
        _stream = _client.receive(_callOptions)
        val _ : Unit = Task.factory.startNew{ -> 
            try  {
                _stream.responseStream.forEachAsync{i -> Actor.Done}
            }
            catch (x : Exception) {
                _logger.logError("Lost connection to address ${_address}, reason ${x.message}")
                val terminated : EndpointTerminatedEvent = EndpointTerminatedEvent
                Actor.eventStream.publish(terminated)
            }
        }

        _streamWriter = _stream.requestStream
        _logger.logDebug("Connected to address ${_address}")
    }
}

