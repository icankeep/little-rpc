package com.passer.littlerpc.core.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

/**
 * @author passer
 * @time 2021/3/28 8:49 上午
 */
public class CustomSpringScan extends ClassPathBeanDefinitionScanner {

    public CustomSpringScan(BeanDefinitionRegistry registry, Class<? extends Annotation> anno) {
        super(registry);
        super.addIncludeFilter(new AnnotationTypeFilter(anno));
    }
}
