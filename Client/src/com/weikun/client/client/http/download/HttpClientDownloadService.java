package com.weikun.client.client.http.download;

import com.weikun.client.client.base.BaseClientService;
import com.weikun.client.client.http.ProgressCallback;

/**
 * Http客户端下载服务
 * @author weikun
 * */
public class HttpClientDownloadService extends BaseClientService<HttpClientDownloadHandler,HttpClientDownloadChannelInitializer> {
    private static HttpClientDownloadService instance = null;
    public static HttpClientDownloadService getInstance(){
        if (instance == null){
            instance = new HttpClientDownloadService();
        }
        return instance;
    }

    @Override
    public int getPort() {
        return 9003;
    }

    @Override
    public void init() {
        initializer = new HttpClientDownloadChannelInitializer(handler);
    }

    /**
     * 下载文件
     * @param path 下载路径
     * @param url 资源名称
     * @param callback 下载监听
     * */
    private void start(boolean isAsync,String path, String url, ProgressCallback<String> callback){
        handler = new HttpClientDownloadHandler(path,url,callback);
        connect(isAsync);
    }



    public void startDownload(boolean isAsync,String path,String url,ProgressCallback<String> callback) {
        start(isAsync,path,url,callback);
    }

}
