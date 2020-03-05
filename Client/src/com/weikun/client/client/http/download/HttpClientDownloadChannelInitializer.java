package com.weikun.client.client.http.download;

import com.weikun.client.client.base.BaseClientChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * Http客户端下载服务连接初始化
 * @author weikun
 * */
public class HttpClientDownloadChannelInitializer extends BaseClientChannelInitializer<HttpClientDownloadHandler> {


    public HttpClientDownloadChannelInitializer(HttpClientDownloadHandler handler) {
        super(handler);
    }

    @Override
    public void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new HttpClientCodec());
        pipeline.addLast(new HttpContentDecompressor());
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(handler);
    }
}
