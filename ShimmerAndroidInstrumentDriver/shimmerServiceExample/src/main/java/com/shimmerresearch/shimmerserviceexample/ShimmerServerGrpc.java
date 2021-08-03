package com.shimmerresearch.shimmerserviceexample;

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
 * The greeter service definition.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.32.1)",
    comments = "Source: src/ShimmerGrpcAndOJC.proto")
public final class ShimmerServerGrpc {

  private ShimmerServerGrpc() {}

  public static final String SERVICE_NAME = "shimmerGRPC.ShimmerServer";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> getSayHelloMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SayHello",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.HelloReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> getSayHelloMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest, com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> getSayHelloMethod;
    if ((getSayHelloMethod = ShimmerServerGrpc.getSayHelloMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getSayHelloMethod = ShimmerServerGrpc.getSayHelloMethod) == null) {
          ShimmerServerGrpc.getSayHelloMethod = getSayHelloMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest, com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SayHello"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.HelloReply.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("SayHello"))
              .build();
        }
      }
    }
    return getSayHelloMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> getGetDataStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetDataStream",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> getGetDataStreamMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest, com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> getGetDataStreamMethod;
    if ((getGetDataStreamMethod = ShimmerServerGrpc.getGetDataStreamMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getGetDataStreamMethod = ShimmerServerGrpc.getGetDataStreamMethod) == null) {
          ShimmerServerGrpc.getGetDataStreamMethod = getGetDataStreamMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest, com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetDataStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("GetDataStream"))
              .build();
        }
      }
    }
    return getGetDataStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2,
      com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> getSendDataStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SendDataStream",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.HelloReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2,
      com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> getSendDataStreamMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2, com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> getSendDataStreamMethod;
    if ((getSendDataStreamMethod = ShimmerServerGrpc.getSendDataStreamMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getSendDataStreamMethod = ShimmerServerGrpc.getSendDataStreamMethod) == null) {
          ShimmerServerGrpc.getSendDataStreamMethod = getSendDataStreamMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2, com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SendDataStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.HelloReply.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("SendDataStream"))
              .build();
        }
      }
    }
    return getSendDataStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.FileByteTransfer,
      com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> getSendFileStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SendFileStream",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.FileByteTransfer.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.HelloReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.FileByteTransfer,
      com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> getSendFileStreamMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.FileByteTransfer, com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> getSendFileStreamMethod;
    if ((getSendFileStreamMethod = ShimmerServerGrpc.getSendFileStreamMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getSendFileStreamMethod = ShimmerServerGrpc.getSendFileStreamMethod) == null) {
          ShimmerServerGrpc.getSendFileStreamMethod = getSendFileStreamMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.FileByteTransfer, com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SendFileStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.FileByteTransfer.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.HelloReply.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("SendFileStream"))
              .build();
        }
      }
    }
    return getSendFileStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> getConnectShimmerMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ConnectShimmer",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> getConnectShimmerMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest, com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> getConnectShimmerMethod;
    if ((getConnectShimmerMethod = ShimmerServerGrpc.getConnectShimmerMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getConnectShimmerMethod = ShimmerServerGrpc.getConnectShimmerMethod) == null) {
          ShimmerServerGrpc.getConnectShimmerMethod = getConnectShimmerMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest, com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ConnectShimmer"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("ConnectShimmer"))
              .build();
        }
      }
    }
    return getConnectShimmerMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> getDisconnectShimmerMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DisconnectShimmer",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> getDisconnectShimmerMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest, com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> getDisconnectShimmerMethod;
    if ((getDisconnectShimmerMethod = ShimmerServerGrpc.getDisconnectShimmerMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getDisconnectShimmerMethod = ShimmerServerGrpc.getDisconnectShimmerMethod) == null) {
          ShimmerServerGrpc.getDisconnectShimmerMethod = getDisconnectShimmerMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest, com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DisconnectShimmer"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("DisconnectShimmer"))
              .build();
        }
      }
    }
    return getDisconnectShimmerMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> getStartStreamingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "StartStreaming",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> getStartStreamingMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest, com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> getStartStreamingMethod;
    if ((getStartStreamingMethod = ShimmerServerGrpc.getStartStreamingMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getStartStreamingMethod = ShimmerServerGrpc.getStartStreamingMethod) == null) {
          ShimmerServerGrpc.getStartStreamingMethod = getStartStreamingMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest, com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "StartStreaming"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("StartStreaming"))
              .build();
        }
      }
    }
    return getStartStreamingMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> getStopStreamingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "StopStreaming",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> getStopStreamingMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest, com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> getStopStreamingMethod;
    if ((getStopStreamingMethod = ShimmerServerGrpc.getStopStreamingMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getStopStreamingMethod = ShimmerServerGrpc.getStopStreamingMethod) == null) {
          ShimmerServerGrpc.getStopStreamingMethod = getStopStreamingMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest, com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "StopStreaming"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("StopStreaming"))
              .build();
        }
      }
    }
    return getStopStreamingMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> getCloseApplicationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CloseApplication",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
      com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> getCloseApplicationMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest, com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> getCloseApplicationMethod;
    if ((getCloseApplicationMethod = ShimmerServerGrpc.getCloseApplicationMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getCloseApplicationMethod = ShimmerServerGrpc.getCloseApplicationMethod) == null) {
          ShimmerServerGrpc.getCloseApplicationMethod = getCloseApplicationMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest, com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CloseApplication"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("CloseApplication"))
              .build();
        }
      }
    }
    return getCloseApplicationMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getSetWorkspaceDirectoryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetWorkspaceDirectory",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getSetWorkspaceDirectoryMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getSetWorkspaceDirectoryMethod;
    if ((getSetWorkspaceDirectoryMethod = ShimmerServerGrpc.getSetWorkspaceDirectoryMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getSetWorkspaceDirectoryMethod = ShimmerServerGrpc.getSetWorkspaceDirectoryMethod) == null) {
          ShimmerServerGrpc.getSetWorkspaceDirectoryMethod = getSetWorkspaceDirectoryMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetWorkspaceDirectory"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("SetWorkspaceDirectory"))
              .build();
        }
      }
    }
    return getSetWorkspaceDirectoryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.StringMsg> getGetWorkspaceDirectoryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetWorkspaceDirectory",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.StringMsg> getGetWorkspaceDirectoryMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.StringMsg> getGetWorkspaceDirectoryMethod;
    if ((getGetWorkspaceDirectoryMethod = ShimmerServerGrpc.getGetWorkspaceDirectoryMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getGetWorkspaceDirectoryMethod = ShimmerServerGrpc.getGetWorkspaceDirectoryMethod) == null) {
          ShimmerServerGrpc.getGetWorkspaceDirectoryMethod = getGetWorkspaceDirectoryMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.StringMsg>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetWorkspaceDirectory"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("GetWorkspaceDirectory"))
              .build();
        }
      }
    }
    return getGetWorkspaceDirectoryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> getGetDockedShimmerInfoMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetDockedShimmerInfo",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> getGetDockedShimmerInfoMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> getGetDockedShimmerInfoMethod;
    if ((getGetDockedShimmerInfoMethod = ShimmerServerGrpc.getGetDockedShimmerInfoMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getGetDockedShimmerInfoMethod = ShimmerServerGrpc.getGetDockedShimmerInfoMethod) == null) {
          ShimmerServerGrpc.getGetDockedShimmerInfoMethod = getGetDockedShimmerInfoMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetDockedShimmerInfo"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("GetDockedShimmerInfo"))
              .build();
        }
      }
    }
    return getGetDockedShimmerInfoMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg> getGetMadgewickBetaValueMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetMadgewickBetaValue",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg> getGetMadgewickBetaValueMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg> getGetMadgewickBetaValueMethod;
    if ((getGetMadgewickBetaValueMethod = ShimmerServerGrpc.getGetMadgewickBetaValueMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getGetMadgewickBetaValueMethod = ShimmerServerGrpc.getGetMadgewickBetaValueMethod) == null) {
          ShimmerServerGrpc.getGetMadgewickBetaValueMethod = getGetMadgewickBetaValueMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetMadgewickBetaValue"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("GetMadgewickBetaValue"))
              .build();
        }
      }
    }
    return getGetMadgewickBetaValueMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getPairShimmersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PairShimmers",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getPairShimmersMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getPairShimmersMethod;
    if ((getPairShimmersMethod = ShimmerServerGrpc.getPairShimmersMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getPairShimmersMethod = ShimmerServerGrpc.getPairShimmersMethod) == null) {
          ShimmerServerGrpc.getPairShimmersMethod = getPairShimmersMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PairShimmers"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("PairShimmers"))
              .build();
        }
      }
    }
    return getPairShimmersMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getGetOperationProgressMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetOperationProgress",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getGetOperationProgressMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getGetOperationProgressMethod;
    if ((getGetOperationProgressMethod = ShimmerServerGrpc.getGetOperationProgressMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getGetOperationProgressMethod = ShimmerServerGrpc.getGetOperationProgressMethod) == null) {
          ShimmerServerGrpc.getGetOperationProgressMethod = getGetOperationProgressMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetOperationProgress"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("GetOperationProgress"))
              .build();
        }
      }
    }
    return getGetOperationProgressMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getImportSdDataFromShimmersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ImportSdDataFromShimmers",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getImportSdDataFromShimmersMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getImportSdDataFromShimmersMethod;
    if ((getImportSdDataFromShimmersMethod = ShimmerServerGrpc.getImportSdDataFromShimmersMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getImportSdDataFromShimmersMethod = ShimmerServerGrpc.getImportSdDataFromShimmersMethod) == null) {
          ShimmerServerGrpc.getImportSdDataFromShimmersMethod = getImportSdDataFromShimmersMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ImportSdDataFromShimmers"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("ImportSdDataFromShimmers"))
              .build();
        }
      }
    }
    return getImportSdDataFromShimmersMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getParseSdDataFromPathMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ParseSdDataFromPath",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getParseSdDataFromPathMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getParseSdDataFromPathMethod;
    if ((getParseSdDataFromPathMethod = ShimmerServerGrpc.getParseSdDataFromPathMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getParseSdDataFromPathMethod = ShimmerServerGrpc.getParseSdDataFromPathMethod) == null) {
          ShimmerServerGrpc.getParseSdDataFromPathMethod = getParseSdDataFromPathMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ParseSdDataFromPath"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("ParseSdDataFromPath"))
              .build();
        }
      }
    }
    return getParseSdDataFromPathMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getScanSdDataAndCopyMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ScanSdDataAndCopy",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getScanSdDataAndCopyMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getScanSdDataAndCopyMethod;
    if ((getScanSdDataAndCopyMethod = ShimmerServerGrpc.getScanSdDataAndCopyMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getScanSdDataAndCopyMethod = ShimmerServerGrpc.getScanSdDataAndCopyMethod) == null) {
          ShimmerServerGrpc.getScanSdDataAndCopyMethod = getScanSdDataAndCopyMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ScanSdDataAndCopy"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("ScanSdDataAndCopy"))
              .build();
        }
      }
    }
    return getScanSdDataAndCopyMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getClearSdCardDataMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ClearSdCardData",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getClearSdCardDataMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getClearSdCardDataMethod;
    if ((getClearSdCardDataMethod = ShimmerServerGrpc.getClearSdCardDataMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getClearSdCardDataMethod = ShimmerServerGrpc.getClearSdCardDataMethod) == null) {
          ShimmerServerGrpc.getClearSdCardDataMethod = getClearSdCardDataMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ClearSdCardData"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("ClearSdCardData"))
              .build();
        }
      }
    }
    return getClearSdCardDataMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getDockAccessSlotWithSdCardMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DockAccessSlotWithSdCard",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getDockAccessSlotWithSdCardMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getDockAccessSlotWithSdCardMethod;
    if ((getDockAccessSlotWithSdCardMethod = ShimmerServerGrpc.getDockAccessSlotWithSdCardMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getDockAccessSlotWithSdCardMethod = ShimmerServerGrpc.getDockAccessSlotWithSdCardMethod) == null) {
          ShimmerServerGrpc.getDockAccessSlotWithSdCardMethod = getDockAccessSlotWithSdCardMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DockAccessSlotWithSdCard"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("DockAccessSlotWithSdCard"))
              .build();
        }
      }
    }
    return getDockAccessSlotWithSdCardMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getDockRestoreAutoTasksMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DockRestoreAutoTasks",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getDockRestoreAutoTasksMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getDockRestoreAutoTasksMethod;
    if ((getDockRestoreAutoTasksMethod = ShimmerServerGrpc.getDockRestoreAutoTasksMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getDockRestoreAutoTasksMethod = ShimmerServerGrpc.getDockRestoreAutoTasksMethod) == null) {
          ShimmerServerGrpc.getDockRestoreAutoTasksMethod = getDockRestoreAutoTasksMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg, com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DockRestoreAutoTasks"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("DockRestoreAutoTasks"))
              .build();
        }
      }
    }
    return getDockRestoreAutoTasksMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.InfoSpans> getGetInfoSpansMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetInfoSpans",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.InfoSpans.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.InfoSpans> getGetInfoSpansMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.InfoSpans> getGetInfoSpansMethod;
    if ((getGetInfoSpansMethod = ShimmerServerGrpc.getGetInfoSpansMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getGetInfoSpansMethod = ShimmerServerGrpc.getGetInfoSpansMethod) == null) {
          ShimmerServerGrpc.getGetInfoSpansMethod = getGetInfoSpansMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.InfoSpans>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetInfoSpans"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.InfoSpans.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("GetInfoSpans"))
              .build();
        }
      }
    }
    return getGetInfoSpansMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> getGetInfoAllShimmersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetInfoAllShimmers",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> getGetInfoAllShimmersMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> getGetInfoAllShimmersMethod;
    if ((getGetInfoAllShimmersMethod = ShimmerServerGrpc.getGetInfoAllShimmersMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getGetInfoAllShimmersMethod = ShimmerServerGrpc.getGetInfoAllShimmersMethod) == null) {
          ShimmerServerGrpc.getGetInfoAllShimmersMethod = getGetInfoAllShimmersMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetInfoAllShimmers"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("GetInfoAllShimmers"))
              .build();
        }
      }
    }
    return getGetInfoAllShimmersMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.EmulatedDevices> getGetEmulatedDevicesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetEmulatedDevices",
      requestType = com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.class,
      responseType = com.shimmerresearch.grpc.ShimmerGRPC.EmulatedDevices.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
      com.shimmerresearch.grpc.ShimmerGRPC.EmulatedDevices> getGetEmulatedDevicesMethod() {
    io.grpc.MethodDescriptor<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.EmulatedDevices> getGetEmulatedDevicesMethod;
    if ((getGetEmulatedDevicesMethod = ShimmerServerGrpc.getGetEmulatedDevicesMethod) == null) {
      synchronized (ShimmerServerGrpc.class) {
        if ((getGetEmulatedDevicesMethod = ShimmerServerGrpc.getGetEmulatedDevicesMethod) == null) {
          ShimmerServerGrpc.getGetEmulatedDevicesMethod = getGetEmulatedDevicesMethod =
              io.grpc.MethodDescriptor.<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg, com.shimmerresearch.grpc.ShimmerGRPC.EmulatedDevices>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetEmulatedDevices"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.StringMsg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.shimmerresearch.grpc.ShimmerGRPC.EmulatedDevices.getDefaultInstance()))
              .setSchemaDescriptor(new ShimmerServerMethodDescriptorSupplier("GetEmulatedDevices"))
              .build();
        }
      }
    }
    return getGetEmulatedDevicesMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ShimmerServerStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ShimmerServerStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ShimmerServerStub>() {
        @java.lang.Override
        public ShimmerServerStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ShimmerServerStub(channel, callOptions);
        }
      };
    return ShimmerServerStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ShimmerServerBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ShimmerServerBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ShimmerServerBlockingStub>() {
        @java.lang.Override
        public ShimmerServerBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ShimmerServerBlockingStub(channel, callOptions);
        }
      };
    return ShimmerServerBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ShimmerServerFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ShimmerServerFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ShimmerServerFutureStub>() {
        @java.lang.Override
        public ShimmerServerFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ShimmerServerFutureStub(channel, callOptions);
        }
      };
    return ShimmerServerFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * The greeter service definition.
   * </pre>
   */
  public static abstract class ShimmerServerImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Sends a greeting
     * </pre>
     */
    public void sayHello(com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> responseObserver) {
      asyncUnimplementedUnaryCall(getSayHelloMethod(), responseObserver);
    }

    /**
     * <pre>
     *Client asking for data
     * </pre>
     */
    public void getDataStream(com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> responseObserver) {
      asyncUnimplementedUnaryCall(getGetDataStreamMethod(), responseObserver);
    }

    /**
     * <pre>
     *Client sending data
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> sendDataStream(
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> responseObserver) {
      return asyncUnimplementedStreamingCall(getSendDataStreamMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.FileByteTransfer> sendFileStream(
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> responseObserver) {
      return asyncUnimplementedStreamingCall(getSendFileStreamMethod(), responseObserver);
    }

    /**
     */
    public void connectShimmer(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getConnectShimmerMethod(), responseObserver);
    }

    /**
     */
    public void disconnectShimmer(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getDisconnectShimmerMethod(), responseObserver);
    }

    /**
     */
    public void startStreaming(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getStartStreamingMethod(), responseObserver);
    }

    /**
     */
    public void stopStreaming(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getStopStreamingMethod(), responseObserver);
    }

    /**
     */
    public void closeApplication(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getCloseApplicationMethod(), responseObserver);
    }

    /**
     * <pre>
     *ConsensysApi related
     * </pre>
     */
    public void setWorkspaceDirectory(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnimplementedUnaryCall(getSetWorkspaceDirectoryMethod(), responseObserver);
    }

    /**
     */
    public void getWorkspaceDirectory(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg> responseObserver) {
      asyncUnimplementedUnaryCall(getGetWorkspaceDirectoryMethod(), responseObserver);
    }

    /**
     */
    public void getDockedShimmerInfo(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> responseObserver) {
      asyncUnimplementedUnaryCall(getGetDockedShimmerInfoMethod(), responseObserver);
    }

    /**
     */
    public void getMadgewickBetaValue(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg> responseObserver) {
      asyncUnimplementedUnaryCall(getGetMadgewickBetaValueMethod(), responseObserver);
    }

    /**
     */
    public void pairShimmers(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnimplementedUnaryCall(getPairShimmersMethod(), responseObserver);
    }

    /**
     */
    public void getOperationProgress(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnimplementedUnaryCall(getGetOperationProgressMethod(), responseObserver);
    }

    /**
     */
    public void importSdDataFromShimmers(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnimplementedUnaryCall(getImportSdDataFromShimmersMethod(), responseObserver);
    }

    /**
     */
    public void parseSdDataFromPath(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnimplementedUnaryCall(getParseSdDataFromPathMethod(), responseObserver);
    }

    /**
     */
    public void scanSdDataAndCopy(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnimplementedUnaryCall(getScanSdDataAndCopyMethod(), responseObserver);
    }

    /**
     */
    public void clearSdCardData(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnimplementedUnaryCall(getClearSdCardDataMethod(), responseObserver);
    }

    /**
     */
    public void dockAccessSlotWithSdCard(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnimplementedUnaryCall(getDockAccessSlotWithSdCardMethod(), responseObserver);
    }

    /**
     */
    public void dockRestoreAutoTasks(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnimplementedUnaryCall(getDockRestoreAutoTasksMethod(), responseObserver);
    }

    /**
     * <pre>
     *Shimmer device emulation software related
     * </pre>
     */
    public void getInfoSpans(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.InfoSpans> responseObserver) {
      asyncUnimplementedUnaryCall(getGetInfoSpansMethod(), responseObserver);
    }

    /**
     */
    public void getInfoAllShimmers(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> responseObserver) {
      asyncUnimplementedUnaryCall(getGetInfoAllShimmersMethod(), responseObserver);
    }

    /**
     */
    public void getEmulatedDevices(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.EmulatedDevices> responseObserver) {
      asyncUnimplementedUnaryCall(getGetEmulatedDevicesMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getSayHelloMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest,
                com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>(
                  this, METHODID_SAY_HELLO)))
          .addMethod(
            getGetDataStreamMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest,
                com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2>(
                  this, METHODID_GET_DATA_STREAM)))
          .addMethod(
            getSendDataStreamMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2,
                com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>(
                  this, METHODID_SEND_DATA_STREAM)))
          .addMethod(
            getSendFileStreamMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.FileByteTransfer,
                com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>(
                  this, METHODID_SEND_FILE_STREAM)))
          .addMethod(
            getConnectShimmerMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
                com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>(
                  this, METHODID_CONNECT_SHIMMER)))
          .addMethod(
            getDisconnectShimmerMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
                com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>(
                  this, METHODID_DISCONNECT_SHIMMER)))
          .addMethod(
            getStartStreamingMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
                com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>(
                  this, METHODID_START_STREAMING)))
          .addMethod(
            getStopStreamingMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
                com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>(
                  this, METHODID_STOP_STREAMING)))
          .addMethod(
            getCloseApplicationMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest,
                com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>(
                  this, METHODID_CLOSE_APPLICATION)))
          .addMethod(
            getSetWorkspaceDirectoryMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>(
                  this, METHODID_SET_WORKSPACE_DIRECTORY)))
          .addMethod(
            getGetWorkspaceDirectoryMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg>(
                  this, METHODID_GET_WORKSPACE_DIRECTORY)))
          .addMethod(
            getGetDockedShimmerInfoMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo>(
                  this, METHODID_GET_DOCKED_SHIMMER_INFO)))
          .addMethod(
            getGetMadgewickBetaValueMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg>(
                  this, METHODID_GET_MADGEWICK_BETA_VALUE)))
          .addMethod(
            getPairShimmersMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>(
                  this, METHODID_PAIR_SHIMMERS)))
          .addMethod(
            getGetOperationProgressMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>(
                  this, METHODID_GET_OPERATION_PROGRESS)))
          .addMethod(
            getImportSdDataFromShimmersMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>(
                  this, METHODID_IMPORT_SD_DATA_FROM_SHIMMERS)))
          .addMethod(
            getParseSdDataFromPathMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>(
                  this, METHODID_PARSE_SD_DATA_FROM_PATH)))
          .addMethod(
            getScanSdDataAndCopyMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>(
                  this, METHODID_SCAN_SD_DATA_AND_COPY)))
          .addMethod(
            getClearSdCardDataMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>(
                  this, METHODID_CLEAR_SD_CARD_DATA)))
          .addMethod(
            getDockAccessSlotWithSdCardMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>(
                  this, METHODID_DOCK_ACCESS_SLOT_WITH_SD_CARD)))
          .addMethod(
            getDockRestoreAutoTasksMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>(
                  this, METHODID_DOCK_RESTORE_AUTO_TASKS)))
          .addMethod(
            getGetInfoSpansMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.InfoSpans>(
                  this, METHODID_GET_INFO_SPANS)))
          .addMethod(
            getGetInfoAllShimmersMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo>(
                  this, METHODID_GET_INFO_ALL_SHIMMERS)))
          .addMethod(
            getGetEmulatedDevicesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.shimmerresearch.grpc.ShimmerGRPC.StringMsg,
                com.shimmerresearch.grpc.ShimmerGRPC.EmulatedDevices>(
                  this, METHODID_GET_EMULATED_DEVICES)))
          .build();
    }
  }

  /**
   * <pre>
   * The greeter service definition.
   * </pre>
   */
  public static final class ShimmerServerStub extends io.grpc.stub.AbstractAsyncStub<ShimmerServerStub> {
    private ShimmerServerStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ShimmerServerStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ShimmerServerStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sends a greeting
     * </pre>
     */
    public void sayHello(com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSayHelloMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *Client asking for data
     * </pre>
     */
    public void getDataStream(com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetDataStreamMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *Client sending data
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> sendDataStream(
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getSendDataStreamMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.FileByteTransfer> sendFileStream(
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getSendFileStreamMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public void connectShimmer(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getConnectShimmerMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void disconnectShimmer(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDisconnectShimmerMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void startStreaming(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getStartStreamingMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void stopStreaming(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getStopStreamingMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void closeApplication(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCloseApplicationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *ConsensysApi related
     * </pre>
     */
    public void setWorkspaceDirectory(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSetWorkspaceDirectoryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getWorkspaceDirectory(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetWorkspaceDirectoryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getDockedShimmerInfo(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetDockedShimmerInfoMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getMadgewickBetaValue(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetMadgewickBetaValueMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void pairShimmers(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPairShimmersMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getOperationProgress(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetOperationProgressMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void importSdDataFromShimmers(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getImportSdDataFromShimmersMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void parseSdDataFromPath(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getParseSdDataFromPathMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void scanSdDataAndCopy(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getScanSdDataAndCopyMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void clearSdCardData(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getClearSdCardDataMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void dockAccessSlotWithSdCard(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDockAccessSlotWithSdCardMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void dockRestoreAutoTasks(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDockRestoreAutoTasksMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *Shimmer device emulation software related
     * </pre>
     */
    public void getInfoSpans(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.InfoSpans> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetInfoSpansMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getInfoAllShimmers(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetInfoAllShimmersMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getEmulatedDevices(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request,
        io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.EmulatedDevices> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetEmulatedDevicesMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * The greeter service definition.
   * </pre>
   */
  public static final class ShimmerServerBlockingStub extends io.grpc.stub.AbstractBlockingStub<ShimmerServerBlockingStub> {
    private ShimmerServerBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ShimmerServerBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ShimmerServerBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sends a greeting
     * </pre>
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.HelloReply sayHello(com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest request) {
      return blockingUnaryCall(
          getChannel(), getSayHelloMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     *Client asking for data
     * </pre>
     */
    public java.util.Iterator<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2> getDataStream(
        com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest request) {
      return blockingServerStreamingCall(
          getChannel(), getGetDataStreamMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus connectShimmer(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request) {
      return blockingUnaryCall(
          getChannel(), getConnectShimmerMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus disconnectShimmer(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request) {
      return blockingUnaryCall(
          getChannel(), getDisconnectShimmerMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus startStreaming(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request) {
      return blockingUnaryCall(
          getChannel(), getStartStreamingMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus stopStreaming(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request) {
      return blockingUnaryCall(
          getChannel(), getStopStreamingMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus closeApplication(com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request) {
      return blockingUnaryCall(
          getChannel(), getCloseApplicationMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     *ConsensysApi related
     * </pre>
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest setWorkspaceDirectory(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return blockingUnaryCall(
          getChannel(), getSetWorkspaceDirectoryMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.StringMsg getWorkspaceDirectory(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return blockingUnaryCall(
          getChannel(), getGetWorkspaceDirectoryMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo getDockedShimmerInfo(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return blockingUnaryCall(
          getChannel(), getGetDockedShimmerInfoMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg getMadgewickBetaValue(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return blockingUnaryCall(
          getChannel(), getGetMadgewickBetaValueMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest pairShimmers(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return blockingUnaryCall(
          getChannel(), getPairShimmersMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest getOperationProgress(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return blockingUnaryCall(
          getChannel(), getGetOperationProgressMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest importSdDataFromShimmers(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return blockingUnaryCall(
          getChannel(), getImportSdDataFromShimmersMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest parseSdDataFromPath(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return blockingUnaryCall(
          getChannel(), getParseSdDataFromPathMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest scanSdDataAndCopy(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return blockingUnaryCall(
          getChannel(), getScanSdDataAndCopyMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest clearSdCardData(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return blockingUnaryCall(
          getChannel(), getClearSdCardDataMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest dockAccessSlotWithSdCard(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return blockingUnaryCall(
          getChannel(), getDockAccessSlotWithSdCardMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest dockRestoreAutoTasks(com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return blockingUnaryCall(
          getChannel(), getDockRestoreAutoTasksMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     *Shimmer device emulation software related
     * </pre>
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.InfoSpans getInfoSpans(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return blockingUnaryCall(
          getChannel(), getGetInfoSpansMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo getInfoAllShimmers(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return blockingUnaryCall(
          getChannel(), getGetInfoAllShimmersMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.shimmerresearch.grpc.ShimmerGRPC.EmulatedDevices getEmulatedDevices(com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return blockingUnaryCall(
          getChannel(), getGetEmulatedDevicesMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * The greeter service definition.
   * </pre>
   */
  public static final class ShimmerServerFutureStub extends io.grpc.stub.AbstractFutureStub<ShimmerServerFutureStub> {
    private ShimmerServerFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ShimmerServerFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ShimmerServerFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Sends a greeting
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply> sayHello(
        com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getSayHelloMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> connectShimmer(
        com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getConnectShimmerMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> disconnectShimmer(
        com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getDisconnectShimmerMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> startStreaming(
        com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getStartStreamingMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> stopStreaming(
        com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getStopStreamingMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus> closeApplication(
        com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getCloseApplicationMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     *ConsensysApi related
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> setWorkspaceDirectory(
        com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getSetWorkspaceDirectoryMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg> getWorkspaceDirectory(
        com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getGetWorkspaceDirectoryMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> getDockedShimmerInfo(
        com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getGetDockedShimmerInfoMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg> getMadgewickBetaValue(
        com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getGetMadgewickBetaValueMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> pairShimmers(
        com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getPairShimmersMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> getOperationProgress(
        com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getGetOperationProgressMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> importSdDataFromShimmers(
        com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getImportSdDataFromShimmersMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> parseSdDataFromPath(
        com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getParseSdDataFromPathMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> scanSdDataAndCopy(
        com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getScanSdDataAndCopyMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> clearSdCardData(
        com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getClearSdCardDataMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> dockAccessSlotWithSdCard(
        com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getDockAccessSlotWithSdCardMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest> dockRestoreAutoTasks(
        com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getDockRestoreAutoTasksMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     *Shimmer device emulation software related
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.InfoSpans> getInfoSpans(
        com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getGetInfoSpansMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo> getInfoAllShimmers(
        com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getGetInfoAllShimmersMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.shimmerresearch.grpc.ShimmerGRPC.EmulatedDevices> getEmulatedDevices(
        com.shimmerresearch.grpc.ShimmerGRPC.StringMsg request) {
      return futureUnaryCall(
          getChannel().newCall(getGetEmulatedDevicesMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SAY_HELLO = 0;
  private static final int METHODID_GET_DATA_STREAM = 1;
  private static final int METHODID_CONNECT_SHIMMER = 2;
  private static final int METHODID_DISCONNECT_SHIMMER = 3;
  private static final int METHODID_START_STREAMING = 4;
  private static final int METHODID_STOP_STREAMING = 5;
  private static final int METHODID_CLOSE_APPLICATION = 6;
  private static final int METHODID_SET_WORKSPACE_DIRECTORY = 7;
  private static final int METHODID_GET_WORKSPACE_DIRECTORY = 8;
  private static final int METHODID_GET_DOCKED_SHIMMER_INFO = 9;
  private static final int METHODID_GET_MADGEWICK_BETA_VALUE = 10;
  private static final int METHODID_PAIR_SHIMMERS = 11;
  private static final int METHODID_GET_OPERATION_PROGRESS = 12;
  private static final int METHODID_IMPORT_SD_DATA_FROM_SHIMMERS = 13;
  private static final int METHODID_PARSE_SD_DATA_FROM_PATH = 14;
  private static final int METHODID_SCAN_SD_DATA_AND_COPY = 15;
  private static final int METHODID_CLEAR_SD_CARD_DATA = 16;
  private static final int METHODID_DOCK_ACCESS_SLOT_WITH_SD_CARD = 17;
  private static final int METHODID_DOCK_RESTORE_AUTO_TASKS = 18;
  private static final int METHODID_GET_INFO_SPANS = 19;
  private static final int METHODID_GET_INFO_ALL_SHIMMERS = 20;
  private static final int METHODID_GET_EMULATED_DEVICES = 21;
  private static final int METHODID_SEND_DATA_STREAM = 22;
  private static final int METHODID_SEND_FILE_STREAM = 23;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ShimmerServerImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ShimmerServerImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SAY_HELLO:
          serviceImpl.sayHello((com.shimmerresearch.grpc.ShimmerGRPC.HelloRequest) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>) responseObserver);
          break;
        case METHODID_GET_DATA_STREAM:
          serviceImpl.getDataStream((com.shimmerresearch.grpc.ShimmerGRPC.StreamRequest) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ObjectCluster2>) responseObserver);
          break;
        case METHODID_CONNECT_SHIMMER:
          serviceImpl.connectShimmer((com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>) responseObserver);
          break;
        case METHODID_DISCONNECT_SHIMMER:
          serviceImpl.disconnectShimmer((com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>) responseObserver);
          break;
        case METHODID_START_STREAMING:
          serviceImpl.startStreaming((com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>) responseObserver);
          break;
        case METHODID_STOP_STREAMING:
          serviceImpl.stopStreaming((com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>) responseObserver);
          break;
        case METHODID_CLOSE_APPLICATION:
          serviceImpl.closeApplication((com.shimmerresearch.grpc.ShimmerGRPC.ShimmerRequest) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.CommandStatus>) responseObserver);
          break;
        case METHODID_SET_WORKSPACE_DIRECTORY:
          serviceImpl.setWorkspaceDirectory((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
          break;
        case METHODID_GET_WORKSPACE_DIRECTORY:
          serviceImpl.getWorkspaceDirectory((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.StringMsg>) responseObserver);
          break;
        case METHODID_GET_DOCKED_SHIMMER_INFO:
          serviceImpl.getDockedShimmerInfo((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo>) responseObserver);
          break;
        case METHODID_GET_MADGEWICK_BETA_VALUE:
          serviceImpl.getMadgewickBetaValue((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.DoubleMsg>) responseObserver);
          break;
        case METHODID_PAIR_SHIMMERS:
          serviceImpl.pairShimmers((com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
          break;
        case METHODID_GET_OPERATION_PROGRESS:
          serviceImpl.getOperationProgress((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
          break;
        case METHODID_IMPORT_SD_DATA_FROM_SHIMMERS:
          serviceImpl.importSdDataFromShimmers((com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
          break;
        case METHODID_PARSE_SD_DATA_FROM_PATH:
          serviceImpl.parseSdDataFromPath((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
          break;
        case METHODID_SCAN_SD_DATA_AND_COPY:
          serviceImpl.scanSdDataAndCopy((com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
          break;
        case METHODID_CLEAR_SD_CARD_DATA:
          serviceImpl.clearSdCardData((com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
          break;
        case METHODID_DOCK_ACCESS_SLOT_WITH_SD_CARD:
          serviceImpl.dockAccessSlotWithSdCard((com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
          break;
        case METHODID_DOCK_RESTORE_AUTO_TASKS:
          serviceImpl.dockRestoreAutoTasks((com.shimmerresearch.grpc.ShimmerGRPC.StringArrayMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.OperationRequest>) responseObserver);
          break;
        case METHODID_GET_INFO_SPANS:
          serviceImpl.getInfoSpans((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.InfoSpans>) responseObserver);
          break;
        case METHODID_GET_INFO_ALL_SHIMMERS:
          serviceImpl.getInfoAllShimmers((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.ShimmersInfo>) responseObserver);
          break;
        case METHODID_GET_EMULATED_DEVICES:
          serviceImpl.getEmulatedDevices((com.shimmerresearch.grpc.ShimmerGRPC.StringMsg) request,
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.EmulatedDevices>) responseObserver);
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
        case METHODID_SEND_DATA_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.sendDataStream(
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>) responseObserver);
        case METHODID_SEND_FILE_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.sendFileStream(
              (io.grpc.stub.StreamObserver<com.shimmerresearch.grpc.ShimmerGRPC.HelloReply>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class ShimmerServerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ShimmerServerBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.shimmerresearch.grpc.ShimmerGRPC.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ShimmerServer");
    }
  }

  private static final class ShimmerServerFileDescriptorSupplier
      extends ShimmerServerBaseDescriptorSupplier {
    ShimmerServerFileDescriptorSupplier() {}
  }

  private static final class ShimmerServerMethodDescriptorSupplier
      extends ShimmerServerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ShimmerServerMethodDescriptorSupplier(String methodName) {
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
      synchronized (ShimmerServerGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ShimmerServerFileDescriptorSupplier())
              .addMethod(getSayHelloMethod())
              .addMethod(getGetDataStreamMethod())
              .addMethod(getSendDataStreamMethod())
              .addMethod(getSendFileStreamMethod())
              .addMethod(getConnectShimmerMethod())
              .addMethod(getDisconnectShimmerMethod())
              .addMethod(getStartStreamingMethod())
              .addMethod(getStopStreamingMethod())
              .addMethod(getCloseApplicationMethod())
              .addMethod(getSetWorkspaceDirectoryMethod())
              .addMethod(getGetWorkspaceDirectoryMethod())
              .addMethod(getGetDockedShimmerInfoMethod())
              .addMethod(getGetMadgewickBetaValueMethod())
              .addMethod(getPairShimmersMethod())
              .addMethod(getGetOperationProgressMethod())
              .addMethod(getImportSdDataFromShimmersMethod())
              .addMethod(getParseSdDataFromPathMethod())
              .addMethod(getScanSdDataAndCopyMethod())
              .addMethod(getClearSdCardDataMethod())
              .addMethod(getDockAccessSlotWithSdCardMethod())
              .addMethod(getDockRestoreAutoTasksMethod())
              .addMethod(getGetInfoSpansMethod())
              .addMethod(getGetInfoAllShimmersMethod())
              .addMethod(getGetEmulatedDevicesMethod())
              .build();
        }
      }
    }
    return result;
  }
}
