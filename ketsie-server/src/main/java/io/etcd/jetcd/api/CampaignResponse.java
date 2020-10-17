// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: election.proto

package io.etcd.jetcd.api;

/**
 * Protobuf type {@code v3electionpb.CampaignResponse}
 */
public  final class CampaignResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:v3electionpb.CampaignResponse)
    CampaignResponseOrBuilder {
  // Use CampaignResponse.newBuilder() to construct.
  private CampaignResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private CampaignResponse() {
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
  }
  private CampaignResponse(
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
          case 18: {
            io.etcd.jetcd.api.LeaderKey.Builder subBuilder = null;
            if (leader_ != null) {
              subBuilder = leader_.toBuilder();
            }
            leader_ = input.readMessage(io.etcd.jetcd.api.LeaderKey.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(leader_);
              leader_ = subBuilder.buildPartial();
            }

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
    return io.etcd.jetcd.api.ElectionOuterClass.internal_static_v3electionpb_CampaignResponse_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return io.etcd.jetcd.api.ElectionOuterClass.internal_static_v3electionpb_CampaignResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            io.etcd.jetcd.api.CampaignResponse.class, io.etcd.jetcd.api.CampaignResponse.Builder.class);
  }

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

  public static final int LEADER_FIELD_NUMBER = 2;
  private io.etcd.jetcd.api.LeaderKey leader_;
  /**
   * <pre>
   * leader describes the resources used for holding leadereship of the election.
   * </pre>
   *
   * <code>.v3electionpb.LeaderKey leader = 2;</code>
   */
  public boolean hasLeader() {
    return leader_ != null;
  }
  /**
   * <pre>
   * leader describes the resources used for holding leadereship of the election.
   * </pre>
   *
   * <code>.v3electionpb.LeaderKey leader = 2;</code>
   */
  public io.etcd.jetcd.api.LeaderKey getLeader() {
    return leader_ == null ? io.etcd.jetcd.api.LeaderKey.getDefaultInstance() : leader_;
  }
  /**
   * <pre>
   * leader describes the resources used for holding leadereship of the election.
   * </pre>
   *
   * <code>.v3electionpb.LeaderKey leader = 2;</code>
   */
  public io.etcd.jetcd.api.LeaderKeyOrBuilder getLeaderOrBuilder() {
    return getLeader();
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
    if (leader_ != null) {
      output.writeMessage(2, getLeader());
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
    if (leader_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getLeader());
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
    if (!(obj instanceof io.etcd.jetcd.api.CampaignResponse)) {
      return super.equals(obj);
    }
    io.etcd.jetcd.api.CampaignResponse other = (io.etcd.jetcd.api.CampaignResponse) obj;

    boolean result = true;
    result = result && (hasHeader() == other.hasHeader());
    if (hasHeader()) {
      result = result && getHeader()
          .equals(other.getHeader());
    }
    result = result && (hasLeader() == other.hasLeader());
    if (hasLeader()) {
      result = result && getLeader()
          .equals(other.getLeader());
    }
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
    if (hasLeader()) {
      hash = (37 * hash) + LEADER_FIELD_NUMBER;
      hash = (53 * hash) + getLeader().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.etcd.jetcd.api.CampaignResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.etcd.jetcd.api.CampaignResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.etcd.jetcd.api.CampaignResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.etcd.jetcd.api.CampaignResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.etcd.jetcd.api.CampaignResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.etcd.jetcd.api.CampaignResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.etcd.jetcd.api.CampaignResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static io.etcd.jetcd.api.CampaignResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.etcd.jetcd.api.CampaignResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static io.etcd.jetcd.api.CampaignResponse parseFrom(
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
  public static Builder newBuilder(io.etcd.jetcd.api.CampaignResponse prototype) {
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
   * Protobuf type {@code v3electionpb.CampaignResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:v3electionpb.CampaignResponse)
      io.etcd.jetcd.api.CampaignResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.etcd.jetcd.api.ElectionOuterClass.internal_static_v3electionpb_CampaignResponse_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.etcd.jetcd.api.ElectionOuterClass.internal_static_v3electionpb_CampaignResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.etcd.jetcd.api.CampaignResponse.class, io.etcd.jetcd.api.CampaignResponse.Builder.class);
    }

    // Construct using io.etcd.jetcd.api.CampaignResponse.newBuilder()
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
      if (headerBuilder_ == null) {
        header_ = null;
      } else {
        header_ = null;
        headerBuilder_ = null;
      }
      if (leaderBuilder_ == null) {
        leader_ = null;
      } else {
        leader_ = null;
        leaderBuilder_ = null;
      }
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return io.etcd.jetcd.api.ElectionOuterClass.internal_static_v3electionpb_CampaignResponse_descriptor;
    }

    public io.etcd.jetcd.api.CampaignResponse getDefaultInstanceForType() {
      return io.etcd.jetcd.api.CampaignResponse.getDefaultInstance();
    }

    public io.etcd.jetcd.api.CampaignResponse build() {
      io.etcd.jetcd.api.CampaignResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public io.etcd.jetcd.api.CampaignResponse buildPartial() {
      io.etcd.jetcd.api.CampaignResponse result = new io.etcd.jetcd.api.CampaignResponse(this);
      if (headerBuilder_ == null) {
        result.header_ = header_;
      } else {
        result.header_ = headerBuilder_.build();
      }
      if (leaderBuilder_ == null) {
        result.leader_ = leader_;
      } else {
        result.leader_ = leaderBuilder_.build();
      }
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
      if (other instanceof io.etcd.jetcd.api.CampaignResponse) {
        return mergeFrom((io.etcd.jetcd.api.CampaignResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.etcd.jetcd.api.CampaignResponse other) {
      if (other == io.etcd.jetcd.api.CampaignResponse.getDefaultInstance()) return this;
      if (other.hasHeader()) {
        mergeHeader(other.getHeader());
      }
      if (other.hasLeader()) {
        mergeLeader(other.getLeader());
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
      io.etcd.jetcd.api.CampaignResponse parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (io.etcd.jetcd.api.CampaignResponse) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

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

    private io.etcd.jetcd.api.LeaderKey leader_ = null;
    private com.google.protobuf.SingleFieldBuilderV3<
        io.etcd.jetcd.api.LeaderKey, io.etcd.jetcd.api.LeaderKey.Builder, io.etcd.jetcd.api.LeaderKeyOrBuilder> leaderBuilder_;
    /**
     * <pre>
     * leader describes the resources used for holding leadereship of the election.
     * </pre>
     *
     * <code>.v3electionpb.LeaderKey leader = 2;</code>
     */
    public boolean hasLeader() {
      return leaderBuilder_ != null || leader_ != null;
    }
    /**
     * <pre>
     * leader describes the resources used for holding leadereship of the election.
     * </pre>
     *
     * <code>.v3electionpb.LeaderKey leader = 2;</code>
     */
    public io.etcd.jetcd.api.LeaderKey getLeader() {
      if (leaderBuilder_ == null) {
        return leader_ == null ? io.etcd.jetcd.api.LeaderKey.getDefaultInstance() : leader_;
      } else {
        return leaderBuilder_.getMessage();
      }
    }
    /**
     * <pre>
     * leader describes the resources used for holding leadereship of the election.
     * </pre>
     *
     * <code>.v3electionpb.LeaderKey leader = 2;</code>
     */
    public Builder setLeader(io.etcd.jetcd.api.LeaderKey value) {
      if (leaderBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        leader_ = value;
        onChanged();
      } else {
        leaderBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <pre>
     * leader describes the resources used for holding leadereship of the election.
     * </pre>
     *
     * <code>.v3electionpb.LeaderKey leader = 2;</code>
     */
    public Builder setLeader(
        io.etcd.jetcd.api.LeaderKey.Builder builderForValue) {
      if (leaderBuilder_ == null) {
        leader_ = builderForValue.build();
        onChanged();
      } else {
        leaderBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <pre>
     * leader describes the resources used for holding leadereship of the election.
     * </pre>
     *
     * <code>.v3electionpb.LeaderKey leader = 2;</code>
     */
    public Builder mergeLeader(io.etcd.jetcd.api.LeaderKey value) {
      if (leaderBuilder_ == null) {
        if (leader_ != null) {
          leader_ =
            io.etcd.jetcd.api.LeaderKey.newBuilder(leader_).mergeFrom(value).buildPartial();
        } else {
          leader_ = value;
        }
        onChanged();
      } else {
        leaderBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <pre>
     * leader describes the resources used for holding leadereship of the election.
     * </pre>
     *
     * <code>.v3electionpb.LeaderKey leader = 2;</code>
     */
    public Builder clearLeader() {
      if (leaderBuilder_ == null) {
        leader_ = null;
        onChanged();
      } else {
        leader_ = null;
        leaderBuilder_ = null;
      }

      return this;
    }
    /**
     * <pre>
     * leader describes the resources used for holding leadereship of the election.
     * </pre>
     *
     * <code>.v3electionpb.LeaderKey leader = 2;</code>
     */
    public io.etcd.jetcd.api.LeaderKey.Builder getLeaderBuilder() {
      
      onChanged();
      return getLeaderFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * leader describes the resources used for holding leadereship of the election.
     * </pre>
     *
     * <code>.v3electionpb.LeaderKey leader = 2;</code>
     */
    public io.etcd.jetcd.api.LeaderKeyOrBuilder getLeaderOrBuilder() {
      if (leaderBuilder_ != null) {
        return leaderBuilder_.getMessageOrBuilder();
      } else {
        return leader_ == null ?
            io.etcd.jetcd.api.LeaderKey.getDefaultInstance() : leader_;
      }
    }
    /**
     * <pre>
     * leader describes the resources used for holding leadereship of the election.
     * </pre>
     *
     * <code>.v3electionpb.LeaderKey leader = 2;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        io.etcd.jetcd.api.LeaderKey, io.etcd.jetcd.api.LeaderKey.Builder, io.etcd.jetcd.api.LeaderKeyOrBuilder> 
        getLeaderFieldBuilder() {
      if (leaderBuilder_ == null) {
        leaderBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            io.etcd.jetcd.api.LeaderKey, io.etcd.jetcd.api.LeaderKey.Builder, io.etcd.jetcd.api.LeaderKeyOrBuilder>(
                getLeader(),
                getParentForChildren(),
                isClean());
        leader_ = null;
      }
      return leaderBuilder_;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }


    // @@protoc_insertion_point(builder_scope:v3electionpb.CampaignResponse)
  }

  // @@protoc_insertion_point(class_scope:v3electionpb.CampaignResponse)
  private static final io.etcd.jetcd.api.CampaignResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.etcd.jetcd.api.CampaignResponse();
  }

  public static io.etcd.jetcd.api.CampaignResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<CampaignResponse>
      PARSER = new com.google.protobuf.AbstractParser<CampaignResponse>() {
    public CampaignResponse parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return new CampaignResponse(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<CampaignResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<CampaignResponse> getParserForType() {
    return PARSER;
  }

  public io.etcd.jetcd.api.CampaignResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

