package com.passer.littlerpc.core.remoting.transport.netty.server;

import com.passer.littlerpc.common.constants.CompressTypeEnum;
import com.passer.littlerpc.common.constants.MessageTypeEnum;
import com.passer.littlerpc.common.constants.RpcConstants;
import com.passer.littlerpc.common.constants.SerializerTypeEnum;
import com.passer.littlerpc.common.remoting.dto.RpcMessage;
import com.passer.littlerpc.common.remoting.dto.RpcRequest;
import com.passer.littlerpc.common.remoting.dto.RpcResponse;
import com.passer.littlerpc.common.utils.SingletonFactory;
import com.passer.littlerpc.core.remoting.handler.RpcRequestHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    private final RpcRequestHandler requestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);

    public NettyRpcServerHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof RpcMessage) {
                log.info("Channel read msg: [{}]", msg);
                RpcMessage castMsg = (RpcMessage) msg;
                byte messageType = castMsg.getMessageType();
                RpcMessage rpcMessage = new RpcMessage().builder()
                        .codec(SerializerTypeEnum.PROTOSTUFF.getCode())
                        .compress(CompressTypeEnum.GZIP.getCode())
                        .requestId(castMsg.getRequestId())
                        .build();

                if (messageType == MessageTypeEnum.HEARTBEAT_REQUEST.ordinal()) {
                    rpcMessage.setMessageType((byte) MessageTypeEnum.HEARTBEAT_RESPONSE.ordinal());
                    rpcMessage.setData(RpcConstants.HEARTBEAT_RESPONSE_PONG);
                } else {
                    rpcMessage.setMessageType((byte) MessageTypeEnum.NORMAL_RESPONSE.ordinal());
                    RpcRequest request = (RpcRequest) castMsg.getData();
                    Object result = requestHandler.handle(request);
                    RpcResponse response;
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        response = RpcResponse.success(result, castMsg.getRequestId());
                    } else {
                        response = RpcResponse.fail(result, castMsg.getRequestId());
                        log.error("Channel is not active or writable, message will be dropped.");
                    }
                    rpcMessage.setData(response);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("Idle check happened, so close the connection.");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Netty rpc server handler caught exception.", cause);
        ctx.close();
    }
}
