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
public final class KVGrpc {

  private KVGrpc() {}

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

  public static final String SERVICE_NAME = "etcdserverpb.KV";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.etcd.jetcd.api.RangeRequest,
      io.etcd.jetcd.api.RangeResponse> getRangeMethod;

  public static io.grpc.MethodDescriptor<io.etcd.jetcd.api.RangeRequest,
      io.etcd.jetcd.api.RangeResponse> getRangeMethod() {
    io.grpc.MethodDescriptor<io.etcd.jetcd.api.RangeRequest, io.etcd.jetcd.api.RangeResponse> getRangeMethod;
    if ((getRangeMethod = KVGrpc.getRangeMethod) == null) {
      synchronized (KVGrpc.class) {
        if ((getRangeMethod = KVGrpc.getRangeMethod) == null) {
          KVGrpc.getRangeMethod = getRangeMethod = 
              io.grpc.MethodDescriptor.<io.etcd.jetcd.api.RangeRequest, io.etcd.jetcd.api.RangeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "etcdserverpb.KV", "Range"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.RangeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.RangeResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new KVMethodDescriptorSupplier("Range"))
                  .build();
          }
        }
     }
     return getRangeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.etcd.jetcd.api.PutRequest,
      io.etcd.jetcd.api.PutResponse> getPutMethod;

  public static io.grpc.MethodDescriptor<io.etcd.jetcd.api.PutRequest,
      io.etcd.jetcd.api.PutResponse> getPutMethod() {
    io.grpc.MethodDescriptor<io.etcd.jetcd.api.PutRequest, io.etcd.jetcd.api.PutResponse> getPutMethod;
    if ((getPutMethod = KVGrpc.getPutMethod) == null) {
      synchronized (KVGrpc.class) {
        if ((getPutMethod = KVGrpc.getPutMethod) == null) {
          KVGrpc.getPutMethod = getPutMethod = 
              io.grpc.MethodDescriptor.<io.etcd.jetcd.api.PutRequest, io.etcd.jetcd.api.PutResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "etcdserverpb.KV", "Put"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.PutRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.PutResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new KVMethodDescriptorSupplier("Put"))
                  .build();
          }
        }
     }
     return getPutMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.etcd.jetcd.api.DeleteRangeRequest,
      io.etcd.jetcd.api.DeleteRangeResponse> getDeleteRangeMethod;

  public static io.grpc.MethodDescriptor<io.etcd.jetcd.api.DeleteRangeRequest,
      io.etcd.jetcd.api.DeleteRangeResponse> getDeleteRangeMethod() {
    io.grpc.MethodDescriptor<io.etcd.jetcd.api.DeleteRangeRequest, io.etcd.jetcd.api.DeleteRangeResponse> getDeleteRangeMethod;
    if ((getDeleteRangeMethod = KVGrpc.getDeleteRangeMethod) == null) {
      synchronized (KVGrpc.class) {
        if ((getDeleteRangeMethod = KVGrpc.getDeleteRangeMethod) == null) {
          KVGrpc.getDeleteRangeMethod = getDeleteRangeMethod = 
              io.grpc.MethodDescriptor.<io.etcd.jetcd.api.DeleteRangeRequest, io.etcd.jetcd.api.DeleteRangeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "etcdserverpb.KV", "DeleteRange"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.DeleteRangeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.DeleteRangeResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new KVMethodDescriptorSupplier("DeleteRange"))
                  .build();
          }
        }
     }
     return getDeleteRangeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.etcd.jetcd.api.TxnRequest,
      io.etcd.jetcd.api.TxnResponse> getTxnMethod;

  public static io.grpc.MethodDescriptor<io.etcd.jetcd.api.TxnRequest,
      io.etcd.jetcd.api.TxnResponse> getTxnMethod() {
    io.grpc.MethodDescriptor<io.etcd.jetcd.api.TxnRequest, io.etcd.jetcd.api.TxnResponse> getTxnMethod;
    if ((getTxnMethod = KVGrpc.getTxnMethod) == null) {
      synchronized (KVGrpc.class) {
        if ((getTxnMethod = KVGrpc.getTxnMethod) == null) {
          KVGrpc.getTxnMethod = getTxnMethod = 
              io.grpc.MethodDescriptor.<io.etcd.jetcd.api.TxnRequest, io.etcd.jetcd.api.TxnResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "etcdserverpb.KV", "Txn"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.TxnRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.TxnResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new KVMethodDescriptorSupplier("Txn"))
                  .build();
          }
        }
     }
     return getTxnMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.etcd.jetcd.api.CompactionRequest,
      io.etcd.jetcd.api.CompactionResponse> getCompactMethod;

  public static io.grpc.MethodDescriptor<io.etcd.jetcd.api.CompactionRequest,
      io.etcd.jetcd.api.CompactionResponse> getCompactMethod() {
    io.grpc.MethodDescriptor<io.etcd.jetcd.api.CompactionRequest, io.etcd.jetcd.api.CompactionResponse> getCompactMethod;
    if ((getCompactMethod = KVGrpc.getCompactMethod) == null) {
      synchronized (KVGrpc.class) {
        if ((getCompactMethod = KVGrpc.getCompactMethod) == null) {
          KVGrpc.getCompactMethod = getCompactMethod = 
              io.grpc.MethodDescriptor.<io.etcd.jetcd.api.CompactionRequest, io.etcd.jetcd.api.CompactionResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "etcdserverpb.KV", "Compact"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.CompactionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.CompactionResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new KVMethodDescriptorSupplier("Compact"))
                  .build();
          }
        }
     }
     return getCompactMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static KVStub newStub(io.grpc.Channel channel) {
    return new KVStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static KVBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new KVBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static KVFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new KVFutureStub(channel);
  }

  /**
   * Creates a new vertx stub that supports all call types for the service
   */
  public static KVVertxStub newVertxStub(io.grpc.Channel channel) {
    return new KVVertxStub(channel);
  }

  /**
   */
  public static abstract class KVImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Range gets the keys in the range from the key-value store.
     * </pre>
     */
    public void range(io.etcd.jetcd.api.RangeRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.RangeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getRangeMethod(), responseObserver);
    }

    /**
     * <pre>
     * Put puts the given key into the key-value store.
     * A put request increments the revision of the key-value store
     * and generates one event in the event history.
     * </pre>
     */
    public void put(io.etcd.jetcd.api.PutRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.PutResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getPutMethod(), responseObserver);
    }

    /**
     * <pre>
     * DeleteRange deletes the given range from the key-value store.
     * A delete request increments the revision of the key-value store
     * and generates a delete event in the event history for every deleted key.
     * </pre>
     */
    public void deleteRange(io.etcd.jetcd.api.DeleteRangeRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.DeleteRangeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getDeleteRangeMethod(), responseObserver);
    }

    /**
     * <pre>
     * Txn processes multiple requests in a single transaction.
     * A txn request increments the revision of the key-value store
     * and generates events with the same revision for every completed request.
     * It is not allowed to modify the same key several times within one txn.
     * </pre>
     */
    public void txn(io.etcd.jetcd.api.TxnRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.TxnResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getTxnMethod(), responseObserver);
    }

    /**
     * <pre>
     * Compact compacts the event history in the etcd key-value store. The key-value
     * store should be periodically compacted or the event history will continue to grow
     * indefinitely.
     * </pre>
     */
    public void compact(io.etcd.jetcd.api.CompactionRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.CompactionResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getCompactMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRangeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.etcd.jetcd.api.RangeRequest,
                io.etcd.jetcd.api.RangeResponse>(
                  this, METHODID_RANGE)))
          .addMethod(
            getPutMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.etcd.jetcd.api.PutRequest,
                io.etcd.jetcd.api.PutResponse>(
                  this, METHODID_PUT)))
          .addMethod(
            getDeleteRangeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.etcd.jetcd.api.DeleteRangeRequest,
                io.etcd.jetcd.api.DeleteRangeResponse>(
                  this, METHODID_DELETE_RANGE)))
          .addMethod(
            getTxnMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.etcd.jetcd.api.TxnRequest,
                io.etcd.jetcd.api.TxnResponse>(
                  this, METHODID_TXN)))
          .addMethod(
            getCompactMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.etcd.jetcd.api.CompactionRequest,
                io.etcd.jetcd.api.CompactionResponse>(
                  this, METHODID_COMPACT)))
          .build();
    }
  }

  /**
   */
  public static final class KVStub extends io.grpc.stub.AbstractStub<KVStub> {
    public KVStub(io.grpc.Channel channel) {
      super(channel);
    }

    public KVStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected KVStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new KVStub(channel, callOptions);
    }

    /**
     * <pre>
     * Range gets the keys in the range from the key-value store.
     * </pre>
     */
    public void range(io.etcd.jetcd.api.RangeRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.RangeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRangeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Put puts the given key into the key-value store.
     * A put request increments the revision of the key-value store
     * and generates one event in the event history.
     * </pre>
     */
    public void put(io.etcd.jetcd.api.PutRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.PutResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * DeleteRange deletes the given range from the key-value store.
     * A delete request increments the revision of the key-value store
     * and generates a delete event in the event history for every deleted key.
     * </pre>
     */
    public void deleteRange(io.etcd.jetcd.api.DeleteRangeRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.DeleteRangeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDeleteRangeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Txn processes multiple requests in a single transaction.
     * A txn request increments the revision of the key-value store
     * and generates events with the same revision for every completed request.
     * It is not allowed to modify the same key several times within one txn.
     * </pre>
     */
    public void txn(io.etcd.jetcd.api.TxnRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.TxnResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getTxnMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Compact compacts the event history in the etcd key-value store. The key-value
     * store should be periodically compacted or the event history will continue to grow
     * indefinitely.
     * </pre>
     */
    public void compact(io.etcd.jetcd.api.CompactionRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.CompactionResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCompactMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class KVBlockingStub extends io.grpc.stub.AbstractStub<KVBlockingStub> {
    public KVBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    public KVBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected KVBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new KVBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Range gets the keys in the range from the key-value store.
     * </pre>
     */
    public io.etcd.jetcd.api.RangeResponse range(io.etcd.jetcd.api.RangeRequest request) {
      return blockingUnaryCall(
          getChannel(), getRangeMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Put puts the given key into the key-value store.
     * A put request increments the revision of the key-value store
     * and generates one event in the event history.
     * </pre>
     */
    public io.etcd.jetcd.api.PutResponse put(io.etcd.jetcd.api.PutRequest request) {
      return blockingUnaryCall(
          getChannel(), getPutMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * DeleteRange deletes the given range from the key-value store.
     * A delete request increments the revision of the key-value store
     * and generates a delete event in the event history for every deleted key.
     * </pre>
     */
    public io.etcd.jetcd.api.DeleteRangeResponse deleteRange(io.etcd.jetcd.api.DeleteRangeRequest request) {
      return blockingUnaryCall(
          getChannel(), getDeleteRangeMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Txn processes multiple requests in a single transaction.
     * A txn request increments the revision of the key-value store
     * and generates events with the same revision for every completed request.
     * It is not allowed to modify the same key several times within one txn.
     * </pre>
     */
    public io.etcd.jetcd.api.TxnResponse txn(io.etcd.jetcd.api.TxnRequest request) {
      return blockingUnaryCall(
          getChannel(), getTxnMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Compact compacts the event history in the etcd key-value store. The key-value
     * store should be periodically compacted or the event history will continue to grow
     * indefinitely.
     * </pre>
     */
    public io.etcd.jetcd.api.CompactionResponse compact(io.etcd.jetcd.api.CompactionRequest request) {
      return blockingUnaryCall(
          getChannel(), getCompactMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class KVFutureStub extends io.grpc.stub.AbstractStub<KVFutureStub> {
    public KVFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    public KVFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected KVFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new KVFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Range gets the keys in the range from the key-value store.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.etcd.jetcd.api.RangeResponse> range(
        io.etcd.jetcd.api.RangeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getRangeMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Put puts the given key into the key-value store.
     * A put request increments the revision of the key-value store
     * and generates one event in the event history.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.etcd.jetcd.api.PutResponse> put(
        io.etcd.jetcd.api.PutRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getPutMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * DeleteRange deletes the given range from the key-value store.
     * A delete request increments the revision of the key-value store
     * and generates a delete event in the event history for every deleted key.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.etcd.jetcd.api.DeleteRangeResponse> deleteRange(
        io.etcd.jetcd.api.DeleteRangeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getDeleteRangeMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Txn processes multiple requests in a single transaction.
     * A txn request increments the revision of the key-value store
     * and generates events with the same revision for every completed request.
     * It is not allowed to modify the same key several times within one txn.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.etcd.jetcd.api.TxnResponse> txn(
        io.etcd.jetcd.api.TxnRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getTxnMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Compact compacts the event history in the etcd key-value store. The key-value
     * store should be periodically compacted or the event history will continue to grow
     * indefinitely.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.etcd.jetcd.api.CompactionResponse> compact(
        io.etcd.jetcd.api.CompactionRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCompactMethod(), getCallOptions()), request);
    }
  }

  /**
   */
  public static abstract class KVVertxImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Range gets the keys in the range from the key-value store.
     * </pre>
     */
    public void range(io.etcd.jetcd.api.RangeRequest request,
        io.vertx.core.Promise<io.etcd.jetcd.api.RangeResponse> response) {
      asyncUnimplementedUnaryCall(getRangeMethod(), KVGrpc.toObserver(response));
    }

    /**
     * <pre>
     * Put puts the given key into the key-value store.
     * A put request increments the revision of the key-value store
     * and generates one event in the event history.
     * </pre>
     */
    public void put(io.etcd.jetcd.api.PutRequest request,
        io.vertx.core.Promise<io.etcd.jetcd.api.PutResponse> response) {
      asyncUnimplementedUnaryCall(getPutMethod(), KVGrpc.toObserver(response));
    }

    /**
     * <pre>
     * DeleteRange deletes the given range from the key-value store.
     * A delete request increments the revision of the key-value store
     * and generates a delete event in the event history for every deleted key.
     * </pre>
     */
    public void deleteRange(io.etcd.jetcd.api.DeleteRangeRequest request,
        io.vertx.core.Promise<io.etcd.jetcd.api.DeleteRangeResponse> response) {
      asyncUnimplementedUnaryCall(getDeleteRangeMethod(), KVGrpc.toObserver(response));
    }

    /**
     * <pre>
     * Txn processes multiple requests in a single transaction.
     * A txn request increments the revision of the key-value store
     * and generates events with the same revision for every completed request.
     * It is not allowed to modify the same key several times within one txn.
     * </pre>
     */
    public void txn(io.etcd.jetcd.api.TxnRequest request,
        io.vertx.core.Promise<io.etcd.jetcd.api.TxnResponse> response) {
      asyncUnimplementedUnaryCall(getTxnMethod(), KVGrpc.toObserver(response));
    }

    /**
     * <pre>
     * Compact compacts the event history in the etcd key-value store. The key-value
     * store should be periodically compacted or the event history will continue to grow
     * indefinitely.
     * </pre>
     */
    public void compact(io.etcd.jetcd.api.CompactionRequest request,
        io.vertx.core.Promise<io.etcd.jetcd.api.CompactionResponse> response) {
      asyncUnimplementedUnaryCall(getCompactMethod(), KVGrpc.toObserver(response));
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRangeMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                io.etcd.jetcd.api.RangeRequest,
                io.etcd.jetcd.api.RangeResponse>(
                  this, METHODID_RANGE)))
          .addMethod(
            getPutMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                io.etcd.jetcd.api.PutRequest,
                io.etcd.jetcd.api.PutResponse>(
                  this, METHODID_PUT)))
          .addMethod(
            getDeleteRangeMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                io.etcd.jetcd.api.DeleteRangeRequest,
                io.etcd.jetcd.api.DeleteRangeResponse>(
                  this, METHODID_DELETE_RANGE)))
          .addMethod(
            getTxnMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                io.etcd.jetcd.api.TxnRequest,
                io.etcd.jetcd.api.TxnResponse>(
                  this, METHODID_TXN)))
          .addMethod(
            getCompactMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                io.etcd.jetcd.api.CompactionRequest,
                io.etcd.jetcd.api.CompactionResponse>(
                  this, METHODID_COMPACT)))
          .build();
    }
  }

  /**
   */
  public static final class KVVertxStub extends io.grpc.stub.AbstractStub<KVVertxStub> {
    public KVVertxStub(io.grpc.Channel channel) {
      super(channel);
    }

    public KVVertxStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected KVVertxStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new KVVertxStub(channel, callOptions);
    }

    /**
     * <pre>
     * Range gets the keys in the range from the key-value store.
     * </pre>
     */
    public void range(io.etcd.jetcd.api.RangeRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<io.etcd.jetcd.api.RangeResponse>> response) {
      asyncUnaryCall(
          getChannel().newCall(getRangeMethod(), getCallOptions()), request, KVGrpc.toObserver(response));
    }

    /**
     * <pre>
     * Put puts the given key into the key-value store.
     * A put request increments the revision of the key-value store
     * and generates one event in the event history.
     * </pre>
     */
    public void put(io.etcd.jetcd.api.PutRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<io.etcd.jetcd.api.PutResponse>> response) {
      asyncUnaryCall(
          getChannel().newCall(getPutMethod(), getCallOptions()), request, KVGrpc.toObserver(response));
    }

    /**
     * <pre>
     * DeleteRange deletes the given range from the key-value store.
     * A delete request increments the revision of the key-value store
     * and generates a delete event in the event history for every deleted key.
     * </pre>
     */
    public void deleteRange(io.etcd.jetcd.api.DeleteRangeRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<io.etcd.jetcd.api.DeleteRangeResponse>> response) {
      asyncUnaryCall(
          getChannel().newCall(getDeleteRangeMethod(), getCallOptions()), request, KVGrpc.toObserver(response));
    }

    /**
     * <pre>
     * Txn processes multiple requests in a single transaction.
     * A txn request increments the revision of the key-value store
     * and generates events with the same revision for every completed request.
     * It is not allowed to modify the same key several times within one txn.
     * </pre>
     */
    public void txn(io.etcd.jetcd.api.TxnRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<io.etcd.jetcd.api.TxnResponse>> response) {
      asyncUnaryCall(
          getChannel().newCall(getTxnMethod(), getCallOptions()), request, KVGrpc.toObserver(response));
    }

    /**
     * <pre>
     * Compact compacts the event history in the etcd key-value store. The key-value
     * store should be periodically compacted or the event history will continue to grow
     * indefinitely.
     * </pre>
     */
    public void compact(io.etcd.jetcd.api.CompactionRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<io.etcd.jetcd.api.CompactionResponse>> response) {
      asyncUnaryCall(
          getChannel().newCall(getCompactMethod(), getCallOptions()), request, KVGrpc.toObserver(response));
    }
  }

  private static final int METHODID_RANGE = 0;
  private static final int METHODID_PUT = 1;
  private static final int METHODID_DELETE_RANGE = 2;
  private static final int METHODID_TXN = 3;
  private static final int METHODID_COMPACT = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final KVImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(KVImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_RANGE:
          serviceImpl.range((io.etcd.jetcd.api.RangeRequest) request,
              (io.grpc.stub.StreamObserver<io.etcd.jetcd.api.RangeResponse>) responseObserver);
          break;
        case METHODID_PUT:
          serviceImpl.put((io.etcd.jetcd.api.PutRequest) request,
              (io.grpc.stub.StreamObserver<io.etcd.jetcd.api.PutResponse>) responseObserver);
          break;
        case METHODID_DELETE_RANGE:
          serviceImpl.deleteRange((io.etcd.jetcd.api.DeleteRangeRequest) request,
              (io.grpc.stub.StreamObserver<io.etcd.jetcd.api.DeleteRangeResponse>) responseObserver);
          break;
        case METHODID_TXN:
          serviceImpl.txn((io.etcd.jetcd.api.TxnRequest) request,
              (io.grpc.stub.StreamObserver<io.etcd.jetcd.api.TxnResponse>) responseObserver);
          break;
        case METHODID_COMPACT:
          serviceImpl.compact((io.etcd.jetcd.api.CompactionRequest) request,
              (io.grpc.stub.StreamObserver<io.etcd.jetcd.api.CompactionResponse>) responseObserver);
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
    private final KVVertxImplBase serviceImpl;
    private final int methodId;

    VertxMethodHandlers(KVVertxImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_RANGE:
          serviceImpl.range((io.etcd.jetcd.api.RangeRequest) request,
              (io.vertx.core.Promise<io.etcd.jetcd.api.RangeResponse>) io.vertx.core.Promise.<io.etcd.jetcd.api.RangeResponse>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<io.etcd.jetcd.api.RangeResponse>) responseObserver).onNext(ar.result());
                  responseObserver.onCompleted();
                } else {
                  responseObserver.onError(ar.cause());
                }
              }));
          break;
        case METHODID_PUT:
          serviceImpl.put((io.etcd.jetcd.api.PutRequest) request,
              (io.vertx.core.Promise<io.etcd.jetcd.api.PutResponse>) io.vertx.core.Promise.<io.etcd.jetcd.api.PutResponse>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<io.etcd.jetcd.api.PutResponse>) responseObserver).onNext(ar.result());
                  responseObserver.onCompleted();
                } else {
                  responseObserver.onError(ar.cause());
                }
              }));
          break;
        case METHODID_DELETE_RANGE:
          serviceImpl.deleteRange((io.etcd.jetcd.api.DeleteRangeRequest) request,
              (io.vertx.core.Promise<io.etcd.jetcd.api.DeleteRangeResponse>) io.vertx.core.Promise.<io.etcd.jetcd.api.DeleteRangeResponse>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<io.etcd.jetcd.api.DeleteRangeResponse>) responseObserver).onNext(ar.result());
                  responseObserver.onCompleted();
                } else {
                  responseObserver.onError(ar.cause());
                }
              }));
          break;
        case METHODID_TXN:
          serviceImpl.txn((io.etcd.jetcd.api.TxnRequest) request,
              (io.vertx.core.Promise<io.etcd.jetcd.api.TxnResponse>) io.vertx.core.Promise.<io.etcd.jetcd.api.TxnResponse>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<io.etcd.jetcd.api.TxnResponse>) responseObserver).onNext(ar.result());
                  responseObserver.onCompleted();
                } else {
                  responseObserver.onError(ar.cause());
                }
              }));
          break;
        case METHODID_COMPACT:
          serviceImpl.compact((io.etcd.jetcd.api.CompactionRequest) request,
              (io.vertx.core.Promise<io.etcd.jetcd.api.CompactionResponse>) io.vertx.core.Promise.<io.etcd.jetcd.api.CompactionResponse>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<io.etcd.jetcd.api.CompactionResponse>) responseObserver).onNext(ar.result());
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

  private static abstract class KVBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    KVBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.etcd.jetcd.api.JetcdProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("KV");
    }
  }

  private static final class KVFileDescriptorSupplier
      extends KVBaseDescriptorSupplier {
    KVFileDescriptorSupplier() {}
  }

  private static final class KVMethodDescriptorSupplier
      extends KVBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    KVMethodDescriptorSupplier(String methodName) {
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
      synchronized (KVGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new KVFileDescriptorSupplier())
              .addMethod(getRangeMethod())
              .addMethod(getPutMethod())
              .addMethod(getDeleteRangeMethod())
              .addMethod(getTxnMethod())
              .addMethod(getCompactMethod())
              .build();
        }
      }
    }
    return result;
  }
}
