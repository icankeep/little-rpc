package com.passer.littlerpc;

import com.passer.littlerpc.core.annotation.RpcScan;
import com.passer.littlerpc.core.remoting.transport.netty.server.NettyRpcServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author passer
 * @time 2021/3/28 1:20 下午
 */
@RpcScan(basePackages = "com.passer.littlerpc")
public class NettyServer {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(NettyServer.class);
        NettyRpcServer server = context.getBean(NettyRpcServer.class);
        server.start();
    }
}
