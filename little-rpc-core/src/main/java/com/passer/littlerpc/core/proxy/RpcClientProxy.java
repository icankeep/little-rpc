package com.passer.littlerpc.core.proxy;

import com.passer.littlerpc.common.constants.RpcErrorMessageEnum;
import com.passer.littlerpc.common.entity.RpcServiceProperty;
import com.passer.littlerpc.common.exception.RpcException;
import com.passer.littlerpc.common.remoting.dto.RpcRequest;
import com.passer.littlerpc.common.remoting.dto.RpcResponse;
import com.passer.littlerpc.core.remoting.transport.RpcRequestTransport;
import com.passer.littlerpc.core.remoting.transport.netty.client.NettyRpcClient;
import com.passer.littlerpc.core.remoting.transport.socket.SocketRpcClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author passer
 * @time 2021/3/28 11:10 上午
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private final RpcServiceProperty property;

    private final RpcRequestTransport client;

    public RpcClientProxy(RpcRequestTransport client, RpcServiceProperty property) {
        this.client = client;

        if (property.getGroup() == null) {
            property.setGroup("");
        }
        if (property.getVersion() == null) {
            property.setVersion("");
        }
        this.property = property;
    }

    public RpcClientProxy(RpcRequestTransport client) {
        this.client = client;
        this.property = new RpcServiceProperty().builder()
                .group("")
                .version("")
                .build();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = new RpcRequest().builder()
                .group(property.getGroup())
                .version(property.getVersion())
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args)
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .build();

        RpcResponse rpcResponse = null;
        if (client instanceof NettyRpcClient) {
            CompletableFuture<RpcResponse> future = (CompletableFuture<RpcResponse>) client.sendRpcRequest(rpcRequest);
            rpcResponse = future.get();
        } else if (client instanceof SocketRpcClient) {
            // TODO
        } else {
            throw new RpcException(String.format("The rpc client doesn't support the class[%s].", client.getClass().getName()));
        }

        checkInvokeStatus(rpcRequest, rpcResponse);
        return rpcResponse.getData();
    }

    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    private void checkInvokeStatus(RpcRequest rpcRequest, RpcResponse rpcResponse) {
        if (rpcResponse == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_ERROR, rpcRequest.toString());
        }

        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, rpcRequest.toString());
        }

        if (rpcResponse.getCode() == null || rpcResponse.getCode().intValue() != 200) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_ERROR, rpcRequest.toString());
        }
    }


}
