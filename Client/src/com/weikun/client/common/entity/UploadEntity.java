package com.weikun.client.common.entity;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * @author weikun
 * @date 2019/12/18
 */
public class UploadEntity implements Serializable {
    String service;
    String method;
    Object data;
    FileData file;

    public String getMethod() {
        return method;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getDataJson(){
        return JSON.toJSONString(this.data);
    }

    public FileData getFile() {
        return file;
    }

    public void setFile(FileData file) {
        this.file = file;
    }
}
