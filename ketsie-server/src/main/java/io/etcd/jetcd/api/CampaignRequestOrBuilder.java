// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: election.proto

package io.etcd.jetcd.api;

public interface CampaignRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:v3electionpb.CampaignRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * name is the election's identifier for the campaign.
   * </pre>
   *
   * <code>bytes name = 1;</code>
   */
  com.google.protobuf.ByteString getName();

  /**
   * <pre>
   * lease is the ID of the lease attached to leadership of the election. If the
   * lease expires or is revoked before resigning leadership, then the
   * leadership is transferred to the next campaigner, if any.
   * </pre>
   *
   * <code>int64 lease = 2;</code>
   */
  long getLease();

  /**
   * <pre>
   * value is the initial proclaimed value set when the campaigner wins the
   * election.
   * </pre>
   *
   * <code>bytes value = 3;</code>
   */
  com.google.protobuf.ByteString getValue();
}
