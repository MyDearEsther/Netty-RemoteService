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
package com.weikun.server.server.http.upload;

import com.alibaba.fastjson.JSON;
import com.weikun.server.common.CodeUtil;
import com.weikun.server.common.LogUtil;
import com.weikun.server.common.MsgType;
import com.weikun.server.common.entity.ClientToken;
import com.weikun.server.common.entity.FileData;
import com.weikun.server.server.base.BaseServerChannelHandler;
import com.weikun.server.server.db.DbHelper;
import com.weikun.server.server.db.ServerLog;
import com.weikun.server.server.service.UserService;
import io.netty.channel.*;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * 服务端 Http上传处理
 *
 * @author weikun
 * @date 2019/12/16
 */
@ChannelHandler.Sharable
public class HttpServerUploadChannelHandler extends BaseServerChannelHandler<HttpObject> {

    /**
     * 上传文件目录
     */
    private final String DIR = "upload";

    private String userName;

    /**
     * 文件原始md5值
     */
    private String md5;

    /**
     * 写入文件对象
     */
    private File file = null;

    /**
     * 上传文件名
     */
    private String fileName;

    /**
     * 请求服务
     */
    private String service;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求数据实体
     */
    private Object data;

    /**
     * 是否开始上传
     */
    private boolean isUploading = false;

    /**
     * 客户端Token
     */
    private ClientToken token;

    /**
     * 预计总上传大小
     */
    private long fileSize = 0;

    /**
     * HttpPost请求解码器
     */
    private HttpPostRequestDecoder decoder;
    private HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    @Override
    public void onActive(ChannelHandlerContext ctx) throws Exception {
        ServerLog.saveInfo(ctx.channel().remoteAddress(), "http upload connected");
    }

    @Override
    public void onReceive(ChannelHandlerContext ctx, HttpObject object) throws Exception {
        if (object instanceof HttpRequest) {
            printRequest(object);
            //文件上传请求
            HttpRequest request = (HttpRequest) object;
            if (!request.decoderResult().isSuccess()) {
                LogUtil.e("decode failed!");
                sendError(ctx.channel(), "decode failed!", true);
                return;
            }
            String uri = request.uri();
            if (!MsgType.HEADER_TYPE_UPLOAD.toString().equals(uri)) {
                sendError(ctx.channel(), "upload type error", true);
                return;
            }
            //必须为POST请求
            if (HttpMethod.GET.equals(request.method())) {
                sendError(ctx.channel(), "it is not a post method", true);
                return;
            }
            //获取附带token信息
            String tokenJson = request.headers().get(MsgType.HEADER_TOKEN).toString();
            token = JSON.parseObject(tokenJson, ClientToken.class);
            //验证token有效性
            if (token == null || !UserService.isTokenValid(token)) {
                LogUtil.d(ctx.channel().remoteAddress() + " upload request:token invalid!");
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
                LogUtil.d(ctx.channel().remoteAddress() + " upload request:token error!");
                sendError(ctx.channel(), "token error!", true);
                return;
            }
            //解析请求
            try {
                complete = false;
                parseRequest(ctx, request);
            } catch (Exception e) {
                LogUtil.e(ctx.channel().remoteAddress() + " request upload error parsing request:" + e.getMessage());
                sendError(ctx.channel(), "error parsing request", true);
            }
            return;
        }

        if (isUploading && object instanceof HttpContent) {
            //文件分块接收
            HttpContent chunk = (HttpContent) object;
            try {
                decoder.offer(chunk);
            } catch (HttpPostRequestDecoder.ErrorDataDecoderException e) {
                e.printStackTrace();
                return;
            }
            readHttpDataChunkByChunk();
            if (complete && chunk instanceof LastHttpContent) {
                isUploading = false;
                decoder.destroy();
                decoder = null;
                sendComplete(ctx);
                execute(ctx);
            }
        }
    }

    private void sendComplete(ChannelHandlerContext ctx) {
        LogUtil.i(ctx.channel().remoteAddress() + " upload complete:" + file.getPath());
        HttpResponse response = new DefaultHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(MsgType.HEADER_TYPE, MsgType.HEADER_TYPE_COMPLETE);
        response.headers().set(MsgType.HEADER_FILE_URL, file.getName());
        long size = 0;
        try {
            RandomAccessFile accessFile = new RandomAccessFile(file, "r");
            size = accessFile.length();
            accessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.headers().set(new AsciiString("debug"),"file size:"+size);
        send(ctx.channel(), response, true);
    }

    boolean isInMemory = false;
    boolean renameFile = false;

    /**
     * 读块数据
     */
    private void readHttpDataChunkByChunk() {
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                        FileUpload fileUpload = (FileUpload) data;
                        if (fileUpload.isCompleted()) {
                            isInMemory = fileUpload.isInMemory();
                            renameFile = fileUpload.renameTo(file);
                            decoder.removeHttpDataFromClean(fileUpload);
                            complete = true;
                        }
                    }
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e) {
            //读到结尾会抛出异常 丢弃异常 (官方Example的做法...)
            LogUtil.i("end of file");
        }
    }

    @Override
    public void onError(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ServerLog.saveError(ctx.channel().remoteAddress(), cause.getMessage());
        if (ctx.channel().isActive()) {
            ctx.channel().close();
        }
    }


    @Override
    public void onInActive(ChannelHandlerContext ctx) {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }

    boolean complete = false;

    /**
     * 解析上传文件请求
     *
     * @param ctx     连接通道上下文
     * @param request 请求实体
     */
    private void parseRequest(ChannelHandlerContext ctx, HttpRequest request) throws Exception {
        //文件名
        fileName = request.headers().get(MsgType.HEADER_FILE_NAME).toString();
        //服务名
        service = request.headers().get(MsgType.HEADER_SERVICE).toString();
        //服务方法
        method = request.headers().get(MsgType.HEADER_METHOD).toString();
        //文件总大小
        fileSize = request.headers().getLong(MsgType.HEADER_FILE_SIZE);
        if (fileSize == 0) {
            sendError(ctx.channel(), "file is empty!", true);
            return;
        }
        String json = request.headers().get(MsgType.HEADER_DATA).toString();
        //JSON转为请求请求数据实体
        data = JSON.parseObject(json, FileData.class);

        //附带md5值作为原始值
        md5 = request.headers().get(MsgType.HEADER_FILE_MD5).toString();
        String dir = DIR + File.separator + userName;
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            boolean ret = dirFile.mkdirs();
            if (!ret) {
                sendError(ctx.channel(), "create user upload directory error", true);
                return;
            }
        }
        //上传路径
        String path = dir + File.separator + "upload_" + fileName;
        ServerLog.saveUserLog(ctx.channel().remoteAddress(), userName, MsgType.OP_UPLOAD, "upload file");
        file = new File(path);
        if (isExist(file)) {
            //文件存在秒传
            sendComplete(ctx);
            execute(ctx);
        } else {
            decoder = new HttpPostRequestDecoder(factory, request);
            isUploading = true;
        }

    }

    /**
     * 检查上传文件是否存在
     *
     * @param file 文件对象
     */
    private boolean isExist(File file) {
        if (!file.exists()) {
            return false;
        }
        if (!md5.equals(CodeUtil.getFileMd5(file))) {
            return !file.delete();
        }
        return true;
    }

    /**
     * 校验文件
     * 校验所上传完毕的文件MD5值是否与最初的值相同
     *
     * @return 校验通过
     */
    private boolean checkMd5(ChannelHandlerContext ctx) {
        if (!md5.equals(CodeUtil.getFileMd5(file))) {
            sendError(ctx.channel(), "file verify failed!", true);
            return false;
        }
        return true;
    }

    /**
     * 执行操作
     *
     * @param ctx 连接通道上下文
     */
    private void execute(ChannelHandlerContext ctx) {
        if (!checkMd5(ctx)) {
            return;
        }
    }
}
