package com.weikun.server.server.base;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLEngine;

/**
 * @author weikun
 * @date 2019/12/25
 */
public abstract class BaseServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private SslContext sslCtx = null;
    public BaseServerChannelInitializer() {

    }

    public void setSsl(SslContext sslContext){
        this.sslCtx = sslContext;
    }

    /**
     * 初始化连接通道
     */
    public abstract void initPipeline(ChannelPipeline pipeline);

    @Override
    protected final void initChannel(SocketChannel channel) throws Exception {
//        SSLEngine engine = sslCtx.newEngine(channel.alloc());
//        engine.setUseClientMode(false);
//        channel.pipeline().addFirst(new SslHandler(engine));
        initPipeline(channel.pipeline());
    }
}
