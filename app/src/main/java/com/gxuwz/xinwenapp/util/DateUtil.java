package com.gxuwz.xinwenapp.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 日期工具类
 * 
 * 作用：
 * 1. 格式化日期时间
 * 2. 计算时间差
 * 3. 转换日期格式
 * 
 * 调用者：
 * 1. NewsAdapter：格式化新闻发布时间
 * 2. NewsDetailActivity：显示新闻发布时间
 */
public class DateUtil {
    private static final String TAG = "DateUtil";
    
    /**
     * 格式化时间字符串
     * @param timeStr 时间字符串
     * @return 格式化后的时间字符串
     */
    public static String formatTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return "未知时间";
        }
        
        try {
            return formatDate(timeStr);
        } catch (Exception e) {
            Log.e(TAG, "格式化时间出错: " + e.getMessage(), e);
            return timeStr;
        }
    }
    
    /**
     * 格式化日期为友好显示格式
     * @param dateStr 日期字符串
     * @return 友好显示的日期字符串
     */
    public static String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "未知时间";
        }
        
        try {
            // 尝试解析多种常见的日期格式
            Date date = parseDate(dateStr);
            if (date == null) {
                Log.w(TAG, "无法解析日期: " + dateStr);
                return dateStr; // 如果无法解析，直接返回原始字符串
            }
            
            long timeGap = (new Date().getTime() - date.getTime()) / 1000; // 转换为秒
            
            if (timeGap < 0) {
                // 处理服务器时间与本地时间不同步的情况
                Log.w(TAG, "发现负时间差: " + timeGap + "秒，日期: " + dateStr);
                return formatSimpleDate(date);
            }
            
            if (timeGap < 60) {
                // 一分钟内
                return "刚刚";
            } else if (timeGap < 3600) {
                // 一小时内
                return timeGap / 60 + "分钟前";
            } else if (timeGap < 86400) {
                // 一天内
                return timeGap / 3600 + "小时前";
            } else if (timeGap < 604800) {
                // 一周内
                return timeGap / 86400 + "天前";
            } else {
                // 超过一周
                return formatSimpleDate(date);
            }
        } catch (Exception e) {
            Log.e(TAG, "格式化日期出错: " + e.getMessage(), e);
            return dateStr;
        }
    }
    
    /**
     * 解析日期字符串为Date对象
     * @param dateStr 日期字符串
     * @return Date对象
     */
    private static Date parseDate(String dateStr) {
        // 尝试多种日期格式
        String[] formats = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy/MM/dd HH:mm:ss",
                "yyyy年MM月dd日 HH:mm:ss",
                "yyyy-MM-dd",
                "MM/dd HH:mm",
                "yyyy-MM-dd HH:mm"
        };
        
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                sdf.setTimeZone(TimeZone.getDefault());
                return sdf.parse(dateStr.trim());
            } catch (ParseException e) {
                // 尝试下一种格式
            }
        }
        
        // 尝试处理特殊格式，如"2小时前"
        try {
            if (dateStr.contains("小时前")) {
                int hours = Integer.parseInt(dateStr.substring(0, dateStr.indexOf("小时前")).trim());
                Date now = new Date();
                return new Date(now.getTime() - hours * 3600 * 1000);
            } else if (dateStr.contains("分钟前")) {
                int minutes = Integer.parseInt(dateStr.substring(0, dateStr.indexOf("分钟前")).trim());
                Date now = new Date();
                return new Date(now.getTime() - minutes * 60 * 1000);
            } else if (dateStr.contains("天前")) {
                int days = Integer.parseInt(dateStr.substring(0, dateStr.indexOf("天前")).trim());
                Date now = new Date();
                return new Date(now.getTime() - days * 86400 * 1000);
            }
        } catch (Exception e) {
            Log.e(TAG, "解析特殊日期格式出错: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * 格式化日期为简单格式（年-月-日）
     * @param date 日期对象
     * @return 格式化后的日期字符串
     */
    private static String formatSimpleDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }
    
    /**
     * 格式化日期为完整格式（年-月-日 时:分:秒）
     * @param date 日期对象
     * @return 格式化后的日期字符串
     */
    public static String formatFullDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }
    
    /**
     * 获取当前日期的字符串表示（年-月-日 时:分:秒）
     * @return 当前日期的字符串表示
     */
    public static String getCurrentDateString() {
        return formatFullDate(new Date());
    }
} 