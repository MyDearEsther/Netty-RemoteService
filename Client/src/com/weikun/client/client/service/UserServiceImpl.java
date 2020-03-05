package com.weikun.client.client.service;

/**
 * 用户服务接口代理类
 * @author weikun
 * */
public class UserServiceImpl implements IUserService{
    private static IUserService service;

    @Override
    public String requestLogin(String name) throws Throwable{
        return service.requestLogin(name);
    }

    @Override
    public byte[][] login(String name, byte[] code, String pbKey)throws Throwable {
        return service.login(name,code,pbKey);
    }

    @Override
    public int checkToken(String accessToken,String refreshToken) throws Throwable{
        return service.checkToken(accessToken,refreshToken);
    }

    @Override
    public String refreshToken(String accessToken,String refreshToken) throws Throwable {
        return service.refreshToken(accessToken,refreshToken);
    }
}
