package com.weikun.server.server.base;

import com.alibaba.fastjson.JSON;
import com.weikun.server.common.LogUtil;
import com.weikun.server.common.MsgType;
import com.weikun.server.common.entity.Request;
import com.weikun.server.common.entity.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 连接通道处理器 基类
 *
 * @author weikun
 * @date 2019/12/24
 */
@ChannelHandler.Sharable
public abstract class BaseServerChannelHandler<T> extends SimpleChannelInboundHandler<T> {


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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        onError(ctx, cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        onActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        onInActive(ctx);
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, T t) throws Exception {
        onReceive(ctx, t);
    }

    public void printRequest(Object obj) {
        if (obj instanceof HttpRequest){
            HttpRequest request = (HttpRequest)obj;
            LogUtil.i("receive http request:" + request.headers().toString());
            return;
        }
        if (obj instanceof Request){
            Request request = (Request) obj;
            LogUtil.i("receive rpc request:\n" + request.toJson());
            return;
        }
        LogUtil.i("receive request:\n" + obj.toString());
    }

    /**
     * 错误响应
     *
     * @param channel 连接通道实例
     * @param msg     错误信息
     * @param close   是否关闭通道
     */
    public void sendError(Channel channel, String msg, boolean close) {
        DefaultHttpResponse response = new DefaultHttpResponse(HTTP_1_1, BAD_REQUEST);
        response.headers().setBoolean(MsgType.HEADER_CLOSE, close);
        response.headers().set(MsgType.HEADER_ERROR, msg);
        send(channel, response, true);
    }

    /**
     * 发送数据
     *
     * @param channel 连接通道实例
     * @param obj     写入数据对象
     * @param flush   是否写后冲刷
     */
    public ChannelFuture send(Channel channel, Object obj, boolean flush) {
        return send(channel, obj, null, flush);
    }


    public ChannelFuture send(Channel channel, final Object obj, ChannelPromise promise, boolean flush) {
        if (channel == null || !channel.isActive()) {
            LogUtil.e("send failed:\n"+obj.toString() + "\ncause: channel inactive");
            return null;
        }
        ChannelFutureListener writeListener = new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                String text;
                if (obj instanceof Response){
                    text =  ((Response)obj).toJson();
                }else {
                    text = obj.toString();
                }
                if (channelFuture.isSuccess()) {
                    LogUtil.i("send success:" + text);
                } else {
                    LogUtil.i("send failed:\n" + text + "\ncause: " + channelFuture.cause().toString());
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

    public ChannelFuture sendLastContent(Channel channel,String data){
        ByteBuf buf = copiedBuffer(data, CharsetUtil.UTF_8);
        LastHttpContent content = new DefaultLastHttpContent(buf);
        return send(channel, content, true);
    }
}
