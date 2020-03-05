package com.weikun.server.server.db.entity;


import com.weikun.server.common.annotation.XmlType;

/**
 * XML 服务日志
 * @author weikun
 * @date 2020/1/13
 */
public class DbLog {

    @XmlType(XmlType.TYPE_ATTRIBUTE)
    private String type;

    @XmlType(XmlType.TYPE_ATTRIBUTE)
    private String time;

    @XmlType(XmlType.TYPE_ATTRIBUTE)
    private String address;

    @XmlType(XmlType.TYPE_ELEMENT)
    private String message;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
