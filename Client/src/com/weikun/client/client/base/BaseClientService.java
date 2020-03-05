package com.weikun.client.client.base;

import com.weikun.client.app.ClientApplication;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;

/**
 * @author weikun
 * @date 2019/12/24
 */
public abstract class BaseClientService<T extends BaseClientChannelHandler, E extends BaseClientChannelInitializer> {
    public EventLoopGroup group;
    public Bootstrap bootstrap;
    public Channel channel;
    public E initializer;
    public T handler;
    private static int connectTimeOut = 6000;

    /**
     * 端口
     * */
    public abstract int getPort();

    /**
     * 初始化操作
     * 需要初始化ChannelInitializer及ChannelHandler
     * */
    public abstract void init();

    /**
     * 连接回调
     * */
    private EventCallback connectCallback;

    /**
     * 设置连接回调
     * */
    public final void setConnectCallback(EventCallback callback) {
        this.connectCallback = callback;
    }

    public static void setConnectTimeOut(int timeOut){
        if (timeOut>0){
            connectTimeOut = timeOut;
        }
    }


    /**
     * 连接远程服务端
     * @param isAsync 是否异步进行
     * */
    public void connect(boolean isAsync) {

        try {
            //执行初始化操作
            init();
//            SslContext sslCtx = null;
//            try {
//                sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
//            } catch (SSLException e) {
//                e.printStackTrace();
//            }
//            initializer.setSsl(sslCtx);
            group = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class)
                    .group(group)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,connectTimeOut)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(initializer);
            ChannelFuture future = bootstrap.connect(ClientApplication.getHost(), getPort());
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        //连接成功
                        if (connectCallback != null) {
                            connectCallback.onSuccess();
                        }
                    } else {
                        //连接失败
                        //这里一定要关闭，不然一直重试会引发OOM
                        channelFuture.channel().close();
                        group.shutdownGracefully();
                        if (connectCallback != null) {
                            connectCallback.onFailed("connect failed");
                        }
                    }
                }
            });
            channel = future.sync().channel();
            if (!isAsync){
                //非异步 则同步阻塞
                channel.closeFuture().sync();
            }
        } catch (Exception e) {
            if (connectCallback != null) {
                connectCallback.onFailed(e.getMessage());
            }
            disconnect();
        }
    }


    /**
     * 断开连接
     */
    public final void disconnect() {
        if (!group.isShutdown()) {
            group.shutdownGracefully();
        }
    }

    /**
     * 返回连接状态
     */
    public final boolean isConnected() {
        if (channel == null) {
            return false;
        }
        return channel.isOpen();
    }

}
