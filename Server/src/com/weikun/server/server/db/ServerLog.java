package com.weikun.server.server.db;

import com.weikun.server.common.*;
import com.weikun.server.server.db.entity.DbLog;
import com.weikun.server.server.db.entity.DbUserLog;
import com.weikun.server.server.db.entity.XmlData;

import java.io.File;
import java.net.SocketAddress;

/**
 * 服务日志
 *
 * @author weikun
 * @date 2020/1/12
 */
public class ServerLog {
    /*
     * 日志根目录
     * */
    private static final String ROOT_DIRECTORY = "log";
    /**
     * 用户日志根目录
     */
    private static final String USER_LOG = ROOT_DIRECTORY + File.separator + "user";
    /**
     * 服务日志根目录
     */
    private static final String SERVER_LOG = ROOT_DIRECTORY + File.separator + "client";


    private static DefaultThreadFactory factory = new DefaultThreadFactory();

    /**
     * 初始化根目录
     */
    public static boolean init() {
        return initDir(USER_LOG) && initDir(SERVER_LOG);
    }

    /**
     * 初始化目录
     *
     * @param path 目录路径
     */
    private static boolean initDir(String path) {
        File dir = new File(path);
        return dir.exists() || dir.mkdirs();
    }


    /**
     * 写日志
     *
     * @param address  客户端地址
     * @param userName 用户名
     * @param type     操作类型
     * @param message  备注信息
     */
    public static void saveUserLog(SocketAddress address, String userName, String type, String message) {
        File userDir = new File(USER_LOG + File.separator + userName);
        if (!userDir.exists()) {
            boolean mkdirRet = userDir.mkdirs();
            if (!mkdirRet) {
                LogUtil.e("mkdir " + userDir.getPath() + " failed!");
                return;
            }
        }
        String time = DateUtil.getDate(DateUtil.YMDHMS);
        DbUserLog dbLog = new DbUserLog();
        dbLog.setTime(time);
        dbLog.setAddress(address.toString());
        dbLog.setMessage(message);
        dbLog.setName(userName);
        dbLog.setType(type);
        XmlData data = new XmlData(dbLog);
        data.setItemName("UserLog");
        data.setRootName("root");
        XmlUtil.writeXML(data, userDir.getPath() + File.separator + "UserLog.xml");
    }


    /**
     * 写服务日志
     *
     * @param address 客户端地址
     * @param type    日志类型
     * @param message 日志内容
     */
    public static void saveLog(boolean flag, SocketAddress address, String type, String message) {
        String time = DateUtil.getDate(DateUtil.YMD);
        String detailTime = DateUtil.getDate(DateUtil.YMDHMS);
        if (!initDir(SERVER_LOG)) {
            return;
        }
        DbLog dbLog = new DbLog();
        dbLog.setTime(detailTime);
        if (address != null) {
            dbLog.setAddress(address.toString());
        }
        dbLog.setMessage(message);
        dbLog.setType(type);
        XmlData data = new XmlData(dbLog);
        data.setItemName("log");
        data.setRootName("root");
        XmlUtil.writeXML(data, SERVER_LOG + File.separator + time + "-detail.xml");
        if (flag) {
            XmlUtil.writeXML(data, SERVER_LOG + File.separator + time + ".xml");
        }
    }


    /**
     * 错误日志
     *
     * @param address 客户端地址
     * @param message 日志内容
     */
    public static void saveError(final SocketAddress address, final String message) {
        factory.newThread(new Runnable() {
            @Override
            public void run() {
                saveLog(true, address, MsgType.LOG_TYPE_ERROR, message);
            }
        }).start();
    }

    /**
     * 普通日志
     *
     * @param address 客户端地址
     * @param message 日志内容
     */
    public static void saveInfo(final SocketAddress address, final String message) {
        factory.newThread(new Runnable() {
            @Override
            public void run() {
                saveLog(true, address, MsgType.LOG_TYPE_INFO, message);
            }
        }).start();
    }

}
