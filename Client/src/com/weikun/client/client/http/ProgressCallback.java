package com.weikun.client.client.http;

/**
 * 传输进度回调
 * @author weikun
 * @date 2019/12/16
 */
public interface ProgressCallback<T> {
    /**
     * 传输开始
     * @param totalSize 准备传输的文件大小
     * */
    void onStart(long totalSize);

    /**
     * 进度监听
     * @param progress 进度值(0~1)
     * */
    void onProgress(float progress);

    /**
     * 传输完毕
     * @param size 传输大小
     * */
    void onComplete(long size);

    /**
     * 响应结果
     * @param data 响应数据
     * */
    void onResult(T data);

    /**
     * 传输错误
     * @param message 错误信息
     * */
    void onError(String message);
}
