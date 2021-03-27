package com.passer.littlerpc.core.remoting.transport.netty.client;

import com.passer.littlerpc.common.constants.CompressTypeEnum;
import com.passer.littlerpc.common.constants.MessageTypeEnum;
import com.passer.littlerpc.common.constants.SerializerTypeEnum;
import com.passer.littlerpc.common.remoting.dto.RpcMessage;
import com.passer.littlerpc.common.remoting.dto.RpcResponse;
import com.passer.littlerpc.common.utils.SingletonFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author passer
 * @time 2021/3/27 8:21 下午
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {

    private final NettyRpcClient nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof RpcMessage) {
                RpcMessage castMsg = (RpcMessage) msg;
                if (castMsg.getMessageType() == MessageTypeEnum.HEARTBEAT_RESPONSE.ordinal()) {
                    log.info("client receive heartbeat response: {}", castMsg.getData());
                } else if (castMsg.getMessageType() == MessageTypeEnum.NORMAL_RESPONSE.ordinal()) {
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) castMsg.getData();
                    UnprocessedRequests.complete(rpcResponse);
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
            if (state == IdleState.WRITER_IDLE) {
                SocketAddress socketAddress = ctx.channel().remoteAddress();
                log.info("client idle... , remote address: [{}]", socketAddress.toString());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) socketAddress);
                RpcMessage rpcMessage = new RpcMessage().builder()
                        .messageType((byte) MessageTypeEnum.HEARTBEAT_REQUEST.ordinal())
                        .compress(CompressTypeEnum.GZIP.getCode())
                        .codec(SerializerTypeEnum.PROTOSTUFF.getCode())
                        .build();
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Client handler caught exception.", cause);
        ctx.close();
    }
}
