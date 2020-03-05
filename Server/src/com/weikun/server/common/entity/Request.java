package com.weikun.server.common.entity;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * 请求实体
 * @author weikun
 * */
public class Request implements Serializable {
    private String id;
    private ClientToken token;
    /**
     * 数据类型
     * */
    private int type;

    /**
     * 服务名
     * */
    private String serviceName;

    /**
     * 方法名称
     * */
    private String methodName;
    /**
     * 参数列表
     * */
    private Object[] parameters;

    /**
     * 参数类型
     * */
    private Class<?>[] parameterTypes;

    public String getId() {
        return this.id;
    }

    public void setId(String requestId) {
        this.id = requestId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public String toJson(){
        return JSON.toJSONString(this);
    }

    public ClientToken getToken() {
        return token;
    }

    public void setToken(ClientToken token) {
        this.token = token;
    }
}
