// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: rpc.proto

package io.etcd.jetcd.api;

/**
 * Protobuf type {@code etcdserverpb.DeleteRangeResponse}
 */
public  final class DeleteRangeResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:etcdserverpb.DeleteRangeResponse)
    DeleteRangeResponseOrBuilder {
  // Use DeleteRangeResponse.newBuilder() to construct.
  private DeleteRangeResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private DeleteRangeResponse() {
    deleted_ = 0L;
    prevKvs_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
  }
  private DeleteRangeResponse(
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
            io.etcd.jetcd.api.ResponseHeader.Builder subBuilder = null;
            if (header_ != null) {
              subBuilder = header_.toBuilder();
            }
            header_ = input.readMessage(io.etcd.jetcd.api.ResponseHeader.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(header_);
              header_ = subBuilder.buildPartial();
            }

            break;
          }
          case 16: {

            deleted_ = input.readInt64();
            break;
          }
          case 26: {
            if (!((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
              prevKvs_ = new java.util.ArrayList<io.etcd.jetcd.api.KeyValue>();
              mutable_bitField0_ |= 0x00000004;
            }
            prevKvs_.add(
                input.readMessage(io.etcd.jetcd.api.KeyValue.parser(), extensionRegistry));
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
      if (((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
        prevKvs_ = java.util.Collections.unmodifiableList(prevKvs_);
      }
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return io.etcd.jetcd.api.JetcdProto.internal_static_etcdserverpb_DeleteRangeResponse_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.etcd.jetcd.api.JetcdProto.internal_static_etcdserverpb_DeleteRangeResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.etcd.jetcd.api.DeleteRangeResponse.class, io.etcd.jetcd.api.DeleteRangeResponse.Builder.class);
  }

  private int bitField0_;
  public static final int HEADER_FIELD_NUMBER = 1;
  private io.etcd.jetcd.api.ResponseHeader header_;
  /**
   * <code>.etcdserverpb.ResponseHeader header = 1;</code>
   */
  public boolean hasHeader() {
    return header_ != null;
  }
  /**
   * <code>.etcdserverpb.ResponseHeader header = 1;</code>
   */
  public io.etcd.jetcd.api.ResponseHeader getHeader() {
    return header_ == null ? io.etcd.jetcd.api.ResponseHeader.getDefaultInstance() : header_;
  }
  /**
   * <code>.etcdserverpb.ResponseHeader header = 1;</code>
   */
  public io.etcd.jetcd.api.ResponseHeaderOrBuilder getHeaderOrBuilder() {
    return getHeader();
  }

  public static final int DELETED_FIELD_NUMBER = 2;
  private long deleted_;
  /**
   * <pre>
   * deleted is the number of keys deleted by the delete range request.
   * </pre>
   *
   * <code>int64 deleted = 2;</code>
   */
  public long getDeleted() {
    return deleted_;
  }

  public static final int PREV_KVS_FIELD_NUMBER = 3;
  private java.util.List<io.etcd.jetcd.api.KeyValue> prevKvs_;
  /**
   * <pre>
   * if prev_kv is set in the request, the previous key-value pairs will be returned.
   * </pre>
   *
   * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
   */
  public java.util.List<io.etcd.jetcd.api.KeyValue> getPrevKvsList() {
    return prevKvs_;
  }
  /**
   * <pre>
   * if prev_kv is set in the request, the previous key-value pairs will be returned.
   * </pre>
   *
   * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
   */
  public java.util.List<? extends io.etcd.jetcd.api.KeyValueOrBuilder> 
      getPrevKvsOrBuilderList() {
    return prevKvs_;
  }
  /**
   * <pre>
   * if prev_kv is set in the request, the previous key-value pairs will be returned.
   * </pre>
   *
   * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
   */
  public int getPrevKvsCount() {
    return prevKvs_.size();
  }
  /**
   * <pre>
   * if prev_kv is set in the request, the previous key-value pairs will be returned.
   * </pre>
   *
   * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
   */
  public io.etcd.jetcd.api.KeyValue getPrevKvs(int index) {
    return prevKvs_.get(index);
  }
  /**
   * <pre>
   * if prev_kv is set in the request, the previous key-value pairs will be returned.
   * </pre>
   *
   * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
   */
  public io.etcd.jetcd.api.KeyValueOrBuilder getPrevKvsOrBuilder(
      int index) {
    return prevKvs_.get(index);
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
    if (header_ != null) {
      output.writeMessage(1, getHeader());
    }
    if (deleted_ != 0L) {
      output.writeInt64(2, deleted_);
    }
    for (int i = 0; i < prevKvs_.size(); i++) {
      output.writeMessage(3, prevKvs_.get(i));
    }
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (header_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getHeader());
    }
    if (deleted_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(2, deleted_);
    }
    for (int i = 0; i < prevKvs_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(3, prevKvs_.get(i));
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
    if (!(obj instanceof io.etcd.jetcd.api.DeleteRangeResponse)) {
      return super.equals(obj);
    }
    io.etcd.jetcd.api.DeleteRangeResponse other = (io.etcd.jetcd.api.DeleteRangeResponse) obj;

    boolean result = true;
    result = result && (hasHeader() == other.hasHeader());
    if (hasHeader()) {
      result = result && getHeader()
          .equals(other.getHeader());
    }
    result = result && (getDeleted()
        == other.getDeleted());
    result = result && getPrevKvsList()
        .equals(other.getPrevKvsList());
    return result;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    if (hasHeader()) {
      hash = (37 * hash) + HEADER_FIELD_NUMBER;
      hash = (53 * hash) + getHeader().hashCode();
    }
    hash = (37 * hash) + DELETED_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getDeleted());
    if (getPrevKvsCount() > 0) {
      hash = (37 * hash) + PREV_KVS_FIELD_NUMBER;
      hash = (53 * hash) + getPrevKvsList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.etcd.jetcd.api.DeleteRangeResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.etcd.jetcd.api.DeleteRangeResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.etcd.jetcd.api.DeleteRangeResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.etcd.jetcd.api.DeleteRangeResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.etcd.jetcd.api.DeleteRangeResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.etcd.jetcd.api.DeleteRangeResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.etcd.jetcd.api.DeleteRangeResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static io.etcd.jetcd.api.DeleteRangeResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.etcd.jetcd.api.DeleteRangeResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.etcd.jetcd.api.DeleteRangeResponse parseFrom(
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
  public static Builder newBuilder(io.etcd.jetcd.api.DeleteRangeResponse prototype) {
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
   * Protobuf type {@code etcdserverpb.DeleteRangeResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:etcdserverpb.DeleteRangeResponse)
      io.etcd.jetcd.api.DeleteRangeResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.etcd.jetcd.api.JetcdProto.internal_static_etcdserverpb_DeleteRangeResponse_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.etcd.jetcd.api.JetcdProto.internal_static_etcdserverpb_DeleteRangeResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.etcd.jetcd.api.DeleteRangeResponse.class, io.etcd.jetcd.api.DeleteRangeResponse.Builder.class);
    }

    // Construct using io.etcd.jetcd.api.DeleteRangeResponse.newBuilder()
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
        getPrevKvsFieldBuilder();
      }
    }
    public Builder clear() {
      super.clear();
      if (headerBuilder_ == null) {
        header_ = null;
      } else {
        header_ = null;
        headerBuilder_ = null;
      }
      deleted_ = 0L;

      if (prevKvsBuilder_ == null) {
        prevKvs_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000004);
      } else {
        prevKvsBuilder_.clear();
      }
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.etcd.jetcd.api.JetcdProto.internal_static_etcdserverpb_DeleteRangeResponse_descriptor;
    }

    public io.etcd.jetcd.api.DeleteRangeResponse getDefaultInstanceForType() {
      return io.etcd.jetcd.api.DeleteRangeResponse.getDefaultInstance();
    }

    public io.etcd.jetcd.api.DeleteRangeResponse build() {
      io.etcd.jetcd.api.DeleteRangeResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public io.etcd.jetcd.api.DeleteRangeResponse buildPartial() {
      io.etcd.jetcd.api.DeleteRangeResponse result = new io.etcd.jetcd.api.DeleteRangeResponse(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (headerBuilder_ == null) {
        result.header_ = header_;
      } else {
        result.header_ = headerBuilder_.build();
      }
      result.deleted_ = deleted_;
      if (prevKvsBuilder_ == null) {
        if (((bitField0_ & 0x00000004) == 0x00000004)) {
          prevKvs_ = java.util.Collections.unmodifiableList(prevKvs_);
          bitField0_ = (bitField0_ & ~0x00000004);
        }
        result.prevKvs_ = prevKvs_;
      } else {
        result.prevKvs_ = prevKvsBuilder_.build();
      }
      result.bitField0_ = to_bitField0_;
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
      if (other instanceof io.etcd.jetcd.api.DeleteRangeResponse) {
        return mergeFrom((io.etcd.jetcd.api.DeleteRangeResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.etcd.jetcd.api.DeleteRangeResponse other) {
      if (other == io.etcd.jetcd.api.DeleteRangeResponse.getDefaultInstance()) return this;
      if (other.hasHeader()) {
        mergeHeader(other.getHeader());
      }
      if (other.getDeleted() != 0L) {
        setDeleted(other.getDeleted());
      }
      if (prevKvsBuilder_ == null) {
        if (!other.prevKvs_.isEmpty()) {
          if (prevKvs_.isEmpty()) {
            prevKvs_ = other.prevKvs_;
            bitField0_ = (bitField0_ & ~0x00000004);
          } else {
            ensurePrevKvsIsMutable();
            prevKvs_.addAll(other.prevKvs_);
          }
          onChanged();
        }
      } else {
        if (!other.prevKvs_.isEmpty()) {
          if (prevKvsBuilder_.isEmpty()) {
            prevKvsBuilder_.dispose();
            prevKvsBuilder_ = null;
            prevKvs_ = other.prevKvs_;
            bitField0_ = (bitField0_ & ~0x00000004);
            prevKvsBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getPrevKvsFieldBuilder() : null;
          } else {
            prevKvsBuilder_.addAllMessages(other.prevKvs_);
          }
        }
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
      io.etcd.jetcd.api.DeleteRangeResponse parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (io.etcd.jetcd.api.DeleteRangeResponse) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private io.etcd.jetcd.api.ResponseHeader header_ = null;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.etcd.jetcd.api.ResponseHeader, io.etcd.jetcd.api.ResponseHeader.Builder, io.etcd.jetcd.api.ResponseHeaderOrBuilder> headerBuilder_;
    /**
     * <code>.etcdserverpb.ResponseHeader header = 1;</code>
     */
    public boolean hasHeader() {
      return headerBuilder_ != null || header_ != null;
    }
    /**
     * <code>.etcdserverpb.ResponseHeader header = 1;</code>
     */
    public io.etcd.jetcd.api.ResponseHeader getHeader() {
      if (headerBuilder_ == null) {
        return header_ == null ? io.etcd.jetcd.api.ResponseHeader.getDefaultInstance() : header_;
      } else {
        return headerBuilder_.getMessage();
      }
    }
    /**
     * <code>.etcdserverpb.ResponseHeader header = 1;</code>
     */
    public Builder setHeader(io.etcd.jetcd.api.ResponseHeader value) {
      if (headerBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        header_ = value;
        onChanged();
      } else {
        headerBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.etcdserverpb.ResponseHeader header = 1;</code>
     */
    public Builder setHeader(
        io.etcd.jetcd.api.ResponseHeader.Builder builderForValue) {
      if (headerBuilder_ == null) {
        header_ = builderForValue.build();
        onChanged();
      } else {
        headerBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.etcdserverpb.ResponseHeader header = 1;</code>
     */
    public Builder mergeHeader(io.etcd.jetcd.api.ResponseHeader value) {
      if (headerBuilder_ == null) {
        if (header_ != null) {
          header_ =
            io.etcd.jetcd.api.ResponseHeader.newBuilder(header_).mergeFrom(value).buildPartial();
        } else {
          header_ = value;
        }
        onChanged();
      } else {
        headerBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.etcdserverpb.ResponseHeader header = 1;</code>
     */
    public Builder clearHeader() {
      if (headerBuilder_ == null) {
        header_ = null;
        onChanged();
      } else {
        header_ = null;
        headerBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.etcdserverpb.ResponseHeader header = 1;</code>
     */
    public io.etcd.jetcd.api.ResponseHeader.Builder getHeaderBuilder() {
      
      onChanged();
      return getHeaderFieldBuilder().getBuilder();
    }
    /**
     * <code>.etcdserverpb.ResponseHeader header = 1;</code>
     */
    public io.etcd.jetcd.api.ResponseHeaderOrBuilder getHeaderOrBuilder() {
      if (headerBuilder_ != null) {
        return headerBuilder_.getMessageOrBuilder();
      } else {
        return header_ == null ?
            io.etcd.jetcd.api.ResponseHeader.getDefaultInstance() : header_;
      }
    }
    /**
     * <code>.etcdserverpb.ResponseHeader header = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.etcd.jetcd.api.ResponseHeader, io.etcd.jetcd.api.ResponseHeader.Builder, io.etcd.jetcd.api.ResponseHeaderOrBuilder> 
        getHeaderFieldBuilder() {
      if (headerBuilder_ == null) {
        headerBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.etcd.jetcd.api.ResponseHeader, io.etcd.jetcd.api.ResponseHeader.Builder, io.etcd.jetcd.api.ResponseHeaderOrBuilder>(
                getHeader(),
                getParentForChildren(),
                isClean());
        header_ = null;
      }
      return headerBuilder_;
    }

    private long deleted_ ;
    /**
     * <pre>
     * deleted is the number of keys deleted by the delete range request.
     * </pre>
     *
     * <code>int64 deleted = 2;</code>
     */
    public long getDeleted() {
      return deleted_;
    }
    /**
     * <pre>
     * deleted is the number of keys deleted by the delete range request.
     * </pre>
     *
     * <code>int64 deleted = 2;</code>
     */
    public Builder setDeleted(long value) {
      
      deleted_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * deleted is the number of keys deleted by the delete range request.
     * </pre>
     *
     * <code>int64 deleted = 2;</code>
     */
    public Builder clearDeleted() {
      
      deleted_ = 0L;
      onChanged();
      return this;
    }

    private java.util.List<io.etcd.jetcd.api.KeyValue> prevKvs_ =
      java.util.Collections.emptyList();
    private void ensurePrevKvsIsMutable() {
      if (!((bitField0_ & 0x00000004) == 0x00000004)) {
        prevKvs_ = new java.util.ArrayList<io.etcd.jetcd.api.KeyValue>(prevKvs_);
        bitField0_ |= 0x00000004;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        io.etcd.jetcd.api.KeyValue, io.etcd.jetcd.api.KeyValue.Builder, io.etcd.jetcd.api.KeyValueOrBuilder> prevKvsBuilder_;

    /**
     * <pre>
     * if prev_kv is set in the request, the previous key-value pairs will be returned.
     * </pre>
     *
     * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
     */
    public java.util.List<io.etcd.jetcd.api.KeyValue> getPrevKvsList() {
      if (prevKvsBuilder_ == null) {
        return java.util.Collections.unmodifiableList(prevKvs_);
      } else {
        return prevKvsBuilder_.getMessageList();
      }
    }
    /**
     * <pre>
     * if prev_kv is set in the request, the previous key-value pairs will be returned.
     * </pre>
     *
     * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
     */
    public int getPrevKvsCount() {
      if (prevKvsBuilder_ == null) {
        return prevKvs_.size();
      } else {
        return prevKvsBuilder_.getCount();
      }
    }
    /**
     * <pre>
     * if prev_kv is set in the request, the previous key-value pairs will be returned.
     * </pre>
     *
     * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
     */
    public io.etcd.jetcd.api.KeyValue getPrevKvs(int index) {
      if (prevKvsBuilder_ == null) {
        return prevKvs_.get(index);
      } else {
        return prevKvsBuilder_.getMessage(index);
      }
    }
    /**
     * <pre>
     * if prev_kv is set in the request, the previous key-value pairs will be returned.
     * </pre>
     *
     * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
     */
    public Builder setPrevKvs(
        int index, io.etcd.jetcd.api.KeyValue value) {
      if (prevKvsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensurePrevKvsIsMutable();
        prevKvs_.set(index, value);
        onChanged();
      } else {
        prevKvsBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <pre>
     * if prev_kv is set in the request, the previous key-value pairs will be returned.
     * </pre>
     *
     * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
     */
    public Builder setPrevKvs(
        int index, io.etcd.jetcd.api.KeyValue.Builder builderForValue) {
      if (prevKvsBuilder_ == null) {
        ensurePrevKvsIsMutable();
        prevKvs_.set(index, builderForValue.build());
        onChanged();
      } else {
        prevKvsBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     * if prev_kv is set in the request, the previous key-value pairs will be returned.
     * </pre>
     *
     * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
     */
    public Builder addPrevKvs(io.etcd.jetcd.api.KeyValue value) {
      if (prevKvsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensurePrevKvsIsMutable();
        prevKvs_.add(value);
        onChanged();
      } else {
        prevKvsBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <pre>
     * if prev_kv is set in the request, the previous key-value pairs will be returned.
     * </pre>
     *
     * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
     */
    public Builder addPrevKvs(
        int index, io.etcd.jetcd.api.KeyValue value) {
      if (prevKvsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensurePrevKvsIsMutable();
        prevKvs_.add(index, value);
        onChanged();
      } else {
        prevKvsBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <pre>
     * if prev_kv is set in the request, the previous key-value pairs will be returned.
     * </pre>
     *
     * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
     */
    public Builder addPrevKvs(
        io.etcd.jetcd.api.KeyValue.Builder builderForValue) {
      if (prevKvsBuilder_ == null) {
        ensurePrevKvsIsMutable();
        prevKvs_.add(builderForValue.build());
        onChanged();
      } else {
        prevKvsBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     * if prev_kv is set in the request, the previous key-value pairs will be returned.
     * </pre>
     *
     * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
     */
    public Builder addPrevKvs(
        int index, io.etcd.jetcd.api.KeyValue.Builder builderForValue) {
      if (prevKvsBuilder_ == null) {
        ensurePrevKvsIsMutable();
        prevKvs_.add(index, builderForValue.build());
        onChanged();
      } else {
        prevKvsBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     * if prev_kv is set in the request, the previous key-value pairs will be returned.
     * </pre>
     *
     * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
     */
    public Builder addAllPrevKvs(
        java.lang.Iterable<? extends io.etcd.jetcd.api.KeyValue> values) {
      if (prevKvsBuilder_ == null) {
        ensurePrevKvsIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, prevKvs_);
        onChanged();
      } else {
        prevKvsBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <pre>
     * if prev_kv is set in the request, the previous key-value pairs will be returned.
     * </pre>
     *
     * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
     */
    public Builder clearPrevKvs() {
      if (prevKvsBuilder_ == null) {
        prevKvs_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000004);
        onChanged();
      } else {
        prevKvsBuilder_.clear();
      }
      return this;
    }
    /**
     * <pre>
     * if prev_kv is set in the request, the previous key-value pairs will be returned.
     * </pre>
     *
     * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
     */
    public Builder removePrevKvs(int index) {
      if (prevKvsBuilder_ == null) {
        ensurePrevKvsIsMutable();
        prevKvs_.remove(index);
        onChanged();
      } else {
        prevKvsBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <pre>
     * if prev_kv is set in the request, the previous key-value pairs will be returned.
     * </pre>
     *
     * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
     */
    public io.etcd.jetcd.api.KeyValue.Builder getPrevKvsBuilder(
        int index) {
      return getPrevKvsFieldBuilder().getBuilder(index);
    }
    /**
     * <pre>
     * if prev_kv is set in the request, the previous key-value pairs will be returned.
     * </pre>
     *
     * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
     */
    public io.etcd.jetcd.api.KeyValueOrBuilder getPrevKvsOrBuilder(
        int index) {
      if (prevKvsBuilder_ == null) {
        return prevKvs_.get(index);  } else {
        return prevKvsBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <pre>
     * if prev_kv is set in the request, the previous key-value pairs will be returned.
     * </pre>
     *
     * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
     */
    public java.util.List<? extends io.etcd.jetcd.api.KeyValueOrBuilder> 
         getPrevKvsOrBuilderList() {
      if (prevKvsBuilder_ != null) {
        return prevKvsBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(prevKvs_);
      }
    }
    /**
     * <pre>
     * if prev_kv is set in the request, the previous key-value pairs will be returned.
     * </pre>
     *
     * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
     */
    public io.etcd.jetcd.api.KeyValue.Builder addPrevKvsBuilder() {
      return getPrevKvsFieldBuilder().addBuilder(
          io.etcd.jetcd.api.KeyValue.getDefaultInstance());
    }
    /**
     * <pre>
     * if prev_kv is set in the request, the previous key-value pairs will be returned.
     * </pre>
     *
     * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
     */
    public io.etcd.jetcd.api.KeyValue.Builder addPrevKvsBuilder(
        int index) {
      return getPrevKvsFieldBuilder().addBuilder(
          index, io.etcd.jetcd.api.KeyValue.getDefaultInstance());
    }
    /**
     * <pre>
     * if prev_kv is set in the request, the previous key-value pairs will be returned.
     * </pre>
     *
     * <code>repeated .mvccpb.KeyValue prev_kvs = 3;</code>
     */
    public java.util.List<io.etcd.jetcd.api.KeyValue.Builder> 
         getPrevKvsBuilderList() {
      return getPrevKvsFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        io.etcd.jetcd.api.KeyValue, io.etcd.jetcd.api.KeyValue.Builder, io.etcd.jetcd.api.KeyValueOrBuilder> 
        getPrevKvsFieldBuilder() {
      if (prevKvsBuilder_ == null) {
        prevKvsBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            io.etcd.jetcd.api.KeyValue, io.etcd.jetcd.api.KeyValue.Builder, io.etcd.jetcd.api.KeyValueOrBuilder>(
                prevKvs_,
                ((bitField0_ & 0x00000004) == 0x00000004),
                getParentForChildren(),
                isClean());
        prevKvs_ = null;
      }
      return prevKvsBuilder_;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }


    // @@protoc_insertion_point(builder_scope:etcdserverpb.DeleteRangeResponse)
  }

  // @@protoc_insertion_point(class_scope:etcdserverpb.DeleteRangeResponse)
  private static final io.etcd.jetcd.api.DeleteRangeResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.etcd.jetcd.api.DeleteRangeResponse();
  }

  public static io.etcd.jetcd.api.DeleteRangeResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<DeleteRangeResponse>
      PARSER = new com.google.protobuf.AbstractParser<DeleteRangeResponse>() {
    public DeleteRangeResponse parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return new DeleteRangeResponse(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<DeleteRangeResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<DeleteRangeResponse> getParserForType() {
    return PARSER;
  }

  public io.etcd.jetcd.api.DeleteRangeResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

