package com.weikun.client.client.base;

import com.weikun.client.common.LogUtil;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;

/**
 * @author weikun
 * @date 2019/12/25
 */
public abstract class BaseClientChannelInitializer<T extends BaseClientChannelHandler> extends ChannelInitializer<SocketChannel> {
    public T handler;
    public SslContext sslCtx;
    public BaseClientChannelInitializer(T handler) {
        super();
        this.handler = handler;
    }

    public void setSsl(SslContext sslContext){
        this.sslCtx = sslContext;
        if (this.sslCtx!=null){
            LogUtil.i("set ssl");
        }
    }

    /**
     * 初始化ChannelPipeline
     * @param pipeline 连接管道
     * */
    public abstract void initPipeline(ChannelPipeline pipeline);

    @Override
    protected final void initChannel(SocketChannel channel) throws Exception {
        initPipeline(channel.pipeline());
//        channel.pipeline().addLast(sslCtx.newHandler(channel.alloc()));
    }
}
