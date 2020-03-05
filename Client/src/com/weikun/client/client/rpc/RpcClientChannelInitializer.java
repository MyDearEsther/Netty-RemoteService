package com.weikun.client.client.rpc;

import com.weikun.client.client.base.BaseClientChannelInitializer;
import com.weikun.client.common.json.RequestEncoder;
import com.weikun.client.common.json.ResponseDecoder;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 客户端通道初始化处理
 * @author weikun
 * */
public class RpcClientChannelInitializer extends BaseClientChannelInitializer<RpcClientHandler> {
    public RpcClientChannelInitializer(RpcClientHandler handler){
        super(handler);
    }

    @Override
    public void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new IdleStateHandler(0, 30, 0));
        pipeline.addLast(new ResponseDecoder());
        pipeline.addLast(new RequestEncoder());
        pipeline.addLast(this.handler);
    }

}
