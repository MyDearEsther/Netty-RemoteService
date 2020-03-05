package com.weikun.client.client.service;

import com.weikun.client.app.ClientApplication;
import com.weikun.client.client.http.ProgressCallback;
import com.weikun.client.common.XmlUtil;
import com.weikun.client.common.entity.ClientToken;
import com.weikun.client.common.entity.Config;
import com.weikun.client.common.entity.XmlData;
import com.weikun.client.client.rpc.RpcClientService;

import java.io.*;
import java.util.List;

import static com.weikun.client.common.MsgType.*;

public class CommandManager {
    private static final String PATH_CONFIG = "config.xml";

    private static final String CMD_UPLOAD = "upload";
    private static final String CMD_DOWNLOAD = "download";
    private static final String CMD_LOGIN = "login";
    private static final String CMD_LOGOUT = "logout";
    private static final String CMD_IN = "-in";
    private static final String CMD_OUT = "-out";
    private static final String CMD_HELP = "help";
    private static final String CMD_CONFIG = "config";
    private static final String CMD_EXECMD = "exe-cmd";

    private static final String CMD_DEBUG = "-debug";
    private static final String CMD_TEST = "-test";

    private static final String CMD_CONFIG_HOST = "host";
    private static final String CMD_CONFIG_TIMEOUT = "timeout";

    private static final String HELP_UPLOAD = "upload -in [file Path] -out [output directory]";
    private static final String HELP_DOWNLOAD = "download -in [url] -out [directory]";
    private static final String HELP_LOGIN = "login [name] [password]";
    private static final String HELP_CONFIG = "config host [host address] timeout [connection timeout]";
    private static final String HELP_EXECMD = "execmd [your command]";


    public static void printAllHelp() {
        System.out.println(" usage:");
        System.out.println("    login:    " + HELP_LOGIN);
        System.out.println("    download: " + HELP_DOWNLOAD);
        System.out.println("    upload:   " + HELP_DOWNLOAD);
        System.out.println("    config:   " + HELP_CONFIG);
        System.out.println("    execute command:   " + HELP_EXECMD);

    }

    private static void alert() {
        System.out.println("command incorrect!");
        printAllHelp();
    }

    public static void execute(String cmd) throws Throwable {
        String[] args = cmd.split(" ");
        execute(args);
    }

    public static void execute(String[] args) throws Throwable {
        if (args.length == 0) {
            printAllHelp();
            return;
        }
        initConfig();
        switch (args[0]) {
            case CMD_EXECMD:
                StringBuilder str = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    str.append(args[i]);
                    if (i != args.length - 1) {
                        str.append(" ");
                    }
                }
                exeCmd(str.toString());
                break;
            case CMD_LOGOUT:
                logout();
                break;
            case CMD_HELP:
                printAllHelp();
                break;
            case CMD_CONFIG:
                config(args);
                break;
            case CMD_UPLOAD:
                //TODO:upload
                break;
            case CMD_DOWNLOAD:
                download(args);
                break;
            case CMD_LOGIN:
                if (args.length == 2) {
                    throw new Throwable("command incorrect!\nnote: " + HELP_LOGIN);
                }
                boolean ret;
                if (args.length == 1) {
                    ret = login();
                } else {
                    String name = args[1];
                    String password = args[2];
                    ret = login(name, password);
                }
                if (ret) {
                    System.out.println("login success!");
                } else {
                    System.out.println("login failed!");
                }
                break;
            default:
                alert();
        }
    }

    private static void exeCmd(String cmd) throws Throwable {
        if (!checkLogin()) {
            throw new Throwable("login failed");
        }
        if (!RpcClientService.getInstance().isConnected()) {
            RpcClientService.getInstance().connect(true);
        }
        String result = WorkService.exeCmd(cmd);
        System.out.println(result);
    }

    private static void logout() {
        Config config = ClientApplication.getConfig();
        if (config.getName() != null) {
            config.setName(null);
            config.setAccessToken(null);
            config.setRefreshToken(null);
            updateConfigXml();
            System.out.println("user clear.");
        } else {
            System.out.println("no user config.");
        }
    }

    private static boolean login() throws Throwable {
        Console console = System.console();
        if (console != null) {
            String name = console.readLine("enter name: ");
            char[] pwd = console.readPassword("enter password: ");
            return login(name, new String(pwd));
        }
        return false;
    }

    private static boolean login(String name, String password) throws Throwable {
        if (!RpcClientService.getInstance().isConnected()) {
            RpcClientService.getInstance().connect(true);
        }
        ClientToken token = UserService.login(name, password);
        if (token != null) {
            if (!ClientApplication.getToken().isEmpty()) {
                logout();
            }
            ClientApplication.setToken(token);
            updateConfigXml();
            return true;
        }
        return false;
    }


    private static boolean checkLogin() throws Throwable {
        if (ClientApplication.getToken().isEmpty()) {
            System.out.println("no user founded,please login.");
            return login();
        } else {
            if (!RpcClientService.getInstance().isConnected()) {
                RpcClientService.getInstance().connect(true);
            }
            int state = UserService.getTokenStatus();
            if (state == TOKEN_AVAILABLE) {
                return true;
            }
            if (state == TOKEN_ACCESS_INVALID) {
                String newToken = UserService.refreshToken();
                if (newToken != null) {
                    updateConfigXml();
                    return true;
                }
            }
            System.out.println("token is invalid,please retry login.");
            return login();
        }
    }

    private static int len = 0;

    /**
     * 打印进度
     */
    private static void printSchedule(int percent) {
        if (percent > 100) {
            percent = 100;
        }
        for (int i = 0; i < len; i++) {
            System.out.print("\b");
        }
        String text = percent + "%";
        len = text.length();
        System.out.print(text);
    }

    /**
     * 初始化配置 读取配置文件
     */
    private static void initConfig() {
        List<Config> results;
        try {
            results = XmlUtil.readXML(PATH_CONFIG, Config.class.getSimpleName(), Config.class);
            if (results == null || results.size() == 0) {
                return;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return;
        }
        Config config = results.get(0);
        ClientApplication.setConfig(config);
    }

    /**
     * 修改配置
     *
     * @param args 配置命令
     */
    private static void config(String[] args) throws Throwable {
        if (args.length < 3) {
            throw new Throwable("command incorrect!\nnote: " + HELP_CONFIG);
        }
        for (int i = 1; i < args.length; i += 2) {
            if ((i + 1) >= args.length) {
                break;
            }
            String value = args[i + 1];
            switch (args[i]) {
                //远程主机地址
                case CMD_CONFIG_HOST:
                    ClientApplication.setHost(value);
                    break;
                //连接超时
                case CMD_CONFIG_TIMEOUT:
                    ClientApplication.setTimeOut(value);
                    break;
                default:
            }
        }
        updateConfigXml();
    }

    /**
     * 更新配置文件
     */
    private static void updateConfigXml() {
        XmlData data = new XmlData(ClientApplication.getConfig());
        XmlUtil.writeXML(data, PATH_CONFIG);
    }

    private static void download(String[] args) throws Throwable {
        if (!CMD_IN.equals(args[1])||args.length < 3) {
            throw new Throwable("command incorrect!\nnote: " + HELP_DOWNLOAD);
        }
        String dir = "";
        String url = args[2];
        if (args.length>=5&&CMD_OUT.equals(args[3])){
            dir = args[4];
        }
        if (!checkLogin()) {
            throw new Throwable("login failed");
        }
        WorkService.donwloadFile(url, dir, new ProgressCallback<String>() {
            @Override
            public void onStart(long totalSize) {

            }

            @Override
            public void onProgress(float progress) {

            }

            @Override
            public void onComplete(long size) {

            }

            @Override
            public void onResult(String data) {

            }

            @Override
            public void onError(String message) {

            }
        });

    }
}
