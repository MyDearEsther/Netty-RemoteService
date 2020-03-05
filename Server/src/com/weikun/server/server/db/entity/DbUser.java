package com.weikun.server.server.db.entity;


import com.weikun.server.common.annotation.XmlType;

import java.io.Serializable;

/**
 * XML 用户信息
 * @author weikun
 * @date 2020/1/12
 */
public class DbUser implements Serializable {

    @XmlType(XmlType.TYPE_ELEMENT)
    private String name;

    @XmlType(XmlType.TYPE_ELEMENT)
    private String code;

    @XmlType(XmlType.TYPE_ELEMENT)
    private String refreshToken;

    @XmlType(XmlType.TYPE_ELEMENT)
    private String accessToken;

    @XmlType(XmlType.TYPE_ELEMENT)
    private String pvKey;

    public DbUser(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public DbUser() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getPvKey() {
        return pvKey;
    }

    public void setPvKey(String pvKey) {
        this.pvKey = pvKey;
    }
}
