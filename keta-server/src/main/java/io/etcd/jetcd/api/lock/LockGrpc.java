package io.etcd.jetcd.api.lock;

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
 * The lock service exposes client-side locking facilities as a gRPC interface.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.20.0)",
    comments = "Source: lock.proto")
public final class LockGrpc {

  private LockGrpc() {}

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

  public static final String SERVICE_NAME = "v3lockpb.Lock";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.etcd.jetcd.api.lock.LockRequest,
      io.etcd.jetcd.api.lock.LockResponse> getLockMethod;

  public static io.grpc.MethodDescriptor<io.etcd.jetcd.api.lock.LockRequest,
      io.etcd.jetcd.api.lock.LockResponse> getLockMethod() {
    io.grpc.MethodDescriptor<io.etcd.jetcd.api.lock.LockRequest, io.etcd.jetcd.api.lock.LockResponse> getLockMethod;
    if ((getLockMethod = LockGrpc.getLockMethod) == null) {
      synchronized (LockGrpc.class) {
        if ((getLockMethod = LockGrpc.getLockMethod) == null) {
          LockGrpc.getLockMethod = getLockMethod = 
              io.grpc.MethodDescriptor.<io.etcd.jetcd.api.lock.LockRequest, io.etcd.jetcd.api.lock.LockResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "v3lockpb.Lock", "Lock"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.lock.LockRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.lock.LockResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new LockMethodDescriptorSupplier("Lock"))
                  .build();
          }
        }
     }
     return getLockMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.etcd.jetcd.api.lock.UnlockRequest,
      io.etcd.jetcd.api.lock.UnlockResponse> getUnlockMethod;

  public static io.grpc.MethodDescriptor<io.etcd.jetcd.api.lock.UnlockRequest,
      io.etcd.jetcd.api.lock.UnlockResponse> getUnlockMethod() {
    io.grpc.MethodDescriptor<io.etcd.jetcd.api.lock.UnlockRequest, io.etcd.jetcd.api.lock.UnlockResponse> getUnlockMethod;
    if ((getUnlockMethod = LockGrpc.getUnlockMethod) == null) {
      synchronized (LockGrpc.class) {
        if ((getUnlockMethod = LockGrpc.getUnlockMethod) == null) {
          LockGrpc.getUnlockMethod = getUnlockMethod = 
              io.grpc.MethodDescriptor.<io.etcd.jetcd.api.lock.UnlockRequest, io.etcd.jetcd.api.lock.UnlockResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "v3lockpb.Lock", "Unlock"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.lock.UnlockRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.etcd.jetcd.api.lock.UnlockResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new LockMethodDescriptorSupplier("Unlock"))
                  .build();
          }
        }
     }
     return getUnlockMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static LockStub newStub(io.grpc.Channel channel) {
    return new LockStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static LockBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new LockBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static LockFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new LockFutureStub(channel);
  }

  /**
   * Creates a new vertx stub that supports all call types for the service
   */
  public static LockVertxStub newVertxStub(io.grpc.Channel channel) {
    return new LockVertxStub(channel);
  }

  /**
   * <pre>
   * The lock service exposes client-side locking facilities as a gRPC interface.
   * </pre>
   */
  public static abstract class LockImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Lock acquires a distributed shared lock on a given named lock.
     * On success, it will return a unique key that exists so long as the
     * lock is held by the caller. This key can be used in conjunction with
     * transactions to safely ensure updates to etcd only occur while holding
     * lock ownership. The lock is held until Unlock is called on the key or the
     * lease associate with the owner expires.
     * </pre>
     */
    public void lock(io.etcd.jetcd.api.lock.LockRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.lock.LockResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getLockMethod(), responseObserver);
    }

    /**
     * <pre>
     * Unlock takes a key returned by Lock and releases the hold on lock. The
     * next Lock caller waiting for the lock will then be woken up and given
     * ownership of the lock.
     * </pre>
     */
    public void unlock(io.etcd.jetcd.api.lock.UnlockRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.lock.UnlockResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getUnlockMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getLockMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.etcd.jetcd.api.lock.LockRequest,
                io.etcd.jetcd.api.lock.LockResponse>(
                  this, METHODID_LOCK)))
          .addMethod(
            getUnlockMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.etcd.jetcd.api.lock.UnlockRequest,
                io.etcd.jetcd.api.lock.UnlockResponse>(
                  this, METHODID_UNLOCK)))
          .build();
    }
  }

  /**
   * <pre>
   * The lock service exposes client-side locking facilities as a gRPC interface.
   * </pre>
   */
  public static final class LockStub extends io.grpc.stub.AbstractStub<LockStub> {
    public LockStub(io.grpc.Channel channel) {
      super(channel);
    }

    public LockStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LockStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new LockStub(channel, callOptions);
    }

    /**
     * <pre>
     * Lock acquires a distributed shared lock on a given named lock.
     * On success, it will return a unique key that exists so long as the
     * lock is held by the caller. This key can be used in conjunction with
     * transactions to safely ensure updates to etcd only occur while holding
     * lock ownership. The lock is held until Unlock is called on the key or the
     * lease associate with the owner expires.
     * </pre>
     */
    public void lock(io.etcd.jetcd.api.lock.LockRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.lock.LockResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getLockMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Unlock takes a key returned by Lock and releases the hold on lock. The
     * next Lock caller waiting for the lock will then be woken up and given
     * ownership of the lock.
     * </pre>
     */
    public void unlock(io.etcd.jetcd.api.lock.UnlockRequest request,
        io.grpc.stub.StreamObserver<io.etcd.jetcd.api.lock.UnlockResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getUnlockMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * The lock service exposes client-side locking facilities as a gRPC interface.
   * </pre>
   */
  public static final class LockBlockingStub extends io.grpc.stub.AbstractStub<LockBlockingStub> {
    public LockBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    public LockBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LockBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new LockBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Lock acquires a distributed shared lock on a given named lock.
     * On success, it will return a unique key that exists so long as the
     * lock is held by the caller. This key can be used in conjunction with
     * transactions to safely ensure updates to etcd only occur while holding
     * lock ownership. The lock is held until Unlock is called on the key or the
     * lease associate with the owner expires.
     * </pre>
     */
    public io.etcd.jetcd.api.lock.LockResponse lock(io.etcd.jetcd.api.lock.LockRequest request) {
      return blockingUnaryCall(
          getChannel(), getLockMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Unlock takes a key returned by Lock and releases the hold on lock. The
     * next Lock caller waiting for the lock will then be woken up and given
     * ownership of the lock.
     * </pre>
     */
    public io.etcd.jetcd.api.lock.UnlockResponse unlock(io.etcd.jetcd.api.lock.UnlockRequest request) {
      return blockingUnaryCall(
          getChannel(), getUnlockMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * The lock service exposes client-side locking facilities as a gRPC interface.
   * </pre>
   */
  public static final class LockFutureStub extends io.grpc.stub.AbstractStub<LockFutureStub> {
    public LockFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    public LockFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LockFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new LockFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Lock acquires a distributed shared lock on a given named lock.
     * On success, it will return a unique key that exists so long as the
     * lock is held by the caller. This key can be used in conjunction with
     * transactions to safely ensure updates to etcd only occur while holding
     * lock ownership. The lock is held until Unlock is called on the key or the
     * lease associate with the owner expires.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.etcd.jetcd.api.lock.LockResponse> lock(
        io.etcd.jetcd.api.lock.LockRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getLockMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Unlock takes a key returned by Lock and releases the hold on lock. The
     * next Lock caller waiting for the lock will then be woken up and given
     * ownership of the lock.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.etcd.jetcd.api.lock.UnlockResponse> unlock(
        io.etcd.jetcd.api.lock.UnlockRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getUnlockMethod(), getCallOptions()), request);
    }
  }

  /**
   * <pre>
   * The lock service exposes client-side locking facilities as a gRPC interface.
   * </pre>
   */
  public static abstract class LockVertxImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Lock acquires a distributed shared lock on a given named lock.
     * On success, it will return a unique key that exists so long as the
     * lock is held by the caller. This key can be used in conjunction with
     * transactions to safely ensure updates to etcd only occur while holding
     * lock ownership. The lock is held until Unlock is called on the key or the
     * lease associate with the owner expires.
     * </pre>
     */
    public void lock(io.etcd.jetcd.api.lock.LockRequest request,
        io.vertx.core.Promise<io.etcd.jetcd.api.lock.LockResponse> response) {
      asyncUnimplementedUnaryCall(getLockMethod(), LockGrpc.toObserver(response));
    }

    /**
     * <pre>
     * Unlock takes a key returned by Lock and releases the hold on lock. The
     * next Lock caller waiting for the lock will then be woken up and given
     * ownership of the lock.
     * </pre>
     */
    public void unlock(io.etcd.jetcd.api.lock.UnlockRequest request,
        io.vertx.core.Promise<io.etcd.jetcd.api.lock.UnlockResponse> response) {
      asyncUnimplementedUnaryCall(getUnlockMethod(), LockGrpc.toObserver(response));
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getLockMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                io.etcd.jetcd.api.lock.LockRequest,
                io.etcd.jetcd.api.lock.LockResponse>(
                  this, METHODID_LOCK)))
          .addMethod(
            getUnlockMethod(),
            asyncUnaryCall(
              new VertxMethodHandlers<
                io.etcd.jetcd.api.lock.UnlockRequest,
                io.etcd.jetcd.api.lock.UnlockResponse>(
                  this, METHODID_UNLOCK)))
          .build();
    }
  }

  /**
   * <pre>
   * The lock service exposes client-side locking facilities as a gRPC interface.
   * </pre>
   */
  public static final class LockVertxStub extends io.grpc.stub.AbstractStub<LockVertxStub> {
    public LockVertxStub(io.grpc.Channel channel) {
      super(channel);
    }

    public LockVertxStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LockVertxStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new LockVertxStub(channel, callOptions);
    }

    /**
     * <pre>
     * Lock acquires a distributed shared lock on a given named lock.
     * On success, it will return a unique key that exists so long as the
     * lock is held by the caller. This key can be used in conjunction with
     * transactions to safely ensure updates to etcd only occur while holding
     * lock ownership. The lock is held until Unlock is called on the key or the
     * lease associate with the owner expires.
     * </pre>
     */
    public void lock(io.etcd.jetcd.api.lock.LockRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<io.etcd.jetcd.api.lock.LockResponse>> response) {
      asyncUnaryCall(
          getChannel().newCall(getLockMethod(), getCallOptions()), request, LockGrpc.toObserver(response));
    }

    /**
     * <pre>
     * Unlock takes a key returned by Lock and releases the hold on lock. The
     * next Lock caller waiting for the lock will then be woken up and given
     * ownership of the lock.
     * </pre>
     */
    public void unlock(io.etcd.jetcd.api.lock.UnlockRequest request,
        io.vertx.core.Handler<io.vertx.core.AsyncResult<io.etcd.jetcd.api.lock.UnlockResponse>> response) {
      asyncUnaryCall(
          getChannel().newCall(getUnlockMethod(), getCallOptions()), request, LockGrpc.toObserver(response));
    }
  }

  private static final int METHODID_LOCK = 0;
  private static final int METHODID_UNLOCK = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final LockImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(LockImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_LOCK:
          serviceImpl.lock((io.etcd.jetcd.api.lock.LockRequest) request,
              (io.grpc.stub.StreamObserver<io.etcd.jetcd.api.lock.LockResponse>) responseObserver);
          break;
        case METHODID_UNLOCK:
          serviceImpl.unlock((io.etcd.jetcd.api.lock.UnlockRequest) request,
              (io.grpc.stub.StreamObserver<io.etcd.jetcd.api.lock.UnlockResponse>) responseObserver);
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
    private final LockVertxImplBase serviceImpl;
    private final int methodId;

    VertxMethodHandlers(LockVertxImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_LOCK:
          serviceImpl.lock((io.etcd.jetcd.api.lock.LockRequest) request,
              (io.vertx.core.Promise<io.etcd.jetcd.api.lock.LockResponse>) io.vertx.core.Promise.<io.etcd.jetcd.api.lock.LockResponse>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<io.etcd.jetcd.api.lock.LockResponse>) responseObserver).onNext(ar.result());
                  responseObserver.onCompleted();
                } else {
                  responseObserver.onError(ar.cause());
                }
              }));
          break;
        case METHODID_UNLOCK:
          serviceImpl.unlock((io.etcd.jetcd.api.lock.UnlockRequest) request,
              (io.vertx.core.Promise<io.etcd.jetcd.api.lock.UnlockResponse>) io.vertx.core.Promise.<io.etcd.jetcd.api.lock.UnlockResponse>promise().future().setHandler(ar -> {
                if (ar.succeeded()) {
                  ((io.grpc.stub.StreamObserver<io.etcd.jetcd.api.lock.UnlockResponse>) responseObserver).onNext(ar.result());
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

  private static abstract class LockBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    LockBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.etcd.jetcd.api.lock.JetcdProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Lock");
    }
  }

  private static final class LockFileDescriptorSupplier
      extends LockBaseDescriptorSupplier {
    LockFileDescriptorSupplier() {}
  }

  private static final class LockMethodDescriptorSupplier
      extends LockBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    LockMethodDescriptorSupplier(String methodName) {
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
      synchronized (LockGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new LockFileDescriptorSupplier())
              .addMethod(getLockMethod())
              .addMethod(getUnlockMethod())
              .build();
        }
      }
    }
    return result;
  }
}
