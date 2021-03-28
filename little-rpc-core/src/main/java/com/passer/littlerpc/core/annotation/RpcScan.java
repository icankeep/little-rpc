package com.passer.littlerpc.core.annotation;

import com.passer.littlerpc.core.spring.CustomSpringScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author passer
 * @time 2021/3/28 9:26 上午
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Import(CustomSpringScannerRegistrar.class)
@Documented
public @interface RpcScan {
    String[] basePackages();
}
