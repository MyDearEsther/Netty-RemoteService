package com.weikun.client.client.http.upload;

import com.alibaba.fastjson.JSONObject;
import com.weikun.client.app.ClientApplication;
import com.weikun.client.client.base.BaseClientChannelHandler;
import com.weikun.client.client.http.ProgressCallback;
import com.weikun.client.common.LogUtil;
import com.weikun.client.common.MsgType;
import com.weikun.client.common.entity.UploadEntity;
import com.weikun.client.common.entity.UploadResult;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;

import java.net.URI;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Http客户端上传处理
 * @author weikun
 */
public class HttpClientUploadHandler extends BaseClientChannelHandler<HttpObject> {
    private UploadEntity entity;
    private ProgressCallback<String> progressCallback;

    /**
     * 构造器
     *
     * @param entity   上传内容
     * @param callback 上传进度回调
     */
    public HttpClientUploadHandler(UploadEntity entity, ProgressCallback<String> callback) {
        this.entity = entity;
        this.progressCallback = callback;
    }

    @Override
    public void onActive(ChannelHandlerContext ctx) throws Exception {
        startUpload(ctx);
    }

    @Override
    public void onInActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void onReceive(ChannelHandlerContext ctx, HttpObject obj) throws Exception {
        if (!obj.decoderResult().isSuccess()){
            LogUtil.e("decode failed!");
            return;
        }
        if (obj instanceof HttpResponse) {
            printResponse(obj.toString());
            HttpResponse response = (HttpResponse) obj;
            //获取响应状态码
            int code = response.status().code();
            if (code == MsgType.STATUS_OK) {
                String type = response.headers().get(MsgType.HEADER_TYPE).toString();
                if (MsgType.HEADER_TYPE_COMPLETE.toString().equals(type)){
                    if (progressCallback!=null){
                        progressCallback.onProgress(1);
                        progressCallback.onComplete(entity.getFile().getSize());
                    }
                }
                return;
            }
            if (code == MsgType.STATUS_FAILED) {
                //错误信息
                String message = response.headers().get(MsgType.HEADER_ERROR).toString();
                if (progressCallback != null) {
                    progressCallback.onError(message);
                }
                if (response.headers().getBoolean(MsgType.HEADER_CLOSE)) {
                    ctx.channel().close();
                }
            }
        }
        if (obj instanceof HttpContent){
            HttpContent content = (HttpContent)obj;
            String json = readByteBuf(content.content());
            UploadResult result = JSONObject.parseObject(json, UploadResult.class);
            if (result==null){
                return;
            }
            if (MsgType.HEADER_TYPE_COMPLETE.toString().equals(result.getType())){
                if (progressCallback!=null){
                    progressCallback.onResult(result.getUrl());
                }
            }else if (MsgType.HEADER_TYPE_FAILED.toString().equals(result.getType())){
                if (progressCallback!=null){
                    progressCallback.onError(result.getMessage());
                }
            }
            ctx.channel().close();
        }
    }

    @Override
    public void onError(ChannelHandlerContext ctx, Throwable cause) {
        if (progressCallback != null) {
            progressCallback.onError("Exception: " + cause.toString());
        }
        if (ctx.channel().isActive()) {
            ctx.channel().close();
        }
    }


    /**
     * 开始上传
     */
    private void startUpload(ChannelHandlerContext ctx) throws Exception{
        URI uri = new URI(MsgType.HEADER_TYPE_UPLOAD.toString());
        /*--======== 构建 http 请求 =========--*/
        HttpRequest uploadRequest = new DefaultHttpRequest(HTTP_1_1, HttpMethod.POST, uri.toASCIIString());
        HttpHeaders headers = uploadRequest.headers();
        //服务
        headers.set(MsgType.HEADER_SERVICE,entity.getService());
        //请求类型：上传文件
        headers.set(MsgType.HEADER_TYPE, MsgType.HEADER_TYPE_UPLOAD);
        //操作方法
        headers.set(MsgType.HEADER_METHOD, entity.getMethod());
        //用户令牌
        headers.set(MsgType.HEADER_TOKEN, ClientApplication.getTokenString());
        //操作文件名
        headers.set(MsgType.HEADER_FILE_NAME, entity.getFile().getFileName());
        //文件长度
        headers.setLong(MsgType.HEADER_FILE_SIZE, entity.getFile().getSize());
        //文件MD5值
        headers.set(MsgType.HEADER_FILE_MD5, entity.getFile().getMd5());
        //文件信息 JSON格式
        headers.set(MsgType.HEADER_DATA, entity.getDataJson());
        try {
            HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
            HttpPostRequestEncoder bodyRequestEncoder = new HttpPostRequestEncoder(factory, uploadRequest, true);
            bodyRequestEncoder.addBodyFileUpload("apk", entity.getFile().getFile(), "application/apk", false);
            bodyRequestEncoder.finalizeRequest();
            /*--======== 发送 http 请求 =========--*/
            send(ctx.channel(), uploadRequest, false);
            if (progressCallback != null) {
                progressCallback.onStart(entity.getFile().getSize());
            }
            if (bodyRequestEncoder.isChunked()) {
                ChannelFuture future = send(ctx.channel(), bodyRequestEncoder, ctx.newProgressivePromise(),true);
                future.addListener(new ChannelProgressiveFutureListener(){
                    @Override
                    public void operationProgressed(ChannelProgressiveFuture channelProgressiveFuture, long l, long l1) throws Exception {
                        if (progressCallback!=null){
                            progressCallback.onProgress((float)l/l1);
                        }
                    }

                    @Override
                    public void operationComplete(ChannelProgressiveFuture channelProgressiveFuture) throws Exception {

                    }
                });
            }
            bodyRequestEncoder.cleanFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
