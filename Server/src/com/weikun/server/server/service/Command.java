package com.weikun.server.server.service;

/**
 * @author weikun
 * @date 2020/1/7
 */
public class Command {
    private static final String[] MODEL_LIST = new String[]{
            "X5", "N900","X1"
    };
    private static final String CMD_APK_SIGN = "apk-sign";

    private static boolean matchModel(String model) {
        for (String m : MODEL_LIST) {
            if (m.equals(model)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查命令是否合法
     */
    public static void checkCmd(String cmdStr) throws Throwable {
        String[] cmd = cmdStr.split(" ");
        if (cmd.length < 3) {
            throw new Throwable("command incorrect!");
        }
        if (!CMD_APK_SIGN.equals(cmd[0])) {
            throw new Throwable("no 'apk-sign' command found!");
        }
        String model = cmd[1];
        if (!matchModel(model)) {
            throw new Throwable("no model matched!");
        }
    }

}
