package com.weikun.server.server.base;

/**
 * @author weikun
 * @date 2019/12/17
 */
public interface EventCallback<T> {
    void onEvent(T data);
}
