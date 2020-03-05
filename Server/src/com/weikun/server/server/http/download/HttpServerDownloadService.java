/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.weikun.server.server.http.download;


import com.weikun.server.common.LogUtil;
import com.weikun.server.server.base.BaseServerService;
import com.weikun.server.server.base.EventCallback;
import com.weikun.server.server.db.ServerLog;

import java.io.File;

/**
 * 服务端 Http下载服务
 * @author weikun
 * @date 2019/12/16
 * */
public class HttpServerDownloadService extends BaseServerService<HttpServerDownloadChannelInitializer> {
    private static HttpServerDownloadService instance = null;
    public static HttpServerDownloadService getInstance(){
        if (instance == null){
            instance = new HttpServerDownloadService();
        }
        return instance;
    }

    @Override
    public int getPort() {
        return 9003;
    }

    @Override
    public void init() {
        initializer = new HttpServerDownloadChannelInitializer();
    }

    public void run(){
        try {
            setConnectCallback(new EventCallback<Boolean>() {
                @Override
                public void onEvent(Boolean success) {
                    if (success) {
                        ServerLog.saveInfo(channel.remoteAddress(),"download client start success! port:"+getPort());
                        LogUtil.i("download client start success! port:"+getPort());
                    } else {
                        ServerLog.saveInfo(channel.remoteAddress(),"download client start failed!");
                        LogUtil.e("download client start failed!");
                    }
                }
            });
            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean initDir() {
        File dir = new File("download");
        return dir.exists() || dir.mkdirs();
    }

}
