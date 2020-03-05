package com.weikun.client.client.http.upload;

import com.weikun.client.client.base.BaseClientChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * Http客户端上传服务连接初始化
 * @author weikun
 * */
public class HttpClientUploadChannelInitializer extends BaseClientChannelInitializer<HttpClientUploadHandler> {

    public HttpClientUploadChannelInitializer(HttpClientUploadHandler handler) {
        super(handler);
    }

    @Override
    public void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast("codec",new HttpClientCodec());
        pipeline.addLast("inflater", new HttpContentDecompressor());
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        pipeline.addLast(this.handler);
    }


}
