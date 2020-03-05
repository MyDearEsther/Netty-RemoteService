package com.weikun.client.app;

import com.alibaba.fastjson.JSON;
import com.weikun.client.client.service.CommandManager;
import com.weikun.client.common.CodeUtil;
import com.weikun.client.common.entity.ClientToken;
import com.weikun.client.common.entity.Config;

import java.util.Scanner;

/**
 * @author weikun
 * 客户端应用 全局配置
 */
public class ClientApplication {
    public static void init(String host, int timeOut) {
        config.setHost(host);
        config.setTimeOut(timeOut + "");
        CodeUtil.initAlgorithm(true);
    }



    public static void setToken(ClientToken token){
        config.setToken(token);
    }

    public static ClientToken getToken(){
        return new ClientToken(config.getName(),config.getRefreshToken(),config.getAccessToken());
    }

    public static String getTokenString(){
        ClientToken token = new ClientToken(config.getName(),config.getRefreshToken(),config.getAccessToken());
        return JSON.toJSONString(token);
    }

    public static String getHost() {
        return config.getHost();
    }

    public static void setHost(String host) {
        config.setHost(host);
    }

    public static int getTimeOut(){
        return config.getTimeOut();
    }

    public static void setTimeOut(String timeOut) {
        config.setTimeOut(timeOut);
    }

    private static Config config = new Config("127.0.0.1",0);

    public static void setConfig(Config config) {
        ClientApplication.config = config;
    }

    public static Config getConfig() {
        return ClientApplication.config;
    }

    public static void main(final String[] args) {
        try {
            CommandManager.execute(args);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("press enter to exit.");
        if (scanner.hasNextLine()) {
            scanner.close();
        }
    }

}
