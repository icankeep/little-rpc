package com.passer.littlerpc.core.annotation;

import java.lang.annotation.*;

/**
 * @author passer
 * @time 2021/3/28 9:26 上午
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RpcService {
    String version() default "";
    String group() default "";
}
