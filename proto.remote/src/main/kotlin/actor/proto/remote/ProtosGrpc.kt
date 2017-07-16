package proto.remote

object Remoting {
    val __ServiceName : String = "remote.Remoting"
    val __Marshaller_ConnectRequest : Marshaller<ConnectRequest> = Marshallers.create({arg -> globalGoogle.Protobuf.MessageExtensions.toByteArray(arg)}, globalProto.Remote.ConnectRequest.parser.parseFrom)
    val __Marshaller_ConnectResponse : Marshaller<ConnectResponse> = Marshallers.create({arg -> globalGoogle.Protobuf.MessageExtensions.toByteArray(arg)}, globalProto.Remote.ConnectResponse.parser.parseFrom)
    val __Marshaller_MessageBatch : Marshaller<MessageBatch> = Marshallers.create({arg -> globalGoogle.Protobuf.MessageExtensions.toByteArray(arg)}, globalProto.Remote.MessageBatch.parser.parseFrom)
    val __Marshaller_Unit : Marshaller<Unit> = Marshallers.create({arg -> globalGoogle.Protobuf.MessageExtensions.toByteArray(arg)}, globalProto.Remote.Unit.parser.parseFrom)
    val __Method_Connect : Method<ConnectRequest, ConnectResponse> = Method<ConnectRequest, ConnectResponse>(MethodType.Unary, __ServiceName, "Connect", __Marshaller_ConnectRequest, __Marshaller_ConnectResponse)
    val __Method_Receive : Method<MessageBatch, Unit> = Method<MessageBatch, Unit>(MethodType.DuplexStreaming, __ServiceName, "Receive", __Marshaller_MessageBatch, __Marshaller_Unit)
    val descriptor : ServiceDescriptor

    abstract class RemotingBase {
        suspend fun connect (request : ConnectRequest, context : ServerCallContext) : ConnectResponse {
            throw RpcException(Status(StatusCode.Unimplemented, ""))
        }
        suspend fun receive (requestStream : AsyncStreamReader, responseStream : ServerStreamWriter, context : ServerCallContext) {
            throw RpcException(Status(StatusCode.Unimplemented, ""))
        }
    }

    open class RemotingClient : ClientBase<RemotingClient> {
        constructor(channel : Channel)  {
        }
        constructor(callInvoker : CallInvoker)  {
        }
        constructor()  {
        }
        constructor(configuration : ClientBaseConfiguration)  {
        }
        fun connect (request : ConnectRequest, headers : Metadata, deadline : DateTime?, cancellationToken : CancellationToken) : ConnectResponse {
            return connect(request, CallOptions(headers, deadline, cancellationToken))
        }
        fun connect (request : ConnectRequest, options : CallOptions) : ConnectResponse {
            return callInvoker.blockingUnaryCall(__Method_Connect, null, options, request)
        }
        fun connectAsync (request : ConnectRequest, headers : Metadata, deadline : DateTime?, cancellationToken : CancellationToken) : AsyncUnaryCall<ConnectResponse> {
            return connectAsync(request, CallOptions(headers, deadline, cancellationToken))
        }
        fun connectAsync (request : ConnectRequest, options : CallOptions) : AsyncUnaryCall<ConnectResponse> {
            return callInvoker.asyncUnaryCall(__Method_Connect, null, options, request)
        }
        fun receive (headers : Metadata, deadline : DateTime?, cancellationToken : CancellationToken) : AsyncDuplexStreamingCall<MessageBatch, Unit> {
            return receive(CallOptions(headers, deadline, cancellationToken))
        }
        fun receive (options : CallOptions) : AsyncDuplexStreamingCall<MessageBatch, Unit> {
            return callInvoker.asyncDuplexStreamingCall(__Method_Receive, null, options)
        }
        protected fun newInstance (configuration : ClientBaseConfiguration) : RemotingClient {
            return RemotingClient(configuration)
        }
    }
    fun bindService (serviceImpl : RemotingBase) : ServerServiceDefinition {
        return ServerServiceDefinition.createBuilder().addMethod(__Method_Connect, serviceImpl.connect).addMethod(__Method_Receive, serviceImpl.receive).build()
    }
}

