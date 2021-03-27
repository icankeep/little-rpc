package com.passer.littlerpc.core.provider.impl;

import com.passer.littlerpc.common.constants.RpcErrorMessageEnum;
import com.passer.littlerpc.common.entity.RpcServiceProperty;
import com.passer.littlerpc.common.exception.RpcException;
import com.passer.littlerpc.common.extension.ExtensionLoader;
import com.passer.littlerpc.core.provider.ServiceProvider;
import com.passer.littlerpc.core.registry.ServiceRegistry;
import com.passer.littlerpc.core.remoting.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServiceProviderImpl implements ServiceProvider {

    private final Map<String, Object> registeredServices;
    private final ServiceRegistry registry;

    public ServiceProviderImpl() {
        this.registeredServices = new ConcurrentHashMap<>();
        this.registry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");

    }

    @Override
    public Object getService(RpcServiceProperty property) {
        String serviceName = property.toRpcServiceName();
        Object service = registeredServices.get(serviceName);
        if (service == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_NOT_FOUND.getMessage());
        }
        return service;
    }

    @Override
    public void addService(Object service, Class<?> serviceClass, RpcServiceProperty property) {
        String serviceName = property.toRpcServiceName();
        if (registeredServices.containsKey(serviceName)) {
            return;
        }
        registeredServices.put(serviceName, service);
        log.info("Add service[{}] successfully.", serviceName);
    }

    @Override
    public void publishService(Object service) {
        publishService(service, RpcServiceProperty.builder().version("").group("").build());
    }

    @Override
    public void publishService(Object service, RpcServiceProperty property) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            Class<?> interfaceClazz = service.getClass().getInterfaces()[0];
            String interfaceName = interfaceClazz.getCanonicalName();
            property.setServiceName(interfaceName);
            this.addService(service, interfaceClazz, property);
            registry.registerService(property.toRpcServiceName(), new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (UnknownHostException e) {
            log.error("publish service exception.", e);
        }
    }
}
