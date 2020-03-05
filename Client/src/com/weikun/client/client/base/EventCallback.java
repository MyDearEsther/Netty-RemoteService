package com.weikun.client.client.base;

/**
 * @author weikun
 * @date 2019/12/17
 */
public interface EventCallback {
    void onSuccess();
    void onFailed(String msg);
}
