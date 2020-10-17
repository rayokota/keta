// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: election.proto

package io.etcd.jetcd.api;

public final class ElectionOuterClass {
  private ElectionOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_v3electionpb_CampaignRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_v3electionpb_CampaignRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_v3electionpb_CampaignResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_v3electionpb_CampaignResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_v3electionpb_LeaderKey_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_v3electionpb_LeaderKey_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_v3electionpb_LeaderRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_v3electionpb_LeaderRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_v3electionpb_LeaderResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_v3electionpb_LeaderResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_v3electionpb_ResignRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_v3electionpb_ResignRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_v3electionpb_ResignResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_v3electionpb_ResignResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_v3electionpb_ProclaimRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_v3electionpb_ProclaimRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_v3electionpb_ProclaimResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_v3electionpb_ProclaimResponse_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\016election.proto\022\014v3electionpb\032\trpc.prot" +
      "o\032\010kv.proto\"=\n\017CampaignRequest\022\014\n\004name\030\001" +
      " \001(\014\022\r\n\005lease\030\002 \001(\003\022\r\n\005value\030\003 \001(\014\"i\n\020Ca" +
      "mpaignResponse\022,\n\006header\030\001 \001(\0132\034.etcdser" +
      "verpb.ResponseHeader\022\'\n\006leader\030\002 \001(\0132\027.v" +
      "3electionpb.LeaderKey\"B\n\tLeaderKey\022\014\n\004na" +
      "me\030\001 \001(\014\022\013\n\003key\030\002 \001(\014\022\013\n\003rev\030\003 \001(\003\022\r\n\005le" +
      "ase\030\004 \001(\003\"\035\n\rLeaderRequest\022\014\n\004name\030\001 \001(\014" +
      "\"\\\n\016LeaderResponse\022,\n\006header\030\001 \001(\0132\034.etc" +
      "dserverpb.ResponseHeader\022\034\n\002kv\030\002 \001(\0132\020.m",
      "vccpb.KeyValue\"8\n\rResignRequest\022\'\n\006leade" +
      "r\030\001 \001(\0132\027.v3electionpb.LeaderKey\">\n\016Resi" +
      "gnResponse\022,\n\006header\030\001 \001(\0132\034.etcdserverp" +
      "b.ResponseHeader\"I\n\017ProclaimRequest\022\'\n\006l" +
      "eader\030\001 \001(\0132\027.v3electionpb.LeaderKey\022\r\n\005" +
      "value\030\002 \001(\014\"@\n\020ProclaimResponse\022,\n\006heade" +
      "r\030\001 \001(\0132\034.etcdserverpb.ResponseHeader2\374\002" +
      "\n\010Election\022K\n\010Campaign\022\035.v3electionpb.Ca" +
      "mpaignRequest\032\036.v3electionpb.CampaignRes" +
      "ponse\"\000\022K\n\010Proclaim\022\035.v3electionpb.Procl",
      "aimRequest\032\036.v3electionpb.ProclaimRespon" +
      "se\"\000\022E\n\006Leader\022\033.v3electionpb.LeaderRequ" +
      "est\032\034.v3electionpb.LeaderResponse\"\000\022H\n\007O" +
      "bserve\022\033.v3electionpb.LeaderRequest\032\034.v3" +
      "electionpb.LeaderResponse\"\0000\001\022E\n\006Resign\022" +
      "\033.v3electionpb.ResignRequest\032\034.v3electio" +
      "npb.ResignResponse\"\000B\025\n\021io.etcd.jetcd.ap" +
      "iP\001b\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          io.etcd.jetcd.api.JetcdProto.getDescriptor(),
          io.etcd.jetcd.api.Kv.getDescriptor(),
        }, assigner);
    internal_static_v3electionpb_CampaignRequest_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_v3electionpb_CampaignRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_v3electionpb_CampaignRequest_descriptor,
        new java.lang.String[] { "Name", "Lease", "Value", });
    internal_static_v3electionpb_CampaignResponse_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_v3electionpb_CampaignResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_v3electionpb_CampaignResponse_descriptor,
        new java.lang.String[] { "Header", "Leader", });
    internal_static_v3electionpb_LeaderKey_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_v3electionpb_LeaderKey_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_v3electionpb_LeaderKey_descriptor,
        new java.lang.String[] { "Name", "Key", "Rev", "Lease", });
    internal_static_v3electionpb_LeaderRequest_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_v3electionpb_LeaderRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_v3electionpb_LeaderRequest_descriptor,
        new java.lang.String[] { "Name", });
    internal_static_v3electionpb_LeaderResponse_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_v3electionpb_LeaderResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_v3electionpb_LeaderResponse_descriptor,
        new java.lang.String[] { "Header", "Kv", });
    internal_static_v3electionpb_ResignRequest_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_v3electionpb_ResignRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_v3electionpb_ResignRequest_descriptor,
        new java.lang.String[] { "Leader", });
    internal_static_v3electionpb_ResignResponse_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_v3electionpb_ResignResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_v3electionpb_ResignResponse_descriptor,
        new java.lang.String[] { "Header", });
    internal_static_v3electionpb_ProclaimRequest_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_v3electionpb_ProclaimRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_v3electionpb_ProclaimRequest_descriptor,
        new java.lang.String[] { "Leader", "Value", });
    internal_static_v3electionpb_ProclaimResponse_descriptor =
      getDescriptor().getMessageTypes().get(8);
    internal_static_v3electionpb_ProclaimResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_v3electionpb_ProclaimResponse_descriptor,
        new java.lang.String[] { "Header", });
    io.etcd.jetcd.api.JetcdProto.getDescriptor();
    io.etcd.jetcd.api.Kv.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
