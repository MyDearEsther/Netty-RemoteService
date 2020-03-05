/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.weikun.server.server.http.download;

import com.alibaba.fastjson.JSON;
import com.weikun.server.common.LogUtil;
import com.weikun.server.common.MsgType;
import com.weikun.server.common.entity.ClientToken;
import com.weikun.server.server.base.BaseServerChannelHandler;
import com.weikun.server.server.db.DbHelper;
import com.weikun.server.server.db.ServerLog;
import com.weikun.server.server.service.UserService;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;

import static io.netty.handler.codec.http.HttpHeaderNames.*;

/**
 * 服务端 Http下载处理
 *
 * @author weikun
 * @date 2019/12/16
 */
public class HttpServerDownloadChannelHandler extends BaseServerChannelHandler<HttpObject> {


    private static final String DOWNLOAD_DIR = "download";
    private String userName;
    @Override
    public void onReceive(final ChannelHandlerContext ctx, HttpObject object) throws Exception {
        if (object instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) object;
            printRequest(request);
            if (!request.decoderResult().isSuccess()) {
                sendError(ctx.channel(), "bad request", true);
                return;
            }
            String uri = request.uri();
            if (!MsgType.HEADER_TYPE_DOWNLOAD.toString().equals(uri)) {
                return;
            }
            //获取附带token信息
            String tokenJson = request.headers().get(MsgType.HEADER_TOKEN).toString();
            ClientToken token = JSON.parseObject(tokenJson, ClientToken.class);
            //验证token有效性
            if (token == null || !UserService.isTokenValid(token)) {
                LogUtil.d("token invalid!");
                sendError(ctx.channel(), "token invalid!", true);
                return;
            }
            try {
                userName = DbHelper.getNameByToken(token.getRefreshToken());
            } catch (Throwable t) {
                sendError(ctx.channel(), t.getMessage(), true);
                return;
            }
            if (userName == null || userName.isEmpty()) {
                LogUtil.d("token error!");
                sendError(ctx.channel(), "token error!", true);
                return;
            }
            ServerLog.saveUserLog(ctx.channel().remoteAddress(),userName,MsgType.OP_DOWNLOAD,"download request");
            String url = request.headers().get(MsgType.HEADER_FILE_URL).toString();
            File file = new File(DOWNLOAD_DIR + File.separator + userName + File.separator + url);
            if (file.isHidden() || !file.exists() || !file.isFile()) {
                sendError(ctx.channel(), "file request not permitted!", true);
                return;
            }
            RandomAccessFile raf;
            try {
                raf = new RandomAccessFile(file, "r");
            } catch (FileNotFoundException ignore) {
                sendError(ctx.channel(), "file not found", true);
                return;
            }
            long fileLength = raf.length();
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            HttpHeaderUtil.setContentLength(response, fileLength);
            response.headers().setLong(MsgType.HEADER_FILE_SIZE, fileLength);
            if (HttpHeaderUtil.isKeepAlive(request)) {
                HttpHeaderUtil.setKeepAlive(response, true);
            }
            send(ctx.channel(), response, true);
            ChunkedFile chunkedFile = new ChunkedFile(raf, 0, fileLength, 8192);
            HttpChunkedInput httpChunkedInput = new HttpChunkedInput(chunkedFile);
            ChannelFuture future = send(ctx.channel(), httpChunkedInput, ctx.newProgressivePromise(), true);
            future.addListener(new ChannelProgressiveFutureListener() {
                @Override
                public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                }

                @Override
                public void operationComplete(ChannelProgressiveFuture future) {
                    ServerLog.saveUserLog(ctx.channel().remoteAddress(),userName,MsgType.OP_DOWNLOAD,"download success");
                }
            });
        }
    }

    @Override
    public void onError(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        LogUtil.e(ctx.channel().remoteAddress()+"/http download client error:"+cause.getMessage());
        if (ctx.channel().isActive()) {
            sendError(ctx.channel(), "http download client error", true);
        }
    }

    @Override
    public void onActive(ChannelHandlerContext ctx) throws Exception {
        ServerLog.saveInfo(ctx.channel().remoteAddress(),"connected");
    }

    @Override
    public void onInActive(ChannelHandlerContext ctx) throws Exception {
        ServerLog.saveInfo(ctx.channel().remoteAddress(),"disconnected");
    }

    private static void setContentTypeHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap m = new MimetypesFileTypeMap();
        String contentType = m.getContentType(file.getPath());
        if (!"application/octet-stream".equals(contentType)) {
            contentType += "; charset=utf-8";
        }
        response.headers().set(CONTENT_TYPE, contentType);
    }
}
