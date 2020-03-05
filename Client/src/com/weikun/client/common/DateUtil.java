package com.weikun.client.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author weikun
 * @date 2019/12/17
 * 日期工具类
 */
public class DateUtil {
    public static final String YMDHMS = "yyyy-MM-dd HH:mm:ss";
    public static final String YMDHMSF = "yyyy-MM-dd HH:mm:ss:SSS";
    public static final String YMD = "yyyy-MM-dd";
    public static final String HMS = "HH:mm:ss";
    public static final String HMSF = "HH:mm:ss:fff";
    public static String getDate(String pattern){
        Date date = new Date();
        DateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    public static long getLongFromDate(String dateStr,String pattern){
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        try {
            Date date = dateFormat.parse(dateStr);
            return date.getTime();
        }catch (ParseException e){
            e.printStackTrace();
        }
        return 0;
    }
}
