package com.passer.littlerpc;

import com.passer.littlerpc.core.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author passer
 * @time 2021/3/28 1:21 下午
 */
@RpcScan(basePackages = {"com.passer.littlerpc"})
public class NettyClient {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(NettyClient.class);
        ExampleController controller = context.getBean(ExampleController.class);
        controller.testExampleService();
    }
}
