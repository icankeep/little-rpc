package com.passer.littlerpc;

import com.passer.littlerpc.core.annotation.RpcReference;
import com.passer.littlerpc.domain.Example;
import com.passer.littlerpc.service.ExampleService;
import org.springframework.stereotype.Component;

/**
 * @author passer
 * @time 2021/3/28 2:06 下午
 */
@Component
public class ExampleController {
    @RpcReference
    private ExampleService exampleService;

    public void testExampleService() {
        Example example = new Example().builder()
                .exampleName("passer")
                .id(1L)
                .build();
        String hello = exampleService.helloExample(example);
        System.out.println("client:" + hello);
    }
}
