package com.passer.littlerpc.core.provider;

import com.passer.littlerpc.common.entity.RpcServiceProperty;

public interface ServiceProvider {
    /**
     *
     * @param property
     * @return
     */
    Object getService(RpcServiceProperty property);

    /**
     *
     * @param service
     * @param serviceClass
     * @param property
     */
    void addService(Object service, Class<?> serviceClass, RpcServiceProperty property);

    /**
     *
     * @param object
     */
    void publishService(Object object);

    /**
     *
     * @param service
     * @param property
     */
    void publishService(Object service, RpcServiceProperty property);
}
