package com.weikun.client.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author weikun
 * @date 2020/1/15
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface XmlType {
    int TYPE_ATTRIBUTE = 0;
    int TYPE_ELEMENT = 1;
    int value();
}
