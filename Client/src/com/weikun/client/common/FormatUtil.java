package com.weikun.client.common;

/**
 * @author weikun
 * @date 2019/12/16
 * 格式工具
 */
public class FormatUtil {
    private static final long KB = 1024;
    /**
     * Float保留位数
     * @param value Float原值
     * @param num 保留位数
     * */
    public static float remainNum(float value,int num){
        if (num<0){
            return value;
        }
        long scale = (long)Math.pow(10,num);
        return (float)Math.round(value*scale)/scale;
    }

    /**
     * 显示文件大小
     * @param bytes 字节大小
     * @return KB、MB、GB大小
     * */
    public static String formatSize(long bytes){
        float value = 0;
        float kbSize = (float)bytes/KB;
        if (kbSize<KB){
            value = remainNum(kbSize,1);
            return value+"KB";
        }
        float mbSize = (float)kbSize/KB;
        if (mbSize<KB){
            value = remainNum(mbSize,2);
            return value+"MB";
        }
        float gbSize = (float)mbSize/KB;
        value = remainNum(gbSize,2);
        return value+"GB";
    }
}
