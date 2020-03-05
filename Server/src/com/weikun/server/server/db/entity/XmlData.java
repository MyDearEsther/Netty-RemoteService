package com.weikun.server.server.db.entity;

/**
 * XML数据封装
 * @author weikun
 * @date 2020/1/13
 */
public class XmlData {

    /**
     * 根节点名称
     * */
    private String rootName;

    /**
     * 节点名称
     * */
    private String itemName;

    /**
     * 节点数据
     * */
    private Object data;

    public XmlData(Object data){
        this.data = data;
    }

    public String getRootName() {
        return rootName;
    }

    public void setRootName(String rootName) {
        this.rootName = rootName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
