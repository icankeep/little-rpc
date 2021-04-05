package com.passer.littlerpc.service.impl;

import com.passer.littlerpc.core.annotation.RpcService;
import com.passer.littlerpc.domain.Example;
import com.passer.littlerpc.service.ExampleService;

/**
 * @author passer
 * @time 2021/3/28 1:20 下午
 */
@RpcService
public class ExampleServiceImpl implements ExampleService {
    @Override
    public String helloExample(Example example) {
        String hello = String.format("hello, example[%s]", example);
        System.out.println("server:" + hello);
        return hello;
    }
}
