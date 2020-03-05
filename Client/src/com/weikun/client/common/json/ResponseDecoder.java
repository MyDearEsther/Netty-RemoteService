package com.weikun.client.common.json;

import com.alibaba.fastjson.JSON;
import com.weikun.client.common.entity.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 响应JSON解码器
 * @author weikun
 * */
public class ResponseDecoder extends LengthFieldBasedFrameDecoder {

    public ResponseDecoder() {
        super(65535, 0, 4,0,4);
    }

    @Override
    protected Response decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf decode = (ByteBuf) super.decode(ctx, in);
        if (decode==null){
            return null;
        }
        int len = decode.readableBytes();
        byte[] bytes = new byte[len];
        decode.readBytes(bytes);
        return JSON.parseObject(new String(bytes),Response.class);
    }
}
