package com.passer.littlerpc.core.remoting.handler;

import com.passer.littlerpc.common.exception.RpcException;
import com.passer.littlerpc.common.remoting.dto.RpcRequest;
import com.passer.littlerpc.common.utils.SingletonFactory;
import com.passer.littlerpc.core.provider.ServiceProvider;
import com.passer.littlerpc.core.provider.impl.ServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class RpcRequestHandler {

    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);

    public Object handle(RpcRequest request) {
        Object service = serviceProvider.getService(request.toRpcServiceProperty());
        return invokeMethod(service, request);
    }

    private Object invokeMethod(Object service, RpcRequest request) {
        try {
            Method method = service.getClass().getMethod(request.getMethodName(), request.getParamTypes());
            Object result = method.invoke(request.getParams());
            log.info("Service[{}] invoke method[{}] successfully.", request.getInterfaceName(), request.getMethodName());
            return request;
        } catch (Exception e) {
            throw new RpcException(String.format("Invoke method[{}] error.", request), e);
        }
    }
}
