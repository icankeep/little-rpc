package com.passer.littlerpc.core.spring;

import com.passer.littlerpc.common.entity.RpcServiceProperty;
import com.passer.littlerpc.core.remoting.transport.RpcRequestTransport;
import com.passer.littlerpc.common.extension.ExtensionLoader;
import com.passer.littlerpc.common.utils.SingletonFactory;
import com.passer.littlerpc.core.annotation.RpcReference;
import com.passer.littlerpc.core.annotation.RpcService;
import com.passer.littlerpc.core.provider.ServiceProvider;
import com.passer.littlerpc.core.provider.impl.ServiceProviderImpl;
import com.passer.littlerpc.core.proxy.RpcClientProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @author passer
 * @time 2021/3/28 8:53 上午
 */
@Slf4j
@Component
public class CustomSpringBeanPostProcessor implements BeanPostProcessor {

    private RpcRequestTransport client = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    
    private ServiceProvider provider = SingletonFactory.getInstance(ServiceProviderImpl.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        RpcService annotation = bean.getClass().getAnnotation(RpcService.class);
        if (annotation != null) {
            RpcServiceProperty property = RpcServiceProperty.builder()
                    .group(annotation.group())
                    .version(annotation.version())
                    .build();
            provider.publishService(bean, property);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                RpcServiceProperty property = new RpcServiceProperty().builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version())
                        .build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(client, property);
                Object objectProxy = rpcClientProxy.getProxy(field.getType());
                field.setAccessible(true);
                try {
                    field.set(bean, objectProxy);
                } catch (IllegalAccessException e) {
                    log.error("set proxy error.", e);
                }
            }
        }
        return bean;
    }
}
