package com.passer.littlerpc.core.provider.impl;

import com.passer.littlerpc.common.entity.RpcServiceProperty;
import com.passer.littlerpc.core.provider.ServiceProvider;

public class ServiceProviderImpl implements ServiceProvider {
    @Override
    public Object getService(RpcServiceProperty property) {
        return null;
    }

    @Override
    public void addService(Object service, Class<?> serviceClass, RpcServiceProperty property) {

    }

    @Override
    public void publishService(Object object) {

    }

    @Override
    public void publishService(Object service, RpcServiceProperty property) {

    }
}
