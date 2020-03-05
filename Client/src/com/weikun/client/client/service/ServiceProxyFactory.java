package com.weikun.client.client.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.weikun.client.app.ClientApplication;
import com.weikun.client.client.rpc.RpcClientService;
import com.weikun.client.common.Id;
import com.weikun.client.common.MsgType;
import com.weikun.client.common.entity.Request;
import com.weikun.client.common.entity.Response;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;

/**
 * @author weikun
 * RPC服务接口代理
 * */
public class ServiceProxyFactory implements InvocationHandler {
    private RpcClientService client;
    private String serviceName;
    public ServiceProxyFactory(RpcClientService client, String name){
        this.client = client;
        this.serviceName = name;
    }

    public Object create(Object service) {
        return Proxy.newProxyInstance(
                service.getClass().getClassLoader(), service.getClass().getInterfaces(), this);
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Request request = new Request();
        //设置服务名
        request.setServiceName(this.serviceName);
        //设置方法名
        request.setMethodName(method.getName());
        //设置参数列表
        request.setParameters(args);
        //设置参数类型
        request.setParameterTypes(method.getParameterTypes());
        //设置请求ID
        request.setId(Id.getId());
        request.setType(MsgType.TYPE_WORK);
        request.setToken(ClientApplication.getToken());
        //发送请求
        Response response = client.send(request, ClientApplication.getTimeOut());
        Class<?> returnType = method.getReturnType();
        if (response.getCode()== MsgType.STATUS_FAILED){
            throw new Throwable(response.getMessage());
        }else if (response.getCode()== MsgType.STATUS_TIMEOUT){
            throw new Throwable("time out!");
        }
        if (returnType.isPrimitive() || String.class.isAssignableFrom(returnType)){
            return response.getData();
        }else if (Collection.class.isAssignableFrom(returnType)){
            return JSONArray.parseArray(response.getData().toString(),Object.class);
        }else if(Map.class.isAssignableFrom(returnType)){
            return JSON.parseObject(response.getData().toString(), Map.class);
        }else{
            Object data = response.getData();
            return JSONObject.parseObject(JSON.toJSONString(data), returnType);
        }
    }
}
