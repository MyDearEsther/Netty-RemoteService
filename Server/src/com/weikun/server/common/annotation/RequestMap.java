package com.weikun.server.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author weikun
 * @date 2019/12/19
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMap {
    /**
     * 路由的uri
     *
     * @return
     */
    String uri();
    /**
     * 路由的方法
     *
     * @return
     */
    String method();
}
