package com.weikun.client.common;

import java.io.Serializable;
import java.security.KeyPair;

/**
 * 密钥对实体
 * @author weikun
 * */
public class MsgKey implements Serializable {
    private String pbKey;
    private String pvKey;

    public MsgKey(KeyPair keyPair) {
        this.pbKey = CodeUtil.base64Encode(keyPair.getPublic().getEncoded());
        this.pvKey = CodeUtil.base64Encode(keyPair.getPrivate().getEncoded());
    }


    public String getPbKey() {
        return pbKey;
    }

    public String getPvKey() {
        return pvKey;
    }


    public void setPbKey(String pbKey) {
        this.pbKey = pbKey;
    }

    public void setPvKey(String pvKey) {
        this.pvKey = pvKey;
    }


}
