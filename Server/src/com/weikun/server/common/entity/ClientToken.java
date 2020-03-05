package com.weikun.server.common.entity;

import java.io.Serializable;

/**
 * @author weikun
 * 客户端Token实体
 * */
public class ClientToken implements Serializable {

    /**
     * 用户名
     * */
    private String name;

    /**
     * 长期token
     * */
    private String refreshToken;

    /**
     * 短期token
     * */
    private String accessToken;

    public ClientToken(String name,String refreshToken, String accessToken) {
        this.name = name;
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
