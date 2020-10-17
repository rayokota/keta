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
 * <pre>
 * The election service exposes client-side election facilities as a gRPC interface.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.20.0)",
    comments = "Source: election.proto")
public final class ElectionGrpc {

  private ElectionGrpc() {}

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

  public static final String SERVICE_NAME = "v3electionpb.Election";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.etcd.jetcd.api.CampaignRequest,
      io.etcd.jetcd.api.CampaignResponse> getCampaignMethod;

  public static io.grpc.MethodDescriptor<io.etcd.jetcd.api.CampaignRequest,
      io.etcd.jetcd.api.CampaignResponse> getCampaignMethod() {
    io.grpc.MethodDescriptor<io.etcd.jetcd.api.CampaignRequest, io.etcd.jetcd.api.CampaignResponse> getCampaignMethod;
    if ((getCampaignMethod = ElectionGrpc.getCampaignMethod) == null) {
      synchronized (ElectionGrpc.class) {
        if ((getCampaignMethod = ElectionGrpc.getCampaignMethod) == null) {
          ElectionGrpc.getCampaignMethod = getCampaignMethod = 
              io.grpc.MethodDescriptor.<io.etcd.jetcd.api.CampaignRequest, io.etcd.jetcd.api.CampaignResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "v3electionpb.Election", "Campaign"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.CampaignRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.CampaignResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ElectionMethodDescriptorSupplier("Campaign"))
                  .build();
          }
        }
     }
     return getCampaignMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.etcd.jetcd.api.ProclaimRequest,
      io.etcd.jetcd.api.ProclaimResponse> getProclaimMethod;

  public static io.grpc.MethodDescriptor<io.etcd.jetcd.api.ProclaimRequest,
      io.etcd.jetcd.api.ProclaimResponse> getProclaimMethod() {
    io.grpc.MethodDescriptor<io.etcd.jetcd.api.ProclaimRequest, io.etcd.jetcd.api.ProclaimResponse> getProclaimMethod;
    if ((getProclaimMethod = ElectionGrpc.getProclaimMethod) == null) {
      synchronized (ElectionGrpc.class) {
        if ((getProclaimMethod = ElectionGrpc.getProclaimMethod) == null) {
          ElectionGrpc.getProclaimMethod = getProclaimMethod = 
              io.grpc.MethodDescriptor.<io.etcd.jetcd.api.ProclaimRequest, io.etcd.jetcd.api.ProclaimResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "v3electionpb.Election", "Proclaim"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.ProclaimRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.ProclaimResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ElectionMethodDescriptorSupplier("Proclaim"))
                  .build();
          }
        }
     }
     return getProclaimMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.etcd.jetcd.api.LeaderRequest,
      io.etcd.jetcd.api.LeaderResponse> getLeaderMethod;

  public static io.grpc.MethodDescriptor<io.etcd.jetcd.api.LeaderRequest,
      io.etcd.jetcd.api.LeaderResponse> getLeaderMethod() {
    io.grpc.MethodDescriptor<io.etcd.jetcd.api.LeaderRequest, io.etcd.jetcd.api.LeaderResponse> getLeaderMethod;
    if ((getLeaderMethod = ElectionGrpc.getLeaderMethod) == null) {
      synchronized (ElectionGrpc.class) {
        if ((getLeaderMethod = ElectionGrpc.getLeaderMethod) == null) {
          ElectionGrpc.getLeaderMethod = getLeaderMethod = 
              io.grpc.MethodDescriptor.<io.etcd.jetcd.api.LeaderRequest, io.etcd.jetcd.api.LeaderResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "v3electionpb.Election", "Leader"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.LeaderRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.LeaderResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ElectionMethodDescriptorSupplier("Leader"))
                  .build();
          }
        }
     }
     return getLeaderMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.etcd.jetcd.api.LeaderRequest,
      io.etcd.jetcd.api.LeaderResponse> getObserveMethod;

  public static io.grpc.MethodDescriptor<io.etcd.jetcd.api.LeaderRequest,
      io.etcd.jetcd.api.LeaderResponse> getObserveMethod() {
    io.grpc.MethodDescriptor<io.etcd.jetcd.api.LeaderRequest, io.etcd.jetcd.api.LeaderResponse> getObserveMethod;
    if ((getObserveMethod = ElectionGrpc.getObserveMethod) == null) {
      synchronized (ElectionGrpc.class) {
        if ((getObserveMethod = ElectionGrpc.getObserveMethod) == null) {
          ElectionGrpc.getObserveMethod = getObserveMethod = 
              io.grpc.MethodDescriptor.<io.etcd.jetcd.api.LeaderRequest, io.etcd.jetcd.api.LeaderResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "v3electionpb.Election", "Observe"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.LeaderRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.LeaderResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ElectionMethodDescriptorSupplier("Observe"))
                  .build();
          }
        }
     }
     return getObserveMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.etcd.jetcd.api.ResignRequest,
      io.etcd.jetcd.api.ResignResponse> getResignMethod;

  public static io.grpc.MethodDescriptor<io.etcd.jetcd.api.ResignRequest,
      io.etcd.jetcd.api.ResignResponse> getResignMethod() {
    io.grpc.MethodDescriptor<io.etcd.jetcd.api.ResignRequest, io.etcd.jetcd.api.ResignResponse> getResignMethod;
    if ((getResignMethod = ElectionGrpc.getResignMethod) == null) {
      synchronized (ElectionGrpc.class) {
        if ((getResignMethod = ElectionGrpc.getResignMethod) == null) {
          ElectionGrpc.getResignMethod = getResignMethod = 
              io.grpc.MethodDescriptor.<io.etcd.jetcd.api.ResignRequest, io.etcd.jetcd.api.ResignResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "v3electionpb.Election", "Resign"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.ResignRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.ResignResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ElectionMethodDescriptorSupplier("Resign"))
                  .build();
          }
        }
     }
     return getResignMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ElectionStub newStub(io.grpc.Channel channel) {
    return new ElectionStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ElectionBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new ElectionBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ElectionFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new ElectionFutureStub(channel);
  }

  /**
   * Creates a new vertx stub that supports all call types for the service
   */
  public static ElectionVertxStub newVertxStub(io.grpc.Channel channel) {
    return new ElectionVertxStub(channel);
  }

  /**
   * <pre>
   * The election service exposes client-side election facilities as a gRPC interface.
   * </pre>
   */
  public static abstract class ElectionImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Campaign waits to acquire leadership in an election, returning a LeaderKey
     * representing the leadership if successful. The LeaderKey can then be used
     * to issue new values on the election, transactionally guard API requests on
     * leadership still being held, and resign from the election.
     * </pre>
     */
    public void campaign(io.etcd.jetcd.api.CampaignRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.CampaignResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getCampaignMethod(), responseObserver);
    }

    /**
     * <pre>
     * Proclaim updates the leader's posted value with a new value.
     * </pre>
     */
    public void proclaim(io.etcd.jetcd.api.ProclaimRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.ProclaimResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getProclaimMethod(), responseObserver);
    }

    /**
     * <pre>
     * Leader returns the current election proclamation, if any.
     * </pre>
     */
    public void leader(io.etcd.jetcd.api.LeaderRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.LeaderResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getLeaderMethod(), responseObserver);
    }

    /**
     * <pre>
     * Observe streams election proclamations in-order as made by the election's
     * elected leaders.
     * </pre>
     */
    public void observe(io.etcd.jetcd.api.LeaderRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.LeaderResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getObserveMethod(), responseObserver);
    }

    /**
     * <pre>
     * Resign releases election leadership so other campaigners may acquire
     * leadership on the election.
     * </pre>
     */
    public void resign(io.etcd.jetcd.api.ResignRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.ResignResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getResignMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getCampaignMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.etcd.jetcd.api.CampaignRequest,
                io.etcd.jetcd.api.CampaignResponse>(
                  this, METHODID_CAMPAIGN)))
          .addMethod(
            getProclaimMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.etcd.jetcd.api.ProclaimRequest,
                io.etcd.jetcd.api.ProclaimResponse>(
                  this, METHODID_PROCLAIM)))
          .addMethod(
            getLeaderMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.etcd.jetcd.api.LeaderRequest,
                io.etcd.jetcd.api.LeaderResponse>(
                  this, METHODID_LEADER)))
          .addMethod(
            getObserveMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                io.etcd.jetcd.api.LeaderRequest,
                io.etcd.jetcd.api.LeaderResponse>(
                  this, METHODID_OBSERVE)))
          .addMethod(
            getResignMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.etcd.jetcd.api.ResignRequest,
                io.etcd.jetcd.api.ResignResponse>(
                  this, METHODID_RESIGN)))
          .build();
    }
  }

  /**
   * <pre>
   * The election service exposes client-side election facilities as a gRPC interface.
   * </pre>
   */
  public static final class ElectionStub extends io.grpc.stub.AbstractStub<ElectionStub> {
    public ElectionStub(io.grpc.Channel channel) {
      super(channel);
    }

    public ElectionStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ElectionStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ElectionStub(channel, callOptions);
    }

    /**
     * <pre>
     * Campaign waits to acquire leadership in an election, returning a LeaderKey
     * representing the leadership if successful. The LeaderKey can then be used
     * to issue new values on the election, transactionally guard API requests on
     * leadership still being held, and resign from the election.
     * </pre>
     */
    public void campaign(io.etcd.jetcd.api.CampaignRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.CampaignResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCampaignMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Proclaim updates the leader's posted value with a new value.
     * </pre>
     */
    public void proclaim(io.etcd.jetcd.api.ProclaimRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.ProclaimResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getProclaimMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Leader returns the current election proclamation, if any.
     * </pre>
     */
    public void leader(io.etcd.jetcd.api.LeaderRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.LeaderResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getLeaderMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Observe streams election proclamations in-order as made by the election's
     * elected leaders.
     * </pre>
     */
    public void observe(io.etcd.jetcd.api.LeaderRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.LeaderResponse> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getObserveMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Resign releases election leadership so other campaigners may acquire
     * leadership on the election.
     * </pre>
     */
    public void resign(io.etcd.jetcd.api.ResignRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.ResignResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getResignMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * The election service exposes client-side election facilities as a gRPC interface.
   * </pre>
   */
  public static final class ElectionBlockingStub extends io.grpc.stub.AbstractStub<ElectionBlockingStub> {
    public ElectionBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    public ElectionBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ElectionBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ElectionBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Campaign waits to acquire leadership in an election, returning a LeaderKey
     * representing the leadership if successful. The LeaderKey can then be used
     * to issue new values on the election, transactionally guard API requests on
     * leadership still being held, and resign from the election.
     * </pre>
     */
    public io.etcd.jetcd.api.CampaignResponse campaign(io.etcd.jetcd.api.CampaignRequest request) {
      return blockingUnaryCall(
          getChannel(), getCampaignMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Proclaim updates the leader's posted value with a new value.
     * </pre>
     */
    public io.etcd.jetcd.api.ProclaimResponse proclaim(io.etcd.jetcd.api.ProclaimRequest request) {
      return blockingUnaryCall(
          getChannel(), getProclaimMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Leader returns the current election proclamation, if any.
     * </pre>
     */
    public io.etcd.jetcd.api.LeaderResponse leader(io.etcd.jetcd.api.LeaderRequest request) {
      return blockingUnaryCall(
          getChannel(), getLeaderMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Observe streams election proclamations in-order as made by the election's
     * elected leaders.
     * </pre>
     */
    public java.util.Iterator<io.etcd.jetcd.api.LeaderResponse> observe(
        io.etcd.jetcd.api.LeaderRequest request) {
      return blockingServerStreamingCall(
          getChannel(), getObserveMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Resign releases election leadership so other campaigners may acquire
     * leadership on the election.
     * </pre>
     */
    public io.etcd.jetcd.api.ResignResponse resign(io.etcd.jetcd.api.ResignRequest request) {
      return blockingUnaryCall(
          getChannel(), getResignMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * The election service exposes client-side election facilities as a gRPC interface.
   * </pre>
   */
  public static final class ElectionFutureStub extends io.grpc.stub.AbstractStub<ElectionFutureStub> {
    public ElectionFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    public ElectionFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ElectionFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ElectionFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Campaign waits to acquire leadership in an election, returning a LeaderKey
     * representing the leadership if successful. The LeaderKey can then be used
     * to issue new values on the election, transactionally guard API requests on
     * leadership still being held, and resign from the election.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.etcd.jetcd.api.CampaignResponse> campaign(
        io.etcd.jetcd.api.CampaignRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCampaignMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Proclaim updates the leader's posted value with a new value.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.etcd.jetcd.api.ProclaimResponse> proclaim(
        io.etcd.jetcd.api.ProclaimRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getProclaimMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Leader returns the current election proclamation, if any.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.etcd.jetcd.api.LeaderResponse> leader(
        io.etcd.jetcd.api.LeaderRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getLeaderMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Resign releases election leadership so other campaigners may acquire
     * leadership on the election.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.etcd.jetcd.api.ResignResponse> resign(
        io.etcd.jetcd.api.ResignRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getResignMethod(), getCallOptions()), request);
    }
  }

  /**
   * <pre>
   * The election service exposes client-side election facilities as a gRPC interface.
   * </pre>
   */
  public static abstract class ElectionVertxImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Campaign waits to acquire leadership in an election, returning a LeaderKey
     * representing the leadership if successful. The LeaderKey can then be used
     * to issue new values on the election, transactionally guard API requests on
     * leadership still being held, and resign from the election.
     * </pre>
     */
    public void campaign(io.etcd.jetcd.api.CampaignRequest request,
        io.vertx.core.Promise<io.etcd.jetcd.api.CampaignResponse> response) {
      asyncUnimplementedUnaryCall(getCampaignMethod(), ElectionGrpc.toObserver(response));
    }

    /**
     * <pre>
     * Proclaim updates the leader's posted value with a new value.
     * </pre>
     */
    public void proclaim(io.etcd.jetcd.api.ProclaimRequest request,
        io.vertx.core.Promise<io.etcd.jetcd.api.ProclaimResponse> response) {
      asyncUnimplementedUnaryCall(getProclaimMethod(), ElectionGrpc.toObserver(response));
    }

    /**
     * <pre>
     * Leader returns the current election proclamation, if any.
     * </pre>
     */
    public void leader(io.etcd.jetcd.api.LeaderRequest request,
        io.vertx.core.Promise<io.etcd.jetcd.api.LeaderResponse> response) {
      asyncUnimplementedUnaryCall(getLeaderMethod(), ElectionGrpc.toObserver(response));
    }

    /**
     * <pre>
     * Observe streams election proclamations in-order as made by the election's
     * elected leaders.
     * </pre>
     */
    public void observe(io.etcd.jetcd.api.LeaderRequest request,
        io.vertx.grpc.GrpcWriteStream<io.etcd.jetcd.api.LeaderResponse> response) {
      asyncUnimplementedUnaryCall(getObserveMethod(), response.writeObserver());
    }

    /**
     * <pre>
     * Resign releases election leadership so other campaigners may acquire
     * leadership on the election.
     * </pre>
     */
    public void resign(io.etcd.jetcd.api.ResignRequest request,
        io.vertx.core.Promise<io.etcd.jetcd.api.ResignResponse> response) {
      asyncUnimplementedUnaryCall(getResignMethod(), ElectionGrpc.toObserver(response));
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getCampaignMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                io.etcd.jetcd.api.CampaignRequest,
                io.etcd.jetcd.api.CampaignResponse>(
                  this, METHODID_CAMPAIGN)))
          .addMethod(
            getProclaimMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                io.etcd.jetcd.api.ProclaimRequest,
                io.etcd.jetcd.api.ProclaimResponse>(
                  this, METHODID_PROCLAIM)))
          .addMethod(
            getLeaderMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                io.etcd.jetcd.api.LeaderRequest,
                io.etcd.jetcd.api.LeaderResponse>(
                  this, METHODID_LEADER)))
          .addMethod(
            getObserveMethod(),
            asyncServerStreamingCall(
              new VertxMethodHandlers<
                io.etcd.jetcd.api.LeaderRequest,
                io.etcd.jetcd.api.LeaderResponse>(
                  this, METHODID_OBSERVE)))
          .addMethod(
            getResignMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                io.etcd.jetcd.api.ResignRequest,
                io.etcd.jetcd.api.ResignResponse>(
                  this, METHODID_RESIGN)))
          .build();
    }
  }

  /**
   * <pre>
   * The election service exposes client-side election facilities as a gRPC interface.
   * </pre>
   */
  public static final class ElectionVertxStub extends io.grpc.stub.AbstractStub<ElectionVertxStub> {
    public ElectionVertxStub(io.grpc.Channel channel) {
      super(channel);
    }

    public ElectionVertxStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ElectionVertxStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ElectionVertxStub(channel, callOptions);
    }

    /**
     * <pre>
     * Campaign waits to acquire leadership in an election, returning a LeaderKey
     * representing the leadership if successful. The LeaderKey can then be used
     * to issue new values on the election, transactionally guard API requests on
     * leadership still being held, and resign from the election.
     * </pre>
     */
    public void campaign(io.etcd.jetcd.api.CampaignRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<io.etcd.jetcd.api.CampaignResponse>> response) {
      asyncUnaryCall(
          getChannel().newCall(getCampaignMethod(), getCallOptions()), request, ElectionGrpc.toObserver(response));
    }

    /**
     * <pre>
     * Proclaim updates the leader's posted value with a new value.
     * </pre>
     */
    public void proclaim(io.etcd.jetcd.api.ProclaimRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<io.etcd.jetcd.api.ProclaimResponse>> response) {
      asyncUnaryCall(
          getChannel().newCall(getProclaimMethod(), getCallOptions()), request, ElectionGrpc.toObserver(response));
    }

    /**
     * <pre>
     * Leader returns the current election proclamation, if any.
     * </pre>
     */
    public void leader(io.etcd.jetcd.api.LeaderRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<io.etcd.jetcd.api.LeaderResponse>> response) {
      asyncUnaryCall(
          getChannel().newCall(getLeaderMethod(), getCallOptions()), request, ElectionGrpc.toObserver(response));
    }

    /**
     * <pre>
     * Observe streams election proclamations in-order as made by the election's
     * elected leaders.
     * </pre>
     */
    public void observe(io.etcd.jetcd.api.LeaderRequest request,
        io.vertx.core.Handler<io.vertx.grpc.GrpcReadStream<io.etcd.jetcd.api.LeaderResponse>> handler) {
      final io.vertx.grpc.GrpcReadStream<io.etcd.jetcd.api.LeaderResponse> readStream =
          io.vertx.grpc.GrpcReadStream.<io.etcd.jetcd.api.LeaderResponse>create();

      handler.handle(readStream);
      asyncServerStreamingCall(
          getChannel().newCall(getObserveMethod(), getCallOptions()), request, readStream.readObserver());
    }

    /**
     * <pre>
     * Resign releases election leadership so other campaigners may acquire
     * leadership on the election.
     * </pre>
     */
    public void resign(io.etcd.jetcd.api.ResignRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<io.etcd.jetcd.api.ResignResponse>> response) {
      asyncUnaryCall(
          getChannel().newCall(getResignMethod(), getCallOptions()), request, ElectionGrpc.toObserver(response));
    }
  }

  private static final int METHODID_CAMPAIGN = 0;
  private static final int METHODID_PROCLAIM = 1;
  private static final int METHODID_LEADER = 2;
  private static final int METHODID_OBSERVE = 3;
  private static final int METHODID_RESIGN = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ElectionImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ElectionImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CAMPAIGN:
          serviceImpl.campaign((io.etcd.jetcd.api.CampaignRequest) request,
              (io.grpc.stub.StreamObserver<io.etcd.jetcd.api.CampaignResponse>) responseObserver);
          break;
        case METHODID_PROCLAIM:
          serviceImpl.proclaim((io.etcd.jetcd.api.ProclaimRequest) request,
              (io.grpc.stub.StreamObserver<io.etcd.jetcd.api.ProclaimResponse>) responseObserver);
          break;
        case METHODID_LEADER:
          serviceImpl.leader((io.etcd.jetcd.api.LeaderRequest) request,
              (io.grpc.stub.StreamObserver<io.etcd.jetcd.api.LeaderResponse>) responseObserver);
          break;
        case METHODID_OBSERVE:
          serviceImpl.observe((io.etcd.jetcd.api.LeaderRequest) request,
              (io.grpc.stub.StreamObserver<io.etcd.jetcd.api.LeaderResponse>) responseObserver);
          break;
        case METHODID_RESIGN:
          serviceImpl.resign((io.etcd.jetcd.api.ResignRequest) request,
              (io.grpc.stub.StreamObserver<io.etcd.jetcd.api.ResignResponse>) responseObserver);
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
    private final ElectionVertxImplBase serviceImpl;
    private final int methodId;

    VertxMethodHandlers(ElectionVertxImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CAMPAIGN:
          serviceImpl.campaign((io.etcd.jetcd.api.CampaignRequest) request,
              (io.vertx.core.Promise<io.etcd.jetcd.api.CampaignResponse>) io.vertx.core.Promise.<io.etcd.jetcd.api.CampaignResponse>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<io.etcd.jetcd.api.CampaignResponse>) responseObserver).onNext(ar.result());
                  responseObserver.onCompleted();
                } else {
                  responseObserver.onError(ar.cause());
                }
              }));
          break;
        case METHODID_PROCLAIM:
          serviceImpl.proclaim((io.etcd.jetcd.api.ProclaimRequest) request,
              (io.vertx.core.Promise<io.etcd.jetcd.api.ProclaimResponse>) io.vertx.core.Promise.<io.etcd.jetcd.api.ProclaimResponse>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<io.etcd.jetcd.api.ProclaimResponse>) responseObserver).onNext(ar.result());
                  responseObserver.onCompleted();
                } else {
                  responseObserver.onError(ar.cause());
                }
              }));
          break;
        case METHODID_LEADER:
          serviceImpl.leader((io.etcd.jetcd.api.LeaderRequest) request,
              (io.vertx.core.Promise<io.etcd.jetcd.api.LeaderResponse>) io.vertx.core.Promise.<io.etcd.jetcd.api.LeaderResponse>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<io.etcd.jetcd.api.LeaderResponse>) responseObserver).onNext(ar.result());
                  responseObserver.onCompleted();
                } else {
                  responseObserver.onError(ar.cause());
                }
              }));
          break;
        case METHODID_OBSERVE:
          serviceImpl.observe((io.etcd.jetcd.api.LeaderRequest) request,
              (io.vertx.grpc.GrpcWriteStream<io.etcd.jetcd.api.LeaderResponse>) io.vertx.grpc.GrpcWriteStream.create(responseObserver));
          break;
        case METHODID_RESIGN:
          serviceImpl.resign((io.etcd.jetcd.api.ResignRequest) request,
              (io.vertx.core.Promise<io.etcd.jetcd.api.ResignResponse>) io.vertx.core.Promise.<io.etcd.jetcd.api.ResignResponse>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<io.etcd.jetcd.api.ResignResponse>) responseObserver).onNext(ar.result());
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

  private static abstract class ElectionBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ElectionBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.etcd.jetcd.api.ElectionOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Election");
    }
  }

  private static final class ElectionFileDescriptorSupplier
      extends ElectionBaseDescriptorSupplier {
    ElectionFileDescriptorSupplier() {}
  }

  private static final class ElectionMethodDescriptorSupplier
      extends ElectionBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ElectionMethodDescriptorSupplier(String methodName) {
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
      synchronized (ElectionGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ElectionFileDescriptorSupplier())
              .addMethod(getCampaignMethod())
              .addMethod(getProclaimMethod())
              .addMethod(getLeaderMethod())
              .addMethod(getObserveMethod())
              .addMethod(getResignMethod())
              .build();
        }
      }
    }
    return result;
  }
}
