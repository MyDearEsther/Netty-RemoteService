package com.weikun.server.app;

import com.weikun.server.common.DefaultThreadFactory;
import com.weikun.server.server.db.ServerLog;
import com.weikun.server.server.http.download.HttpServerDownloadService;
import com.weikun.server.server.http.upload.HttpServerUploadService;
import com.weikun.server.server.rpc.RpcServerService;
import com.weikun.server.server.service.UserService;

/**
 * @author weikun
 * 服务端应用
 */
public class ServerApplication {
    private static DefaultThreadFactory threadFactory = new DefaultThreadFactory();
    private static Thread rpcThread = threadFactory.newThread(new Runnable() {
        @Override
        public void run() {
            RpcServerService.getInstance().run();
        }
    });
    private static Thread uploadThread = threadFactory.newThread(new Runnable() {
        @Override
        public void run() {
            HttpServerUploadService.getInstance().run();
        }
    });
    private static Thread downloadThread = threadFactory.newThread(new Runnable() {
        @Override
        public void run() {
            HttpServerDownloadService.getInstance().run();
        }
    });

    public static void main(String[] args) {
        init();
        if (args.length==0){
            return;
        }
        if ("run".equals(args[0])){
            rpcThread.start();
            uploadThread.start();
            downloadThread.start();
        }else if ("register".equals(args[0])){
            try {
                UserService.registerUser(args[1],args[2]);
            }catch (Throwable t){
                t.printStackTrace();
            }
        }
    }

    public static void init(){
        HttpServerDownloadService.initDir();
        HttpServerUploadService.initDir();
        ServerLog.init();
    }
}
