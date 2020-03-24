package actor.proto.remote;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
        value = "by gRPC proto compiler (version 1.4.0)",
        comments = "Source: remoteprotos.proto")
@SuppressWarnings("ALL")
public final class RemotingGrpc {

    public static final String SERVICE_NAME = "remote.Remoting";
    // Static method descriptors that strictly reflect the proto.
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
    public static final io.grpc.MethodDescriptor<actor.proto.remote.RemoteProtos.ConnectRequest,
            actor.proto.remote.RemoteProtos.ConnectResponse> METHOD_CONNECT =
            io.grpc.MethodDescriptor.<actor.proto.remote.RemoteProtos.ConnectRequest, actor.proto.remote.RemoteProtos.ConnectResponse>newBuilder()
                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName(generateFullMethodName(
                            "remote.Remoting", "Connect"))
                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                            actor.proto.remote.RemoteProtos.ConnectRequest.getDefaultInstance()))
                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                            actor.proto.remote.RemoteProtos.ConnectResponse.getDefaultInstance()))
                    .build();
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
    public static final io.grpc.MethodDescriptor<actor.proto.remote.RemoteProtos.MessageBatch,
            actor.proto.remote.RemoteProtos.Unit> METHOD_RECEIVE =
            io.grpc.MethodDescriptor.<actor.proto.remote.RemoteProtos.MessageBatch, actor.proto.remote.RemoteProtos.Unit>newBuilder()
                    .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
                    .setFullMethodName(generateFullMethodName(
                            "remote.Remoting", "Receive"))
                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                            actor.proto.remote.RemoteProtos.MessageBatch.getDefaultInstance()))
                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                            actor.proto.remote.RemoteProtos.Unit.getDefaultInstance()))
                    .build();
    private static final int METHODID_CONNECT = 0;
    private static final int METHODID_RECEIVE = 1;
    private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

    private RemotingGrpc() {
    }

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static RemotingStub newStub(io.grpc.Channel channel) {
        return new RemotingStub(channel);
    }

    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service
     */
    public static RemotingBlockingStub newBlockingStub(
            io.grpc.Channel channel) {
        return new RemotingBlockingStub(channel);
    }

    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the service
     */
    public static RemotingFutureStub newFutureStub(
            io.grpc.Channel channel) {
        return new RemotingFutureStub(channel);
    }

    public static io.grpc.ServiceDescriptor getServiceDescriptor() {
        io.grpc.ServiceDescriptor result = serviceDescriptor;
        if (result == null) {
            synchronized (RemotingGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .setSchemaDescriptor(new RemotingDescriptorSupplier())
                            .addMethod(METHOD_CONNECT)
                            .addMethod(METHOD_RECEIVE)
                            .build();
                }
            }
        }
        return result;
    }

    /**
     */
    public static abstract class RemotingImplBase implements io.grpc.BindableService {

        /**
         */
        public void connect(actor.proto.remote.RemoteProtos.ConnectRequest request,
                            io.grpc.stub.StreamObserver<actor.proto.remote.RemoteProtos.ConnectResponse> responseObserver) {
            asyncUnimplementedUnaryCall(METHOD_CONNECT, responseObserver);
        }

        /**
         */
        public io.grpc.stub.StreamObserver<actor.proto.remote.RemoteProtos.MessageBatch> receive(
                io.grpc.stub.StreamObserver<actor.proto.remote.RemoteProtos.Unit> responseObserver) {
            return asyncUnimplementedStreamingCall(METHOD_RECEIVE, responseObserver);
        }

        @java.lang.Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            METHOD_CONNECT,
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            actor.proto.remote.RemoteProtos.ConnectRequest,
                                            actor.proto.remote.RemoteProtos.ConnectResponse>(
                                            this, METHODID_CONNECT)))
                    .addMethod(
                            METHOD_RECEIVE,
                            asyncBidiStreamingCall(
                                    new MethodHandlers<
                                            actor.proto.remote.RemoteProtos.MessageBatch,
                                            actor.proto.remote.RemoteProtos.Unit>(
                                            this, METHODID_RECEIVE)))
                    .build();
        }
    }

    /**
     */
    public static final class RemotingStub extends io.grpc.stub.AbstractStub<RemotingStub> {
        private RemotingStub(io.grpc.Channel channel) {
            super(channel);
        }

        private RemotingStub(io.grpc.Channel channel,
                             io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected RemotingStub build(io.grpc.Channel channel,
                                     io.grpc.CallOptions callOptions) {
            return new RemotingStub(channel, callOptions);
        }

        /**
         */
        public void connect(actor.proto.remote.RemoteProtos.ConnectRequest request,
                            io.grpc.stub.StreamObserver<actor.proto.remote.RemoteProtos.ConnectResponse> responseObserver) {
            asyncUnaryCall(
                    getChannel().newCall(METHOD_CONNECT, getCallOptions()), request, responseObserver);
        }

        /**
         */
        public io.grpc.stub.StreamObserver<actor.proto.remote.RemoteProtos.MessageBatch> receive(
                io.grpc.stub.StreamObserver<actor.proto.remote.RemoteProtos.Unit> responseObserver) {
            return asyncBidiStreamingCall(
                    getChannel().newCall(METHOD_RECEIVE, getCallOptions()), responseObserver);
        }
    }

    /**
     */
    public static final class RemotingBlockingStub extends io.grpc.stub.AbstractStub<RemotingBlockingStub> {
        private RemotingBlockingStub(io.grpc.Channel channel) {
            super(channel);
        }

        private RemotingBlockingStub(io.grpc.Channel channel,
                                     io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected RemotingBlockingStub build(io.grpc.Channel channel,
                                             io.grpc.CallOptions callOptions) {
            return new RemotingBlockingStub(channel, callOptions);
        }

        /**
         */
        public actor.proto.remote.RemoteProtos.ConnectResponse connect(actor.proto.remote.RemoteProtos.ConnectRequest request) {
            return blockingUnaryCall(
                    getChannel(), METHOD_CONNECT, getCallOptions(), request);
        }
    }

    /**
     */
    public static final class RemotingFutureStub extends io.grpc.stub.AbstractStub<RemotingFutureStub> {
        private RemotingFutureStub(io.grpc.Channel channel) {
            super(channel);
        }

        private RemotingFutureStub(io.grpc.Channel channel,
                                   io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected RemotingFutureStub build(io.grpc.Channel channel,
                                           io.grpc.CallOptions callOptions) {
            return new RemotingFutureStub(channel, callOptions);
        }

        /**
         */
        public com.google.common.util.concurrent.ListenableFuture<actor.proto.remote.RemoteProtos.ConnectResponse> connect(
                actor.proto.remote.RemoteProtos.ConnectRequest request) {
            return futureUnaryCall(
                    getChannel().newCall(METHOD_CONNECT, getCallOptions()), request);
        }
    }

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final RemotingImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(RemotingImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_CONNECT:
                    serviceImpl.connect((actor.proto.remote.RemoteProtos.ConnectRequest) request,
                            (io.grpc.stub.StreamObserver<actor.proto.remote.RemoteProtos.ConnectResponse>) responseObserver);
                    break;
                default:
                    throw new AssertionError();
            }
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public io.grpc.stub.StreamObserver<Req> invoke(
                io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_RECEIVE:
                    return (io.grpc.stub.StreamObserver<Req>) serviceImpl.receive(
                            (io.grpc.stub.StreamObserver<actor.proto.remote.RemoteProtos.Unit>) responseObserver);
                default:
                    throw new AssertionError();
            }
        }
    }

    private static final class RemotingDescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier {
        @java.lang.Override
        public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
            return actor.proto.remote.RemoteProtos.getDescriptor();
        }
    }
}
