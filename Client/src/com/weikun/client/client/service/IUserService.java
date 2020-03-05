package com.weikun.client.client.service;

/**
 * @author weikun
 * 用户服务接口
 * */
public interface IUserService {
    /**
     * 登录请求
     * @param name 用户名
     * @return 服务端公钥1
     * @throws Throwable 请求登录异常
     * */
    String requestLogin(String name) throws Throwable;

    /**
     * 登录验证
     * @param name 用户名
     * @param code 经过 服务端公钥1 加密的md5密码
     * @param pbKey 客户端 公钥2
     * @return 长期token 短期token
     * @throws Throwable 登录异常
     * */
    byte[][] login(String name, byte[] code, String pbKey) throws Throwable;

    /**
     * 验证token
     * @param accessToken 短期token
     * @param refreshToken 长期token
     * @return 验证结果
     * @throws Throwable 验证异常
     * */
    int checkToken(String accessToken, String refreshToken) throws Throwable;

    /**
     * 刷新短期token
     * @param accessToken 短期token
     * @param refreshToken 长期token
     * @return 新短期token
     * @throws Throwable 刷新异常
     * */
    String refreshToken(String accessToken, String refreshToken) throws Throwable;
}
