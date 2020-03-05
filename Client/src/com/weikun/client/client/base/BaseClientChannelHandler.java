package com.weikun.client.client.base;

import com.weikun.client.common.LogUtil;
import com.weikun.client.common.MsgType;
import com.weikun.client.common.entity.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.HttpResponse;

/**
 * @author weikun
 * @date 2019/12/24
 */
public abstract class BaseClientChannelHandler<T> extends SimpleChannelInboundHandler<T> {

    /**
     * 消息接收
     *
     * @param ctx 通道处理上下文
     * @param t   消息实体
     */
    abstract public void onReceive(ChannelHandlerContext ctx, T t) throws Exception;

    /**
     * 异常捕获
     *
     * @param ctx   通道处理上下文
     * @param cause 异常
     */
    abstract public void onError(ChannelHandlerContext ctx, Throwable cause) throws Exception;

    /**
     * 连接激活
     *
     * @param ctx 通道处理上下文
     */
    abstract public void onActive(ChannelHandlerContext ctx) throws Exception;

    /**
     * 连接断开
     *
     * @param ctx 通道处理上下文
     */
    abstract public void onInActive(ChannelHandlerContext ctx) throws Exception;

    @Override
    public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        onError(ctx, cause);
    }

    @Override
    public final void channelActive(ChannelHandlerContext ctx) throws Exception {
        onActive(ctx);
    }

    @Override
    public final void channelInactive(ChannelHandlerContext ctx) throws Exception {
        onInActive(ctx);
    }

    @Override
    protected final void messageReceived(ChannelHandlerContext ctx, T t) throws Exception {
        onReceive(ctx, t);
    }


    public void printResponse(Object obj) {
        if (obj instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) obj;
            LogUtil.i("receive http response start------------------------------------\n" + response.headers().toString()
                    + "\nreceive http response end--------------------------------------");
            return;
        }
        if (obj instanceof Response) {
            Response response = (Response) obj;
            LogUtil.i("receive rpc response start------------------------------------\n"
                    + response.getRequestId() + " " + (response.getCode() == MsgType.STATUS_OK ? " OK!" : " FAILED!")
                    + "\nreceive rpc response end--------------------------------------");
            return;
        }
        LogUtil.i("receive response start------------------------------------\n" + obj
                + "\nreceive response end--------------------------------------");
    }

    /**
     * 发送数据
     *
     * @param channel 连接通道实例
     * @param obj     写入数据对象
     * @param flush   是否写后冲刷
     */
    public ChannelFuture send(Channel channel, final Object obj, boolean flush) {
        return send(channel, obj, null, flush);
    }


    public ChannelFuture send(Channel channel, final Object obj, ChannelPromise promise, boolean flush) {
        if (channel == null || !channel.isActive()) {
            LogUtil.e("channel inactive!");
            return null;
        }
        ChannelFutureListener writeListener = new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    LogUtil.i("send success! ------------------------------------\n"+
                            obj.toString()+
                            "\n-------------------------------------------------");
                } else {
                    LogUtil.i("send failed! ------------------------------------\n"+
                            obj.toString()+"\n"+channelFuture.cause().toString()+
                            "\n-------------------------------------------------");
                }
            }
        };
        ChannelFuture writeFuture;
        if (promise == null) {
            writeFuture = channel.write(obj).addListener(writeListener);
        } else {
            writeFuture = channel.write(obj, promise).addListener(writeListener);
        }
        if (flush) {
            channel.flush();
        }
        return writeFuture;
    }


    public String readByteBuf(ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        while (buf.isReadable()) {
            buf.readBytes(data);
        }
        return new String(data);
    }
}
