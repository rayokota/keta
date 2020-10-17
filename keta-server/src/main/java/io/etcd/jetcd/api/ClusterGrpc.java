package io.etcd.jetcd.api;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.20.0)",
    comments = "Source: rpc.proto")
public final class ClusterGrpc {

  private ClusterGrpc() {}

  private static <T> io.grpc.stub.StreamObserver<T> toObserver(final io.vertx.core.Handler<io.vertx.core.AsyncResult<T>> handler) {
    return new io.grpc.stub.StreamObserver<T>() {
      private volatile boolean resolved = false;
      @Override
      public void onNext(T value) {
        if (!resolved) {
          resolved = true;
          handler.handle(io.vertx.core.Future.succeededFuture(value));
        }
      }

      @Override
      public void onError(Throwable t) {
        if (!resolved) {
          resolved = true;
          handler.handle(io.vertx.core.Future.failedFuture(t));
        }
      }

      @Override
      public void onCompleted() {
        if (!resolved) {
          resolved = true;
          handler.handle(io.vertx.core.Future.succeededFuture());
        }
      }
    };
  }

  public static final String SERVICE_NAME = "etcdserverpb.Cluster";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.etcd.jetcd.api.MemberAddRequest,
      io.etcd.jetcd.api.MemberAddResponse> getMemberAddMethod;

  public static io.grpc.MethodDescriptor<io.etcd.jetcd.api.MemberAddRequest,
      io.etcd.jetcd.api.MemberAddResponse> getMemberAddMethod() {
    io.grpc.MethodDescriptor<io.etcd.jetcd.api.MemberAddRequest, io.etcd.jetcd.api.MemberAddResponse> getMemberAddMethod;
    if ((getMemberAddMethod = ClusterGrpc.getMemberAddMethod) == null) {
      synchronized (ClusterGrpc.class) {
        if ((getMemberAddMethod = ClusterGrpc.getMemberAddMethod) == null) {
          ClusterGrpc.getMemberAddMethod = getMemberAddMethod = 
              io.grpc.MethodDescriptor.<io.etcd.jetcd.api.MemberAddRequest, io.etcd.jetcd.api.MemberAddResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "etcdserverpb.Cluster", "MemberAdd"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.MemberAddRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.MemberAddResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ClusterMethodDescriptorSupplier("MemberAdd"))
                  .build();
          }
        }
     }
     return getMemberAddMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.etcd.jetcd.api.MemberRemoveRequest,
      io.etcd.jetcd.api.MemberRemoveResponse> getMemberRemoveMethod;

  public static io.grpc.MethodDescriptor<io.etcd.jetcd.api.MemberRemoveRequest,
      io.etcd.jetcd.api.MemberRemoveResponse> getMemberRemoveMethod() {
    io.grpc.MethodDescriptor<io.etcd.jetcd.api.MemberRemoveRequest, io.etcd.jetcd.api.MemberRemoveResponse> getMemberRemoveMethod;
    if ((getMemberRemoveMethod = ClusterGrpc.getMemberRemoveMethod) == null) {
      synchronized (ClusterGrpc.class) {
        if ((getMemberRemoveMethod = ClusterGrpc.getMemberRemoveMethod) == null) {
          ClusterGrpc.getMemberRemoveMethod = getMemberRemoveMethod = 
              io.grpc.MethodDescriptor.<io.etcd.jetcd.api.MemberRemoveRequest, io.etcd.jetcd.api.MemberRemoveResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "etcdserverpb.Cluster", "MemberRemove"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.MemberRemoveRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.MemberRemoveResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ClusterMethodDescriptorSupplier("MemberRemove"))
                  .build();
          }
        }
     }
     return getMemberRemoveMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.etcd.jetcd.api.MemberUpdateRequest,
      io.etcd.jetcd.api.MemberUpdateResponse> getMemberUpdateMethod;

  public static io.grpc.MethodDescriptor<io.etcd.jetcd.api.MemberUpdateRequest,
      io.etcd.jetcd.api.MemberUpdateResponse> getMemberUpdateMethod() {
    io.grpc.MethodDescriptor<io.etcd.jetcd.api.MemberUpdateRequest, io.etcd.jetcd.api.MemberUpdateResponse> getMemberUpdateMethod;
    if ((getMemberUpdateMethod = ClusterGrpc.getMemberUpdateMethod) == null) {
      synchronized (ClusterGrpc.class) {
        if ((getMemberUpdateMethod = ClusterGrpc.getMemberUpdateMethod) == null) {
          ClusterGrpc.getMemberUpdateMethod = getMemberUpdateMethod = 
              io.grpc.MethodDescriptor.<io.etcd.jetcd.api.MemberUpdateRequest, io.etcd.jetcd.api.MemberUpdateResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "etcdserverpb.Cluster", "MemberUpdate"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.MemberUpdateRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.MemberUpdateResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ClusterMethodDescriptorSupplier("MemberUpdate"))
                  .build();
          }
        }
     }
     return getMemberUpdateMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.etcd.jetcd.api.MemberListRequest,
      io.etcd.jetcd.api.MemberListResponse> getMemberListMethod;

  public static io.grpc.MethodDescriptor<io.etcd.jetcd.api.MemberListRequest,
      io.etcd.jetcd.api.MemberListResponse> getMemberListMethod() {
    io.grpc.MethodDescriptor<io.etcd.jetcd.api.MemberListRequest, io.etcd.jetcd.api.MemberListResponse> getMemberListMethod;
    if ((getMemberListMethod = ClusterGrpc.getMemberListMethod) == null) {
      synchronized (ClusterGrpc.class) {
        if ((getMemberListMethod = ClusterGrpc.getMemberListMethod) == null) {
          ClusterGrpc.getMemberListMethod = getMemberListMethod = 
              io.grpc.MethodDescriptor.<io.etcd.jetcd.api.MemberListRequest, io.etcd.jetcd.api.MemberListResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "etcdserverpb.Cluster", "MemberList"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.MemberListRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.MemberListResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ClusterMethodDescriptorSupplier("MemberList"))
                  .build();
          }
        }
     }
     return getMemberListMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ClusterStub newStub(io.grpc.Channel channel) {
    return new ClusterStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ClusterBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new ClusterBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ClusterFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new ClusterFutureStub(channel);
  }

  /**
   * Creates a new vertx stub that supports all call types for the service
   */
  public static ClusterVertxStub newVertxStub(io.grpc.Channel channel) {
    return new ClusterVertxStub(channel);
  }

  /**
   */
  public static abstract class ClusterImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * MemberAdd adds a member into the cluster.
     * </pre>
     */
    public void memberAdd(io.etcd.jetcd.api.MemberAddRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.MemberAddResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getMemberAddMethod(), responseObserver);
    }

    /**
     * <pre>
     * MemberRemove removes an existing member from the cluster.
     * </pre>
     */
    public void memberRemove(io.etcd.jetcd.api.MemberRemoveRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.MemberRemoveResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getMemberRemoveMethod(), responseObserver);
    }

    /**
     * <pre>
     * MemberUpdate updates the member configuration.
     * </pre>
     */
    public void memberUpdate(io.etcd.jetcd.api.MemberUpdateRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.MemberUpdateResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getMemberUpdateMethod(), responseObserver);
    }

    /**
     * <pre>
     * MemberList lists all the members in the cluster.
     * </pre>
     */
    public void memberList(io.etcd.jetcd.api.MemberListRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.MemberListResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getMemberListMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getMemberAddMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.etcd.jetcd.api.MemberAddRequest,
                io.etcd.jetcd.api.MemberAddResponse>(
                  this, METHODID_MEMBER_ADD)))
          .addMethod(
            getMemberRemoveMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.etcd.jetcd.api.MemberRemoveRequest,
                io.etcd.jetcd.api.MemberRemoveResponse>(
                  this, METHODID_MEMBER_REMOVE)))
          .addMethod(
            getMemberUpdateMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.etcd.jetcd.api.MemberUpdateRequest,
                io.etcd.jetcd.api.MemberUpdateResponse>(
                  this, METHODID_MEMBER_UPDATE)))
          .addMethod(
            getMemberListMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.etcd.jetcd.api.MemberListRequest,
                io.etcd.jetcd.api.MemberListResponse>(
                  this, METHODID_MEMBER_LIST)))
          .build();
    }
  }

  /**
   */
  public static final class ClusterStub extends io.grpc.stub.AbstractStub<ClusterStub> {
    public ClusterStub(io.grpc.Channel channel) {
      super(channel);
    }

    public ClusterStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClusterStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ClusterStub(channel, callOptions);
    }

    /**
     * <pre>
     * MemberAdd adds a member into the cluster.
     * </pre>
     */
    public void memberAdd(io.etcd.jetcd.api.MemberAddRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.MemberAddResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getMemberAddMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * MemberRemove removes an existing member from the cluster.
     * </pre>
     */
    public void memberRemove(io.etcd.jetcd.api.MemberRemoveRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.MemberRemoveResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getMemberRemoveMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * MemberUpdate updates the member configuration.
     * </pre>
     */
    public void memberUpdate(io.etcd.jetcd.api.MemberUpdateRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.MemberUpdateResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getMemberUpdateMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * MemberList lists all the members in the cluster.
     * </pre>
     */
    public void memberList(io.etcd.jetcd.api.MemberListRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.MemberListResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getMemberListMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class ClusterBlockingStub extends io.grpc.stub.AbstractStub<ClusterBlockingStub> {
    public ClusterBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    public ClusterBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClusterBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ClusterBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * MemberAdd adds a member into the cluster.
     * </pre>
     */
    public io.etcd.jetcd.api.MemberAddResponse memberAdd(io.etcd.jetcd.api.MemberAddRequest request) {
      return blockingUnaryCall(
          getChannel(), getMemberAddMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * MemberRemove removes an existing member from the cluster.
     * </pre>
     */
    public io.etcd.jetcd.api.MemberRemoveResponse memberRemove(io.etcd.jetcd.api.MemberRemoveRequest request) {
      return blockingUnaryCall(
          getChannel(), getMemberRemoveMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * MemberUpdate updates the member configuration.
     * </pre>
     */
    public io.etcd.jetcd.api.MemberUpdateResponse memberUpdate(io.etcd.jetcd.api.MemberUpdateRequest request) {
      return blockingUnaryCall(
          getChannel(), getMemberUpdateMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * MemberList lists all the members in the cluster.
     * </pre>
     */
    public io.etcd.jetcd.api.MemberListResponse memberList(io.etcd.jetcd.api.MemberListRequest request) {
      return blockingUnaryCall(
          getChannel(), getMemberListMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class ClusterFutureStub extends io.grpc.stub.AbstractStub<ClusterFutureStub> {
    public ClusterFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    public ClusterFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClusterFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ClusterFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * MemberAdd adds a member into the cluster.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.etcd.jetcd.api.MemberAddResponse> memberAdd(
        io.etcd.jetcd.api.MemberAddRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getMemberAddMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * MemberRemove removes an existing member from the cluster.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.etcd.jetcd.api.MemberRemoveResponse> memberRemove(
        io.etcd.jetcd.api.MemberRemoveRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getMemberRemoveMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * MemberUpdate updates the member configuration.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.etcd.jetcd.api.MemberUpdateResponse> memberUpdate(
        io.etcd.jetcd.api.MemberUpdateRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getMemberUpdateMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * MemberList lists all the members in the cluster.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.etcd.jetcd.api.MemberListResponse> memberList(
        io.etcd.jetcd.api.MemberListRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getMemberListMethod(), getCallOptions()), request);
    }
  }

  /**
   */
  public static abstract class ClusterVertxImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * MemberAdd adds a member into the cluster.
     * </pre>
     */
    public void memberAdd(io.etcd.jetcd.api.MemberAddRequest request,
        io.vertx.core.Promise<io.etcd.jetcd.api.MemberAddResponse> response) {
      asyncUnimplementedUnaryCall(getMemberAddMethod(), ClusterGrpc.toObserver(response));
    }

    /**
     * <pre>
     * MemberRemove removes an existing member from the cluster.
     * </pre>
     */
    public void memberRemove(io.etcd.jetcd.api.MemberRemoveRequest request,
        io.vertx.core.Promise<io.etcd.jetcd.api.MemberRemoveResponse> response) {
      asyncUnimplementedUnaryCall(getMemberRemoveMethod(), ClusterGrpc.toObserver(response));
    }

    /**
     * <pre>
     * MemberUpdate updates the member configuration.
     * </pre>
     */
    public void memberUpdate(io.etcd.jetcd.api.MemberUpdateRequest request,
        io.vertx.core.Promise<io.etcd.jetcd.api.MemberUpdateResponse> response) {
      asyncUnimplementedUnaryCall(getMemberUpdateMethod(), ClusterGrpc.toObserver(response));
    }

    /**
     * <pre>
     * MemberList lists all the members in the cluster.
     * </pre>
     */
    public void memberList(io.etcd.jetcd.api.MemberListRequest request,
        io.vertx.core.Promise<io.etcd.jetcd.api.MemberListResponse> response) {
      asyncUnimplementedUnaryCall(getMemberListMethod(), ClusterGrpc.toObserver(response));
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getMemberAddMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                io.etcd.jetcd.api.MemberAddRequest,
                io.etcd.jetcd.api.MemberAddResponse>(
                  this, METHODID_MEMBER_ADD)))
          .addMethod(
            getMemberRemoveMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                io.etcd.jetcd.api.MemberRemoveRequest,
                io.etcd.jetcd.api.MemberRemoveResponse>(
                  this, METHODID_MEMBER_REMOVE)))
          .addMethod(
            getMemberUpdateMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                io.etcd.jetcd.api.MemberUpdateRequest,
                io.etcd.jetcd.api.MemberUpdateResponse>(
                  this, METHODID_MEMBER_UPDATE)))
          .addMethod(
            getMemberListMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                io.etcd.jetcd.api.MemberListRequest,
                io.etcd.jetcd.api.MemberListResponse>(
                  this, METHODID_MEMBER_LIST)))
          .build();
    }
  }

  /**
   */
  public static final class ClusterVertxStub extends io.grpc.stub.AbstractStub<ClusterVertxStub> {
    public ClusterVertxStub(io.grpc.Channel channel) {
      super(channel);
    }

    public ClusterVertxStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClusterVertxStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ClusterVertxStub(channel, callOptions);
    }

    /**
     * <pre>
     * MemberAdd adds a member into the cluster.
     * </pre>
     */
    public void memberAdd(io.etcd.jetcd.api.MemberAddRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<io.etcd.jetcd.api.MemberAddResponse>> response) {
      asyncUnaryCall(
          getChannel().newCall(getMemberAddMethod(), getCallOptions()), request, ClusterGrpc.toObserver(response));
    }

    /**
     * <pre>
     * MemberRemove removes an existing member from the cluster.
     * </pre>
     */
    public void memberRemove(io.etcd.jetcd.api.MemberRemoveRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<io.etcd.jetcd.api.MemberRemoveResponse>> response) {
      asyncUnaryCall(
          getChannel().newCall(getMemberRemoveMethod(), getCallOptions()), request, ClusterGrpc.toObserver(response));
    }

    /**
     * <pre>
     * MemberUpdate updates the member configuration.
     * </pre>
     */
    public void memberUpdate(io.etcd.jetcd.api.MemberUpdateRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<io.etcd.jetcd.api.MemberUpdateResponse>> response) {
      asyncUnaryCall(
          getChannel().newCall(getMemberUpdateMethod(), getCallOptions()), request, ClusterGrpc.toObserver(response));
    }

    /**
     * <pre>
     * MemberList lists all the members in the cluster.
     * </pre>
     */
    public void memberList(io.etcd.jetcd.api.MemberListRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<io.etcd.jetcd.api.MemberListResponse>> response) {
      asyncUnaryCall(
          getChannel().newCall(getMemberListMethod(), getCallOptions()), request, ClusterGrpc.toObserver(response));
    }
  }

  private static final int METHODID_MEMBER_ADD = 0;
  private static final int METHODID_MEMBER_REMOVE = 1;
  private static final int METHODID_MEMBER_UPDATE = 2;
  private static final int METHODID_MEMBER_LIST = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ClusterImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ClusterImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_MEMBER_ADD:
          serviceImpl.memberAdd((io.etcd.jetcd.api.MemberAddRequest) request,
              (io.grpc.stub.StreamObserver<io.etcd.jetcd.api.MemberAddResponse>) responseObserver);
          break;
        case METHODID_MEMBER_REMOVE:
          serviceImpl.memberRemove((io.etcd.jetcd.api.MemberRemoveRequest) request,
              (io.grpc.stub.StreamObserver<io.etcd.jetcd.api.MemberRemoveResponse>) responseObserver);
          break;
        case METHODID_MEMBER_UPDATE:
          serviceImpl.memberUpdate((io.etcd.jetcd.api.MemberUpdateRequest) request,
              (io.grpc.stub.StreamObserver<io.etcd.jetcd.api.MemberUpdateResponse>) responseObserver);
          break;
        case METHODID_MEMBER_LIST:
          serviceImpl.memberList((io.etcd.jetcd.api.MemberListRequest) request,
              (io.grpc.stub.StreamObserver<io.etcd.jetcd.api.MemberListResponse>) responseObserver);
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
        default:
          throw new AssertionError();
      }
    }
  }

  private static final class VertxMethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ClusterVertxImplBase serviceImpl;
    private final int methodId;

    VertxMethodHandlers(ClusterVertxImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_MEMBER_ADD:
          serviceImpl.memberAdd((io.etcd.jetcd.api.MemberAddRequest) request,
              (io.vertx.core.Promise<io.etcd.jetcd.api.MemberAddResponse>) io.vertx.core.Promise.<io.etcd.jetcd.api.MemberAddResponse>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<io.etcd.jetcd.api.MemberAddResponse>) responseObserver).onNext(ar.result());
                  responseObserver.onCompleted();
                } else {
                  responseObserver.onError(ar.cause());
                }
              }));
          break;
        case METHODID_MEMBER_REMOVE:
          serviceImpl.memberRemove((io.etcd.jetcd.api.MemberRemoveRequest) request,
              (io.vertx.core.Promise<io.etcd.jetcd.api.MemberRemoveResponse>) io.vertx.core.Promise.<io.etcd.jetcd.api.MemberRemoveResponse>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<io.etcd.jetcd.api.MemberRemoveResponse>) responseObserver).onNext(ar.result());
                  responseObserver.onCompleted();
                } else {
                  responseObserver.onError(ar.cause());
                }
              }));
          break;
        case METHODID_MEMBER_UPDATE:
          serviceImpl.memberUpdate((io.etcd.jetcd.api.MemberUpdateRequest) request,
              (io.vertx.core.Promise<io.etcd.jetcd.api.MemberUpdateResponse>) io.vertx.core.Promise.<io.etcd.jetcd.api.MemberUpdateResponse>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<io.etcd.jetcd.api.MemberUpdateResponse>) responseObserver).onNext(ar.result());
                  responseObserver.onCompleted();
                } else {
                  responseObserver.onError(ar.cause());
                }
              }));
          break;
        case METHODID_MEMBER_LIST:
          serviceImpl.memberList((io.etcd.jetcd.api.MemberListRequest) request,
              (io.vertx.core.Promise<io.etcd.jetcd.api.MemberListResponse>) io.vertx.core.Promise.<io.etcd.jetcd.api.MemberListResponse>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<io.etcd.jetcd.api.MemberListResponse>) responseObserver).onNext(ar.result());
                  responseObserver.onCompleted();
                } else {
                  responseObserver.onError(ar.cause());
                }
              }));
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
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class ClusterBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ClusterBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.etcd.jetcd.api.JetcdProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Cluster");
    }
  }

  private static final class ClusterFileDescriptorSupplier
      extends ClusterBaseDescriptorSupplier {
    ClusterFileDescriptorSupplier() {}
  }

  private static final class ClusterMethodDescriptorSupplier
      extends ClusterBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ClusterMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ClusterGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ClusterFileDescriptorSupplier())
              .addMethod(getMemberAddMethod())
              .addMethod(getMemberRemoveMethod())
              .addMethod(getMemberUpdateMethod())
              .addMethod(getMemberListMethod())
              .build();
        }
      }
    }
    return result;
  }
}
