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
package com.weikun.server.server.http.upload;


import com.weikun.server.common.LogUtil;
import com.weikun.server.server.base.BaseServerService;
import com.weikun.server.server.base.EventCallback;

import java.io.File;

/**
 * 服务端 Http上传服务
 * @author weikun
 * @date 2019/12/16
 * */
public class HttpServerUploadService extends BaseServerService<HttpServerUploadChannelInitializer> {
    private static HttpServerUploadService instance = null;
    public static HttpServerUploadService getInstance(){
        if (instance == null){
            instance = new HttpServerUploadService();
        }
        return instance;
    }

    @Override
    public int getPort() {
        return 9002;
    }

    @Override
    public void init() {
        initializer = new HttpServerUploadChannelInitializer();
    }

    public void run(){
        try {
            setConnectCallback(new EventCallback<Boolean>() {
                @Override
                public void onEvent(Boolean success) {
                    if (success) {
                        LogUtil.i("upload client start success! port:"+getPort());
                    } else {
                        LogUtil.e("upload client start failed!");
                    }
                }
            });
            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean initDir() {
        File dir = new File("upload");
        return dir.exists() || dir.mkdirs();
    }
}
