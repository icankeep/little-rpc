package com.passer.littlerpc;

import com.passer.littlerpc.common.entity.RpcServiceProperty;
import com.passer.littlerpc.core.annotation.RpcScan;
import com.passer.littlerpc.core.remoting.transport.netty.server.NettyRpcServer;
import com.passer.littlerpc.service.impl.ExampleServiceImpl;
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
//        RpcServiceProperty property = new RpcServiceProperty().builder()
//                .group("")
//                .version("")
//                .build();

//        server.registerService(new ExampleServiceImpl(), property);
        server.start();
    }
}
