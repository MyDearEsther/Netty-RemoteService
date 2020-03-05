package com.weikun.client.client.http.upload;

import com.weikun.client.client.base.BaseClientService;
import com.weikun.client.client.http.ProgressCallback;
import com.weikun.client.common.entity.FileData;
import com.weikun.client.common.entity.UploadEntity;

import java.io.File;

/**
 * Http客户端上传服务
 * @author weikun
 * */
public class HttpClientUploadService extends BaseClientService<HttpClientUploadHandler,HttpClientUploadChannelInitializer> {
    private static HttpClientUploadService instance = null;

    public static HttpClientUploadService getInstance(){
        if (instance==null){
            instance = new HttpClientUploadService();
        }
        return instance;
    }


    @Override
    public int getPort() {
        return 9002;
    }

    @Override
    public void init() {
        initializer = new HttpClientUploadChannelInitializer(handler);
    }

    /**
     * 启动文件上传服务
     * */
    private void start(boolean isAsync, UploadEntity entity, ProgressCallback<String> callback){
        handler = new HttpClientUploadHandler(entity,callback);
        connect(isAsync);
    }


    /**
     * 上传文件
     * @param method 操作方法
     * @param data 其他数据
     * @param file 文件对象
     * @param callback 上传进度回调
     * */
    public void startUpload(boolean isAsync, String serviceName, String method, Object data, File file, ProgressCallback<String> callback) throws Throwable{
        if (file == null || !file.exists()) {
            throw new Throwable("file not exist!");
        }
        FileData fileData = new FileData(file);
        UploadEntity entity = new UploadEntity();
        entity.setData(data);
        entity.setFile(fileData);
        entity.setService(serviceName);
        entity.setMethod(method);
        start(isAsync,entity,callback);
    }
}
