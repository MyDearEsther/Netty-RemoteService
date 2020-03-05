package com.weikun.server.common;

public interface Callback<T> {
    void onEvent(int code, String msg, T t);
}
