// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: rpc.proto

package io.etcd.jetcd.api;

public interface LeaseRevokeResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:etcdserverpb.LeaseRevokeResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.etcdserverpb.ResponseHeader header = 1;</code>
   */
  boolean hasHeader();
  /**
   * <code>.etcdserverpb.ResponseHeader header = 1;</code>
   */
  io.etcd.jetcd.api.ResponseHeader getHeader();
  /**
   * <code>.etcdserverpb.ResponseHeader header = 1;</code>
   */
  io.etcd.jetcd.api.ResponseHeaderOrBuilder getHeaderOrBuilder();
}