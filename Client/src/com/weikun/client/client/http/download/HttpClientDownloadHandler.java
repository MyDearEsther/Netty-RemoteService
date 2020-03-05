package com.weikun.client.client.http.download;

import com.weikun.client.app.ClientApplication;
import com.weikun.client.client.http.ProgressCallback;
import com.weikun.client.client.service.UserService;
import com.weikun.client.common.LogUtil;
import com.weikun.client.common.MsgType;
import com.weikun.client.client.base.BaseClientChannelHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Http客户端下载处理
 * @author weikun
 * */
public class HttpClientDownloadHandler extends BaseClientChannelHandler<HttpObject> {
    private String path;
    private String url;
    private ProgressCallback<String> progressCallback;

    public HttpClientDownloadHandler(String path, String url, ProgressCallback<String> callback) {
        this.path = path;
        this.url = url;
        this.progressCallback = callback;
    }

    /**
     * 是否开始读块数据
     */
    private boolean readingChunks;

    /**
     * 文件输出流
     */
    private FileOutputStream fOutputStream = null;

    /**
     * 接收总大小
     * */
    private long sum = 0;

    /**
     * 文件总大小
     * */
    private long totalSize = 0;

    /**
     * 下载文件对象
     * */
    private File file;


    @Override
    public void onActive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.i("download service connected:"+ctx.channel().remoteAddress());
        downloadRequest(ctx);
    }

    @Override
    public void onInActive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.i("download service disconnected:"+ctx.channel().remoteAddress());
    }

    @Override
    public void onReceive(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            printResponse(response);
            if (response.decoderResult().isSuccess()&&response.status().code() == MsgType.STATUS_OK) {
                readingChunks = true;
                if (path.length() > 0 && !(path.charAt(path.length() - 1) + "").equals(File.separator)) {
                    path = path + File.separator;
                }
                file = new File(path + url);
                boolean start = true;
                if (!file.exists()) {
                    start = file.createNewFile();
                }
                if (!start) {
                    throw new Exception("file create failed");
                }
                fOutputStream = new FileOutputStream(file);
                totalSize = response.headers().getLong(MsgType.HEADER_FILE_SIZE);
                if (progressCallback!=null){
                    progressCallback.onStart(totalSize);
                }
            }else {
                ctx.channel().close();
            }
            return;
        }
        if (readingChunks && msg instanceof HttpContent) {
            //读取块数据
            HttpContent chunk = (HttpContent) msg;
            ByteBuf buf = chunk.content();
            byte[] cache = new byte[buf.readableBytes()];
            sum += buf.readableBytes();
            while (buf.isReadable()) {
                buf.readBytes(cache);
                fOutputStream.write(cache);
            }
            if (null != fOutputStream) {
                fOutputStream.flush();
            }
            if (progressCallback!=null){
                progressCallback.onProgress((float) sum/totalSize);
            }
            if (chunk instanceof LastHttpContent) {
                readingChunks = false;
                if (null != fOutputStream) {
                    fOutputStream.close();
                    fOutputStream = null;
                }
                ctx.channel().close();
                if (progressCallback!=null){
                    progressCallback.onComplete(sum);
                    progressCallback.onResult(file.getPath());
                }
            }
        }
    }

    @Override
    public void onError(ChannelHandlerContext ctx, Throwable cause) {
        if (progressCallback!=null){
            progressCallback.onError("connection error: " + cause.toString());
        }
        if (ctx.channel().isActive()) {
            ctx.channel().close();
        }
    }

    /**
     * 下载请求
     * */
    private void downloadRequest(ChannelHandlerContext ctx){
        try {
            URI uri =new URI("download");
            /*--======== 构建 http 请求 =========--*/
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.toASCIIString());
            //header设置token (json结构)
            request.headers().set(MsgType.HEADER_FILE_URL,url);
            request.headers().set(MsgType.HEADER_TYPE,MsgType.HEADER_TYPE_DOWNLOAD);
            request.headers().set(MsgType.HEADER_TOKEN, ClientApplication.getTokenString());
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            /*--======== 发送 http 请求 =========--*/
            send(ctx.channel(),request,true);
        }catch (URISyntaxException e){
            e.printStackTrace();
        }
    }
}
