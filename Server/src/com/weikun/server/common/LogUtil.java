package com.weikun.server.common;


import com.weikun.server.server.db.ServerLog;

/**
 * @author weikun
 * @date 2019/12/25
 * 日志记录
 */
public class LogUtil {

    /**
     * 0 ALL
     * 1 INFO
     * 2 DEBUG
     * 3 WARN
     * 4 ERROR
     * 5 CLOSE
     */
    private static int level = 0;
    private static final int LEVEL_INFO = 1;
    private static final int LEVEL_DEBUG = 2;
    private static final int LEVEL_WARN = 3;
    private static final int LEVEL_ERROR = 4;
    private static final int LEVEL_CLOSE = 5;


    private static String formatLog(int level, String msg) {
        String tag = "";
        switch (level) {
            case 0:
            case 1:
                tag = "info";
                break;
            case 2:
                tag = "debug";
                break;
            case 3:
                tag = "warn";
                break;
            case 4:
                tag = "error";
                break;
            default:
        }
        String date = DateUtil.getDate(DateUtil.YMDHMSF);
        return date + " " + tag + "/\n" + msg;
    }

    public static void i(String msg) {
        if (level <= LEVEL_INFO) {
            ServerLog.saveLog(false,null,MsgType.LOG_TYPE_INFO,msg);
            System.out.println(formatLog(LEVEL_INFO, msg + "\n"));
        }
    }

    public static void d(String msg) {
        if (level <= LEVEL_DEBUG) {
            ServerLog.saveLog(false,null,MsgType.LOG_TYPE_INFO,msg);
            System.out.println(formatLog(LEVEL_DEBUG, msg + "\n"));
        }
    }

    public static void w(String msg) {
        if (level <= LEVEL_WARN) {
            ServerLog.saveLog(false,null,MsgType.LOG_TYPE_INFO,msg);
            System.out.println(formatLog(LEVEL_WARN, msg + "\n"));
        }
    }

    public static void e(String msg) {
        if (level <= LEVEL_ERROR) {
            ServerLog.saveLog(false,null,MsgType.LOG_TYPE_ERROR,msg);
            System.out.println(formatLog(LEVEL_ERROR, msg + "\n"));
        }
    }
}
