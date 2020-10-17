// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: rpc.proto

package io.etcd.jetcd.api;

public interface MemberUpdateRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:etcdserverpb.MemberUpdateRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * ID is the member ID of the member to update.
   * </pre>
   *
   * <code>uint64 ID = 1;</code>
   */
  long getID();

  /**
   * <pre>
   * peerURLs is the new list of URLs the member will use to communicate with the cluster.
   * </pre>
   *
   * <code>repeated string peerURLs = 2;</code>
   */
  java.util.List<java.lang.String>
      getPeerURLsList();
  /**
   * <pre>
   * peerURLs is the new list of URLs the member will use to communicate with the cluster.
   * </pre>
   *
   * <code>repeated string peerURLs = 2;</code>
   */
  int getPeerURLsCount();
  /**
   * <pre>
   * peerURLs is the new list of URLs the member will use to communicate with the cluster.
   * </pre>
   *
   * <code>repeated string peerURLs = 2;</code>
   */
  java.lang.String getPeerURLs(int index);
  /**
   * <pre>
   * peerURLs is the new list of URLs the member will use to communicate with the cluster.
   * </pre>
   *
   * <code>repeated string peerURLs = 2;</code>
   */
  com.google.protobuf.ByteString
      getPeerURLsBytes(int index);
}
