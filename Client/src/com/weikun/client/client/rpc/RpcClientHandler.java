package com.weikun.client.client.rpc;

import com.weikun.client.client.base.BaseClientChannelHandler;
import com.weikun.client.common.LogUtil;
import com.weikun.client.common.MsgType;
import com.weikun.client.common.entity.Request;
import com.weikun.client.common.entity.Response;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.*;

/**
 * @author weikun
 * 客户端业务逻辑
 * */
@ChannelHandler.Sharable
public class RpcClientHandler extends BaseClientChannelHandler<Response> {
    private ConcurrentHashMap<String,LinkedBlockingDeque<Response>> queueMap = new ConcurrentHashMap<>();

    @Override
    public void onActive(ChannelHandlerContext ctx){
        LogUtil.i("已连接到服务器 "+ctx.channel().remoteAddress());
    }

    @Override
    public void onInActive(ChannelHandlerContext ctx){

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //已超过30秒未与服务器进行读写操作,发送心跳消息
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.WRITER_IDLE) {
                //空闲 发送心跳包
                Request request = new Request();
                request.setType(MsgType.TYPE_IDLE);
                ctx.channel().writeAndFlush(request);
            }
        }else{
            super.userEventTriggered(ctx,evt);
        }
    }



    @Override
    public void onReceive(ChannelHandlerContext ctx, Response response) throws Exception{
        String requestId = response.getRequestId();
        printResponse(response);
        LinkedBlockingDeque<Response> queue = queueMap.get(requestId);
        queue.put(response);
        queueMap.remove(requestId);
        ReferenceCountUtil.release(response);
    }

    @Override
    public void onError(ChannelHandlerContext ctx, Throwable cause){
        LogUtil.e("通信服务器发生异常 "+cause);
        ctx.channel().close();
    }



    /**
     * 发送请求
     * @param request 请求实体
     * @param channel 连接通道
     * */
    public LinkedBlockingDeque<Response> sendRequest(Request request, Channel channel) {
        String requestId = request.getId();
        LinkedBlockingDeque<Response> deque = new LinkedBlockingDeque<>();
        queueMap.put(requestId,deque);
        channel.writeAndFlush(request);
        return deque;
    }


}
