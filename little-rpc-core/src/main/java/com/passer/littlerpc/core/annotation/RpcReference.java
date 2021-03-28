package com.passer.littlerpc.core.annotation;

import java.lang.annotation.*;

/**
 * @author passer
 * @time 2021/3/28 9:26 上午
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface RpcReference {
    String group() default "";
    String version() default "";
}
