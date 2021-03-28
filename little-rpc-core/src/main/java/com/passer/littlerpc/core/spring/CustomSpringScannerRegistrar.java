package com.passer.littlerpc.core.spring;

import com.passer.littlerpc.core.annotation.RpcScan;
import com.passer.littlerpc.core.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * @author passer
 * @time 2021/3/28 9:08 上午
 */
@Slf4j
public class CustomSpringScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private static final String CUSTOM_SPRING_BEAN_BASE_PACKAGE = "com.passer.littlerpc.core.spring";

    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackages";

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annotationAttributes = AnnotationAttributes
                .fromMap(annotationMetadata.getAnnotationAttributes(RpcScan.class.getName()));
        String[] rpcScanPackages = new String[0];
        if (annotationAttributes != null) {
            rpcScanPackages = annotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        if (rpcScanPackages.length == 0) {
            rpcScanPackages = new String[]{((StandardAnnotationMetadata) annotationMetadata).getIntrospectedClass().getPackage().getName()};
        }
        CustomSpringScan springScan = new CustomSpringScan(registry, Component.class);
        CustomSpringScan rpcScan = new CustomSpringScan(registry, RpcService.class);
        if (resourceLoader != null) {
            springScan.setResourceLoader(resourceLoader);
            rpcScan.setResourceLoader(resourceLoader);
        }
        int springScanCount = springScan.scan(CUSTOM_SPRING_BEAN_BASE_PACKAGE);
        int rpcScanCount = rpcScan.scan(rpcScanPackages);
        log.info("spring component count: {}, rpc service count: {}", springScanCount, rpcScanCount);
    }
}
