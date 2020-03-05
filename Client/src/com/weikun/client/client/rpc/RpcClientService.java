package com.weikun.client.client.rpc;

import com.weikun.client.client.base.BaseClientService;
import com.weikun.client.client.service.AbstractService;
import com.weikun.client.client.service.ServiceProxyFactory;
import com.weikun.client.common.LogUtil;
import com.weikun.client.common.MsgType;
import com.weikun.client.common.annotation.AnnotationScannerUtils;
import com.weikun.client.common.annotation.Service;
import com.weikun.client.common.annotation.ServiceImpl;
import com.weikun.client.common.entity.Request;
import com.weikun.client.common.entity.Response;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author linweikun
 * 客户端实体
 */
public class RpcClientService extends BaseClientService<RpcClientHandler, RpcClientChannelInitializer> {

    @Override
    public int getPort() {
        return 9001;
    }

    /**
     * 客户端单例
     */
    private static RpcClientService instance = null;

    /**
     * 获取客户端单例
     */
    public static RpcClientService getInstance() {
        if (instance == null) {
            instance = new RpcClientService();
        }
        return instance;
    }

    /**
     * 绑定服务接口
     */
    @Override
    public void init() {
        HashMap<String, Object> serviceMap = new HashMap<>(10);
        try {
            //查询服务代理注解类
            Set<Class> set1 =
                    AnnotationScannerUtils.getClass4Annotation("com.newland.client.client.service", ServiceImpl.class);
            for (Class<? extends AbstractService> clz : set1) {
                ServiceImpl annotation = clz.getAnnotation(ServiceImpl.class);
                ServiceProxyFactory serviceProxy = new ServiceProxyFactory(this, annotation.value());
                Object service = serviceProxy.create(clz.newInstance());
                serviceMap.put(annotation.value(),service);
            }

            //查询服务实现注解类
            Set<Class> set2 =AnnotationScannerUtils.getClass4Annotation("com.newland.client.client.service", Service.class);
            for (Class<? extends AbstractService> clz : set2) {
                Service annotation = clz.getAnnotation(Service.class);
                Object service = serviceMap.get(annotation.value());
                if (service!=null){
                    clz.newInstance().setService(service);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        handler = new RpcClientHandler();
        initializer = new RpcClientChannelInitializer(handler);
    }


    /**
     * 发送请求
     * @param request 请求实体
     * @param timeOut 超时设置(ms)
     */
    public Response send(final Request request, long timeOut) {
        Response response = new Response();
        final ExecutorService exec = Executors.newFixedThreadPool(1);
        Callable<Response> call = new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                try {
                    return send(request);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        try {
            Future<Response> future = exec.submit(call);
            //任务处理超时时间
            if (timeOut>0){
                response = future.get(timeOut, TimeUnit.MILLISECONDS);
            }else {
                response = future.get();
            }
        } catch (TimeoutException ex) {
            response.setRequestId(request.getId());
            response.setCode(MsgType.STATUS_TIMEOUT);
        } catch (Exception e) {
            response.setRequestId(request.getId());
            response.setMessage(e.getMessage());
            response.setCode(MsgType.STATUS_FAILED);
        }
        // 关闭线程池
        exec.shutdown();
        return response;
    }

    /**
     * 发送请求 等待响应
     *
     * @param request 请求实体
     */
    public Response send(Request request) throws InterruptedException {
        if (channel != null && channel.isActive()) {
            LogUtil.i("connected!");
            LinkedBlockingDeque<Response> queue = handler.sendRequest(request, channel);
            //从远程服务器拿结果 阻塞线程 设置超时时间
            return queue.take();
        } else {
            Response res = new Response();
            res.setCode(MsgType.STATUS_FAILED);
            res.setMessage("未正确连接到服务器,请检查相关配置信息!");
            LogUtil.i("disconnected!");
            return res;
        }
    }


}
