package com.weikun.client.client.service;


import com.weikun.client.app.ClientApplication;
import com.weikun.client.common.CodeUtil;
import com.weikun.client.common.LogUtil;
import com.weikun.client.common.MsgKey;
import com.weikun.client.common.MsgType;
import com.weikun.client.common.entity.ClientToken;

/**
 * @author weikun
 * 用户服务实现类
 */
public class UserService extends AbstractService{
    /**
     * 用户服务接口
     * */
    private static IUserService service;

    /**
     * 设置代理对象
     * @param impl 代理对象
     * */
    @Override
    public void setService(Object impl){
        service = (IUserService) impl;
    }

    /**
     * 请求登录
     * @param name 用户名
     * @return 服务端公钥1
     */
    public static String requestLogin(String name) throws Throwable {
        return service.requestLogin(name);
    }

    /**
     * 登录验证
     *
     * @param name         用户名
     * @param password     明文密码
     * @param serverPbKey 服务端公钥1
     * @return token对
     */
    private static ClientToken verifyLogin(String name, String password, String serverPbKey) throws Throwable {
        String md5Pwd = CodeUtil.md5Encode(password);
        if (md5Pwd == null) {
            throw new Throwable("MD5加密失败");
        }
        byte[] code = CodeUtil.encryptText(md5Pwd.getBytes(), serverPbKey);
        if (code==null){
            LogUtil.e("加密失败");
            return null;
        }
        MsgKey key = CodeUtil.generateKeyPair();
        if (key == null) {
            throw new Throwable("生成密钥对失败");
        }
        byte[][] clientToken = service.login(name, code, key.getPbKey());
        if (clientToken == null) {
            return null;
        }
        byte[] refreshTokenBytes = CodeUtil.decryptText(clientToken[0], key.getPvKey());
        byte[] accessTokenBytes = CodeUtil.decryptText(clientToken[1], key.getPvKey());
        if (accessTokenBytes == null || refreshTokenBytes == null) {
            return null;
        }
        ClientToken token = new ClientToken(name,new String(refreshTokenBytes),new String(accessTokenBytes));
        ClientApplication.setToken(token);
        return token;
    }

    /**
     * 登录操作
     * @param name 用户名
     * @param password 密码
     * @return token对
     */
    public static ClientToken login(String name,String password)throws Throwable{
        String pbKey = requestLogin(name);
        return verifyLogin(name,password,pbKey);
    }

    /**
     * 获取Token状态
     * @return 状态标志
     */
    public static int getTokenStatus() throws Throwable {
        ClientToken token = ClientApplication.getToken();
        return service.checkToken(token.getAccessToken(),token.getRefreshToken());
    }

    /**
     * 获取Token状态
     * @return 状态描述
     */
    public static String getTokenStatus(ClientToken token) throws Throwable {
        int status = service.checkToken(token.getAccessToken(),token.getRefreshToken());
        String message;
        switch (status) {
            case MsgType.TOKEN_REFRESH_INVALID:
                message = "长期token过期";
                break;
            case MsgType.TOKEN_ACCESS_INVALID:
                message = "访问token过期";
                break;
            case MsgType.TOKEN_AVAILABLE:
                message = "token有效";
                break;
            case MsgType.TOKEN_INVALID:
                message = "token无效";
                break;
            default:
                message = "内部错误";
        }
        return message;

    }

    /**
     * 刷新短期token
     * @return 新短期token
     */
    public static String refreshToken() throws Throwable {
        ClientToken token = ClientApplication.getToken();
        String newAccessToken = service.refreshToken(token.getAccessToken(),token.getRefreshToken());
        if (newAccessToken==null){
            return null;
        }
        token.setAccessToken(newAccessToken);
        ClientApplication.setToken(token);
        return newAccessToken;
    }

}
