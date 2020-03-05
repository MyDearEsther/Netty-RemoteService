package com.weikun.client.common.entity;


import com.weikun.client.common.annotation.XmlType;

public class Config {

    @XmlType(XmlType.TYPE_ELEMENT)
    private String host;
    @XmlType(XmlType.TYPE_ELEMENT)
    private String timeOut;
    @XmlType(XmlType.TYPE_ELEMENT)
    private String name;
    @XmlType(XmlType.TYPE_ELEMENT)
    private String accessToken;
    @XmlType(XmlType.TYPE_ELEMENT)
    private String refreshToken;


    public Config(){

    }

    public Config(String host,int timeOut){
        this.host = host;
        this.timeOut = timeOut+"";
    }

    public void setToken(ClientToken token){
        this.name = token.getName();
        this.accessToken = token.getAccessToken();
        this.refreshToken = token.getRefreshToken();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getTimeOut() {
        return Integer.parseInt(this.timeOut);
    }

    public void setTimeOut(String timeOut) {
        this.timeOut = timeOut;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "Config{" +
                "host='" + host + '\'' +
                ", timeOut='" + timeOut + '\'' +
                ", name='" + name + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }
}
