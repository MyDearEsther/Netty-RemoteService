package com.weikun.server.server.rpc;

import com.alibaba.fastjson.JSON;
import com.weikun.server.common.LogUtil;
import com.weikun.server.common.MsgType;
import com.weikun.server.common.annotation.AnnotationScannerUtils;
import com.weikun.server.common.annotation.Service;
import com.weikun.server.common.annotation.ServiceMethod;
import com.weikun.server.common.entity.Request;
import com.weikun.server.common.entity.Response;
import com.weikun.server.server.base.BaseServerChannelHandler;
import com.weikun.server.server.db.ServerLog;
import com.weikun.server.server.service.UserService;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author weikun
 * 服务端业务逻辑
 */
public class RpcServerChannelHandler extends BaseServerChannelHandler<Request> {
    private RpcChannelMap rpcChannelMap;
    private Map<String, Object> serviceMap;

    public RpcServerChannelHandler() {
        initRpcService();
    }

    private void initRpcService(){
        rpcChannelMap = new RpcChannelMap();
        serviceMap = new HashMap<>(10);
        try {
            Set<Class> set =
                    AnnotationScannerUtils.getClass4Annotation("com.newland.client.client.service", Service.class);
            for (Class clz : set) {
                Service service = (Service) clz.getAnnotation(Service.class);
                Object obj = clz.newInstance();
                serviceMap.put(service.value(), obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(ChannelHandlerContext ctx, Request request) throws Exception {
        printRequest(request);
        if (request.getType() == MsgType.TYPE_IDLE || request.getType() != MsgType.TYPE_WORK) {
            //心跳数据不做处理
            return;
        }
        ServerLog.saveInfo(ctx.channel().remoteAddress(),"service:"+request.getServiceName()+"\n"
                +"method:"+request.getMethodName()+"\n");
        Response response = new Response();
        response.setRequestId(request.getId());
        try {
            Object result = handler(request,ctx);
            response.setData(result);
            response.setCode(MsgType.STATUS_OK);
        } catch (Throwable e) {
            String message = e.getMessage();
            if (message==null){
                message = e.getCause().getMessage();
            }
            LogUtil.e(message);
            response.setCode(MsgType.STATUS_FAILED);
            response.setMessage("error:"+message);
        }
        send(ctx.channel(),response,true);
    }

    @Override
    public void onError(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ServerLog.saveError(ctx.channel().remoteAddress(),"错误:"+cause.getMessage());
    }

    @Override
    public void onActive(ChannelHandlerContext ctx) throws Exception {
        rpcChannelMap.add(ctx.name(), ctx.channel());
        ServerLog.saveInfo(ctx.channel().remoteAddress(),"connected");
    }

    @Override
    public void onInActive(ChannelHandlerContext ctx) throws Exception {
        rpcChannelMap.remove(ctx.channel());
        ctx.channel().close();
    }




    /**
     * 通过反射，执行本地方法
     *
     * @param request
     * @return
     * @throws Throwable
     */
    private Object handler(Request request,ChannelHandlerContext ctx) throws Throwable {
        Class clz = findClass(request.getServiceName());
        if (clz == null) {
            throw new Throwable("未找到服务类:" + request.getServiceName());
        }
        Object serviceBean = serviceMap.get(request.getServiceName());
        if (serviceBean == null) {
            throw new Throwable("未找到服务对象:" + request.getServiceName());
        }
        Method method = findMethod(clz, request);
        if (method == null) {
            throw new Throwable("未找到方法:" + serviceBean.getClass().getName() + "#" + request.getMethodName());
        }
        method.setAccessible(true);
        return method.invoke(serviceBean, getParameters(ctx,request.getParameterTypes(), request.getParameters()));
    }

    public static Class findClass(String serviceName) {
        try {
            Set<Class> set =
                    AnnotationScannerUtils.getClass4Annotation("com.newland.client.client.service", Service.class);
            for (Class clz : set) {
                Service service = (Service) clz.getAnnotation(Service.class);
                if (service.value().equals(serviceName)) {
                    return clz;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Method findMethod(Class clz, Request request) throws Throwable{
        Method[] methods = clz.getDeclaredMethods();
        for (Method method : methods) {
            ServiceMethod obj = method.getAnnotation(ServiceMethod.class);
            if (obj != null) {
                method.setAccessible(true);
                if (obj.value().equals(request.getMethodName())) {
                    if (obj.verify()){
                        boolean ret = UserService.isTokenValid(request.getToken());
                        if (!ret){
                            throw new Throwable("token invalid!");
                        }
                    }
                    return method;
                }
            }
        }
        return null;
    }


    /**
     * 获取参数列表
     *
     * @param parameterTypes
     * @param parameters
     * @return
     */
    private Object[] getParameters(ChannelHandlerContext ctx,Class<?>[] parameterTypes, Object[] parameters) {
        if (parameters == null || parameters.length == 0) {
            return parameters;
        } else {
            Object[] newParameters = new Object[parameters.length+1];
            newParameters[0] = ctx.channel().remoteAddress();
            for (int i = 0; i < parameters.length; i++) {
                String json = JSON.toJSONString(parameters[i]);
                newParameters[i+1] = JSON.parseObject(json, parameterTypes[i]);
            }
            return newParameters;
        }
    }

}
