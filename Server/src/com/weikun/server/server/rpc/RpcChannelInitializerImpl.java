package com.weikun.server.server.rpc;

import com.weikun.server.common.json.RequestDecoder;
import com.weikun.server.common.json.ResponseEncoder;
import com.weikun.server.server.base.BaseServerChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author weikun
 * Channel初始化
 * */
public class RpcChannelInitializerImpl extends BaseServerChannelInitializer {

    @Override
    public void initPipeline(ChannelPipeline pipeline) {
        //超过60秒未读写数据 关闭此连接
        pipeline.addLast(new IdleStateHandler(0, 0, 60));
        pipeline.addLast(new RequestDecoder());
        pipeline.addLast(new ResponseEncoder());
        //设置ChannelHandler 用于处理该连接的IO事件
        pipeline.addLast(new RpcServerChannelHandler());
    }

}
