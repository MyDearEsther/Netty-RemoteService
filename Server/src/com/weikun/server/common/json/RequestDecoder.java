package com.weikun.server.common.json;

import com.alibaba.fastjson.JSON;
import com.weikun.server.common.entity.Request;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class RequestDecoder extends LengthFieldBasedFrameDecoder {

    public RequestDecoder() {
        super(65535, 0, 4,0,4);
    }

    @Override
    protected Request decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf decode = (ByteBuf) super.decode(ctx, in);
        if (decode==null){
            return null;
        }
        int len = decode.readableBytes();
        byte[] bytes = new byte[len];
        decode.readBytes(bytes);
        return JSON.parseObject(new String(bytes), Request.class);
    }
}
