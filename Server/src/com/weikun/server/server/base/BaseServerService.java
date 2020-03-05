package com.weikun.server.server.base;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import sun.security.pkcs.SigningCertificateInfo;

import javax.net.ssl.SSLException;

/**
 * @author weikun
 * @date 2019/12/24
 */
public abstract class BaseServerService<I extends BaseServerChannelInitializer> {
    public NioEventLoopGroup bossGroup;
    public NioEventLoopGroup workGroup;
    public ServerBootstrap bootstrap;
    public Channel channel;
    public I initializer;

    /**
     * 自定义端口
     *
     * @return 端口
     */
    public abstract int getPort();


    /**
     * 初始化操作
     * 需要初始化ChannelInitializer
     */
    public abstract void init();

    private EventCallback<Boolean> connectCallback;

    protected final void setConnectCallback(EventCallback<Boolean> callback) {
        this.connectCallback = callback;
    }

    protected final void start() {
        init();
//        SslContext sslCtx = null;
//        try {
//            SelfSignedCertificate cert = new SelfSignedCertificate();
//            sslCtx = SslContext.newServerContext(cert.certificate(),cert.privateKey());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        initializer.setSsl(sslCtx);
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        try {
            bootstrap.channel(NioServerSocketChannel.class)
                    .group(bossGroup, workGroup)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(initializer);
            ChannelFuture future = bootstrap.bind(getPort()).sync();
            channel = future.channel();
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        if (connectCallback != null) {
                            connectCallback.onEvent(true);
                        }
                    } else {
                        //连接失败
                        //这里一定要关闭，不然一直重试会引发OOM
                        channelFuture.channel().close();
                        bossGroup.shutdownGracefully();
                        workGroup.shutdownGracefully();
                        if (connectCallback != null) {
                            connectCallback.onEvent(false);
                        }
                    }
                }
            });
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
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
