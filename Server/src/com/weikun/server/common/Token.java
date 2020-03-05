package com.weikun.server.common;


/**
 * @author weikun
 * 令牌
 * */
public class Token{
    /**
     * 会话ID
     * */
    private String sessionId;
    /**
     * 用户标识
     * */
    private String userInfo;

    /**
     * 有效时间
     * */
    private String timeStamp;

    /**
     * 分隔符
     * */
    private static final String SEPARATOR = "&";
    /**
     * 短期访问为1小时
     * */
    private static final long ACCESS_INTERVAL = 60*60*1000;

    /**
     * 长期有效刷新时间 15天
     */
    private static final long REFRESH_INTERVAL = 15*24*60*1000;

    /**
     * Constructor
     * */
    public Token(String sessionId, String userInfo, String timeStamp) {
        this.sessionId = sessionId;
        this.userInfo = userInfo;
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return sessionId + SEPARATOR + userInfo + SEPARATOR + timeStamp;
    }

    /**
     * 获得Base64编码的Token
     * */
    public String getEncodedToken() {
        return CodeUtil.base64Encode(this.toString().getBytes());
    }

    public static String getDecodeToken(String encodedToken){
        byte[] data = CodeUtil.base64Decode(encodedToken);
        if (data==null){
            return null;
        }
        return new String(data);
    }

    public static Token getToken(String token){
        String decodedToken = getDecodeToken(token);
        if (decodedToken==null){
            return null;
        }
        String[] data = decodedToken.split(SEPARATOR);
        if (data.length!=3){
            return null;
        }
        return new Token(data[0],data[1],data[2]);
    }

    /**
     * 生成长期Token
     * */
    public static Token generateRefreshToken(String name){
        long currentTime = System.currentTimeMillis();
        String sessionId = currentTime+"";
        long time = currentTime+REFRESH_INTERVAL;
        String timeStamp = time+"";
        return new Token(sessionId,name,timeStamp);
    }

    /**
     * 刷新短期Token
     */
    public static Token generateAccessToken(String name){
        long currentTime = System.currentTimeMillis();
        String sessionId = currentTime+"";
        long time = currentTime+ACCESS_INTERVAL;
        String timeStamp = time+"";
        return new Token(sessionId,name,timeStamp);
    }

    /**
     * 检查时效性
     */
    public boolean isValid(){
        long validTime = Long.valueOf(this.timeStamp);
        return validTime>System.currentTimeMillis();
    }


    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
