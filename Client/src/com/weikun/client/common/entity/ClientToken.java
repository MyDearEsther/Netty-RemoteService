package com.weikun.client.common.entity;

import com.weikun.client.common.annotation.XmlType;

import java.io.Serializable;

/**
 * 客户端存放Token实体
 */
public class ClientToken implements Serializable {

    /**
     * 用户名
     */
    @XmlType(XmlType.TYPE_ELEMENT)
    private String name;

    /**
     * 长期token
     */
    @XmlType(XmlType.TYPE_ELEMENT)
    private String refreshToken;

    /**
     * 短期token
     */
    @XmlType(XmlType.TYPE_ELEMENT)
    private String accessToken;

    public ClientToken(String name, String refreshToken, String accessToken) {
        this.name = name;
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    public ClientToken() {

    }

    public boolean isEmpty() {
        return name == null || accessToken == null || refreshToken == null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
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
