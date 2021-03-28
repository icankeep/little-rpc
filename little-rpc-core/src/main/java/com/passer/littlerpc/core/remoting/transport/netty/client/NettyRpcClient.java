package com.passer.littlerpc.core.remoting.transport.netty.client;

import com.passer.littlerpc.common.constants.CompressTypeEnum;
import com.passer.littlerpc.common.constants.MessageTypeEnum;
import com.passer.littlerpc.common.constants.SerializerTypeEnum;
import com.passer.littlerpc.common.entity.RpcServiceProperty;
import com.passer.littlerpc.common.remoting.dto.RpcMessage;
import com.passer.littlerpc.common.remoting.dto.RpcRequest;
import com.passer.littlerpc.common.remoting.dto.RpcResponse;
import com.passer.littlerpc.core.registry.ServiceDiscovery;
import com.passer.littlerpc.core.remoting.transport.RpcRequestTransport;
import com.passer.littlerpc.core.remoting.transport.netty.codec.RpcMessageDecoder;
import com.passer.littlerpc.core.remoting.transport.netty.codec.RpcMessageEncoder;
import com.passer.littlerpc.common.extension.ExtensionLoader;
import com.passer.littlerpc.common.utils.SingletonFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author passer
 * @time 2021/3/27 5:00 下午
 */
@Slf4j
public class NettyRpcClient implements RpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;
    private final ChannelCache channelCache;
    private final EventLoopGroup eventLoopGroup;
    private final Bootstrap bootstrap;

    public NettyRpcClient() {
        this.eventLoopGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(this.eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        pipeline.addLast(new RpcMessageEncoder());
                        pipeline.addLast(new RpcMessageDecoder());
                        pipeline.addLast(new NettyRpcClientHandler());
                    }
                });

        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        this.channelCache = SingletonFactory.getInstance(ChannelCache.class);
    }

    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("The client has connected [{}] successfully.", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }

    @Override
    public Object sendRpcRequest(RpcRequest request) {
        CompletableFuture<RpcResponse<Object>> completableFuture = new CompletableFuture<>();

        // look up rpc service server from zk
        RpcServiceProperty property = request.toRpcServiceProperty();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(property.toRpcServiceName());

        // get channel for server and client's connection
        Channel channel = getChannel(inetSocketAddress);

        // send rpc request to channel
        if (channel.isActive()) {
            UnprocessedRequests.put(request.getRequestId(), completableFuture);
            RpcMessage rpcMessage = new RpcMessage().builder()
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType((byte) MessageTypeEnum.NORMAL_REQUEST.ordinal())
                    .codec(SerializerTypeEnum.PROTOSTUFF.getCode())
                    .data(request)
                    .build();
            channel.writeAndFlush(rpcMessage).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("Send rpc request successfully, message[{}]", rpcMessage);
                    } else {
                        future.channel().close();
                        completableFuture.completeExceptionally(future.cause());
                        log.error("Send rpc request failed.", future.cause());
                    }
                }
            });
        } else {
            throw new IllegalStateException();
        }
        return completableFuture;
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelCache.getChannel(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            channelCache.addChannel(inetSocketAddress, channel);
        }
        return channel;
    }
}
