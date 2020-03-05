package com.weikun.server.server.rpc;

import com.weikun.server.common.LogUtil;
import com.weikun.server.server.base.BaseServerService;
import com.weikun.server.server.base.EventCallback;
import com.weikun.server.server.db.ServerLog;

/**
 * @author weikun
 * 服务端实体
 */
public class RpcServerService extends BaseServerService<RpcChannelInitializerImpl> {
    private static RpcServerService instance = null;
    public static RpcServerService getInstance(){
        if (instance == null){
            instance = new RpcServerService();
        }
        return instance;
    }

    @Override
    public int getPort() {
        return 9001;
    }

    @Override
    public void init() {
        initializer = new RpcChannelInitializerImpl();
    }

    public void run(){
        try {
            setConnectCallback(new EventCallback<Boolean>() {
                @Override
                public void onEvent(Boolean success) {
                    if (success) {
                        ServerLog.saveInfo(channel.remoteAddress(),"RPC client start success "+getPort());
                        LogUtil.i("rpc client start success! port:"+getPort());
                    } else {
                        ServerLog.saveInfo(channel.remoteAddress(),"RPC client start failed");
                        LogUtil.e("rpc client start failed!");
                    }
                }
            });
            start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
