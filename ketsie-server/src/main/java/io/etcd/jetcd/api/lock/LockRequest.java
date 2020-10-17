// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: lock.proto

package io.etcd.jetcd.api.lock;

/**
 * Protobuf type {@code v3lockpb.LockRequest}
 */
public  final class LockRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:v3lockpb.LockRequest)
    LockRequestOrBuilder {
  // Use LockRequest.newBuilder() to construct.
  private LockRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private LockRequest() {
    name_ = com.google.protobuf.ByteString.EMPTY;
    lease_ = 0L;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
  }
  private LockRequest(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    int mutable_bitField0_ = 0;
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          default: {
            if (!input.skipField(tag)) {
              done = true;
            }
            break;
          }
          case 10: {

            name_ = input.readBytes();
            break;
          }
          case 16: {

            lease_ = input.readInt64();
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.etcd.jetcd.api.lock.JetcdProto.internal_static_v3lockpb_LockRequest_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.etcd.jetcd.api.lock.JetcdProto.internal_static_v3lockpb_LockRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.etcd.jetcd.api.lock.LockRequest.class, io.etcd.jetcd.api.lock.LockRequest.Builder.class);
  }

  public static final int NAME_FIELD_NUMBER = 1;
  private com.google.protobuf.ByteString name_;
  /**
   * <pre>
   * name is the identifier for the distributed shared lock to be acquired.
   * </pre>
   *
   * <code>bytes name = 1;</code>
   */
  public com.google.protobuf.ByteString getName() {
    return name_;
  }

  public static final int LEASE_FIELD_NUMBER = 2;
  private long lease_;
  /**
   * <pre>
   * lease is the ID of the lease that will be attached to ownership of the
   * lock. If the lease expires or is revoked and currently holds the lock,
   * the lock is automatically released. Calls to Lock with the same lease will
   * be treated as a single acquistion; locking twice with the same lease is a
   * no-op.
   * </pre>
   *
   * <code>int64 lease = 2;</code>
   */
  public long getLease() {
    return lease_;
  }

  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!name_.isEmpty()) {
      output.writeBytes(1, name_);
    }
    if (lease_ != 0L) {
      output.writeInt64(2, lease_);
    }
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!name_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream
        .computeBytesSize(1, name_);
    }
    if (lease_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(2, lease_);
    }
    memoizedSize = size;
    return size;
  }

  private static final long serialVersionUID = 0L;
  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof io.etcd.jetcd.api.lock.LockRequest)) {
      return super.equals(obj);
    }
    io.etcd.jetcd.api.lock.LockRequest other = (io.etcd.jetcd.api.lock.LockRequest) obj;

    boolean result = true;
    result = result && getName()
        .equals(other.getName());
    result = result && (getLease()
        == other.getLease());
    return result;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + NAME_FIELD_NUMBER;
    hash = (53 * hash) + getName().hashCode();
    hash = (37 * hash) + LEASE_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getLease());
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.etcd.jetcd.api.lock.LockRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.etcd.jetcd.api.lock.LockRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.etcd.jetcd.api.lock.LockRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.etcd.jetcd.api.lock.LockRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.etcd.jetcd.api.lock.LockRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.etcd.jetcd.api.lock.LockRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.etcd.jetcd.api.lock.LockRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static io.etcd.jetcd.api.lock.LockRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.etcd.jetcd.api.lock.LockRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.etcd.jetcd.api.lock.LockRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(io.etcd.jetcd.api.lock.LockRequest prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code v3lockpb.LockRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:v3lockpb.LockRequest)
      io.etcd.jetcd.api.lock.LockRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.etcd.jetcd.api.lock.JetcdProto.internal_static_v3lockpb_LockRequest_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.etcd.jetcd.api.lock.JetcdProto.internal_static_v3lockpb_LockRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.etcd.jetcd.api.lock.LockRequest.class, io.etcd.jetcd.api.lock.LockRequest.Builder.class);
    }

    // Construct using io.etcd.jetcd.api.lock.LockRequest.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    public Builder clear() {
      super.clear();
      name_ = com.google.protobuf.ByteString.EMPTY;

      lease_ = 0L;

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.etcd.jetcd.api.lock.JetcdProto.internal_static_v3lockpb_LockRequest_descriptor;
    }

    public io.etcd.jetcd.api.lock.LockRequest getDefaultInstanceForType() {
      return io.etcd.jetcd.api.lock.LockRequest.getDefaultInstance();
    }

    public io.etcd.jetcd.api.lock.LockRequest build() {
      io.etcd.jetcd.api.lock.LockRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public io.etcd.jetcd.api.lock.LockRequest buildPartial() {
      io.etcd.jetcd.api.lock.LockRequest result = new io.etcd.jetcd.api.lock.LockRequest(this);
      result.name_ = name_;
      result.lease_ = lease_;
      onBuilt();
      return result;
    }

    public Builder clone() {
      return (Builder) super.clone();
    }
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return (Builder) super.setField(field, value);
    }
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return (Builder) super.clearField(field);
    }
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return (Builder) super.clearOneof(oneof);
    }
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, Object value) {
      return (Builder) super.setRepeatedField(field, index, value);
    }
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return (Builder) super.addRepeatedField(field, value);
    }
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.etcd.jetcd.api.lock.LockRequest) {
        return mergeFrom((io.etcd.jetcd.api.lock.LockRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.etcd.jetcd.api.lock.LockRequest other) {
      if (other == io.etcd.jetcd.api.lock.LockRequest.getDefaultInstance()) return this;
      if (other.getName() != com.google.protobuf.ByteString.EMPTY) {
        setName(other.getName());
      }
      if (other.getLease() != 0L) {
        setLease(other.getLease());
      }
      onChanged();
      return this;
    }

    public final boolean isInitialized() {
      return true;
    }

    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      io.etcd.jetcd.api.lock.LockRequest parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (io.etcd.jetcd.api.lock.LockRequest) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private com.google.protobuf.ByteString name_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <pre>
     * name is the identifier for the distributed shared lock to be acquired.
     * </pre>
     *
     * <code>bytes name = 1;</code>
     */
    public com.google.protobuf.ByteString getName() {
      return name_;
    }
    /**
     * <pre>
     * name is the identifier for the distributed shared lock to be acquired.
     * </pre>
     *
     * <code>bytes name = 1;</code>
     */
    public Builder setName(com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      name_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * name is the identifier for the distributed shared lock to be acquired.
     * </pre>
     *
     * <code>bytes name = 1;</code>
     */
    public Builder clearName() {
      
      name_ = getDefaultInstance().getName();
      onChanged();
      return this;
    }

    private long lease_ ;
    /**
     * <pre>
     * lease is the ID of the lease that will be attached to ownership of the
     * lock. If the lease expires or is revoked and currently holds the lock,
     * the lock is automatically released. Calls to Lock with the same lease will
     * be treated as a single acquistion; locking twice with the same lease is a
     * no-op.
     * </pre>
     *
     * <code>int64 lease = 2;</code>
     */
    public long getLease() {
      return lease_;
    }
    /**
     * <pre>
     * lease is the ID of the lease that will be attached to ownership of the
     * lock. If the lease expires or is revoked and currently holds the lock,
     * the lock is automatically released. Calls to Lock with the same lease will
     * be treated as a single acquistion; locking twice with the same lease is a
     * no-op.
     * </pre>
     *
     * <code>int64 lease = 2;</code>
     */
    public Builder setLease(long value) {
      
      lease_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * lease is the ID of the lease that will be attached to ownership of the
     * lock. If the lease expires or is revoked and currently holds the lock,
     * the lock is automatically released. Calls to Lock with the same lease will
     * be treated as a single acquistion; locking twice with the same lease is a
     * no-op.
     * </pre>
     *
     * <code>int64 lease = 2;</code>
     */
    public Builder clearLease() {
      
      lease_ = 0L;
      onChanged();
      return this;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }


    // @@protoc_insertion_point(builder_scope:v3lockpb.LockRequest)
  }

  // @@protoc_insertion_point(class_scope:v3lockpb.LockRequest)
  private static final io.etcd.jetcd.api.lock.LockRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.etcd.jetcd.api.lock.LockRequest();
  }

  public static io.etcd.jetcd.api.lock.LockRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<LockRequest>
      PARSER = new com.google.protobuf.AbstractParser<LockRequest>() {
    public LockRequest parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return new LockRequest(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<LockRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<LockRequest> getParserForType() {
    return PARSER;
  }

  public io.etcd.jetcd.api.lock.LockRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

