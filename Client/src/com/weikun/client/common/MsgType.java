package com.weikun.client.common;

import io.netty.handler.codec.AsciiString;

/**
 * 消息类型
 * @author weikun
 * */
public interface MsgType {

    /**
     * 消息类型：心跳包
     * */
    int TYPE_IDLE = 0;
    /**
     * 消息类型：业务包
     * */
    int TYPE_WORK = 1;
    /**
     * 响应类型 成功
     * */
    int STATUS_OK = 200;
    /**
     * 响应类型 失败
     * */
    int STATUS_FAILED = 400;
    /**
     * 响应类型 超时
     * */
    int STATUS_TIMEOUT = 404;


    /**
     * 令牌有效
     * */
    int TOKEN_AVAILABLE = 0;

    /**
     * 短期令牌失效
     * */
    int TOKEN_ACCESS_INVALID = 1;
    /**
     * 长期令牌失效
     * */
    int TOKEN_REFRESH_INVALID = 2;

    /**
     * 令牌无效
     * */
    int TOKEN_INVALID = -1;

    String SERVICE_USER = "/user";
    String SERVICE_SIGN = "/sign";

    String OP_LOGIN = "login";
    String OP_SIGN_APK = "sign-apk";


    AsciiString HEADER_TOKEN = new AsciiString("token");
    AsciiString HEADER_TYPE = new AsciiString("type");
    AsciiString HEADER_FILE_SIZE = new AsciiString("file-size");
    AsciiString HEADER_FILE_URL = new AsciiString("file-url");
    AsciiString HEADER_METHOD = new AsciiString("method");
    AsciiString HEADER_FILE_NAME = new AsciiString("file-name");
    AsciiString HEADER_FILE_MD5 = new AsciiString("file-md5");
    AsciiString HEADER_DATA = new AsciiString("data");
    AsciiString HEADER_CLOSE= new AsciiString("close");
    AsciiString HEADER_ERROR= new AsciiString("error");
    AsciiString HEADER_SERVICE= new AsciiString("service");


    AsciiString HEADER_TYPE_UPLOAD = new AsciiString("upload");
    AsciiString HEADER_TYPE_FAILED= new AsciiString("failed");
    AsciiString HEADER_TYPE_DOWNLOAD = new AsciiString("download");
    AsciiString HEADER_TYPE_COMPLETE= new AsciiString("complete");
}
