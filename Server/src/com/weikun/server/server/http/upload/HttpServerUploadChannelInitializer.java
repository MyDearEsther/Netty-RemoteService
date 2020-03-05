package com.weikun.server.server.http.upload;

import com.weikun.server.server.base.BaseServerChannelInitializer;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 服务端 Http上传连接通道初始化
 * @author weikun
 * @date 2019/12/16
 * */
public class HttpServerUploadChannelInitializer extends BaseServerChannelInitializer {

    @Override
    public void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpContentCompressor());
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpServerUploadChannelHandler());
    }
}
