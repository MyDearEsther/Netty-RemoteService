package com.weikun.server.common.entity;

import java.io.Serializable;

/**
 * @author weikun
 * @date 2020/1/3
 * 上传结果
 */
public class UploadResult implements Serializable {
    private String type;
    private String url;
    private String message;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
