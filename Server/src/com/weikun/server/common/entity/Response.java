package com.weikun.server.common.entity;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * 响应实体
 * @author weikun
 * */
public class Response implements Serializable {
    /**
     * 请求ID
     * */
    private String requestId;
    /**
     * 响应代码
     * */
    private int code;
    /**
     * 错误信息
     * */
    private String message;
    /**
     * 数据实体
     */
    private Object data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String toJson(){
        return JSON.toJSONString(this);
    }
}
