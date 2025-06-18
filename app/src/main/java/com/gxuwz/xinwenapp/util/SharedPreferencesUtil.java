package com.gxuwz.xinwenapp.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences工具类
 * 
 * 作用：
 * 1. 封装SharedPreferences操作
 * 2. 提供简单的接口保存和读取数据
 * 3. 用于保存用户信息、设置等小型数据
 * 
 * 调用者：
 * 1. UserController：保存用户登录状态
 * 2. ProfileFragment：获取用户ID
 * 3. App各处需要获取/保存配置的地方
 */
public class SharedPreferencesUtil {
    private static final String PREFERENCE_NAME = "xinwen_app_prefs";
    
    /**
     * 保存字符串值
     * @param context 上下文
     * @param key 键
     * @param value 值
     */
    public static void putString(Context context, String key, String value) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
    
    /**
     * 获取字符串值
     * @param context 上下文
     * @param key 键
     * @param defaultValue 默认值
     * @return 保存的字符串值，如果不存在则返回默认值
     */
    public static String getString(Context context, String key, String defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return preferences.getString(key, defaultValue);
    }
    
    /**
     * 保存整数值
     * @param context 上下文
     * @param key 键
     * @param value 值
     */
    public static void putInt(Context context, String key, int value) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }
    
    /**
     * 获取整数值
     * @param context 上下文
     * @param key 键
     * @param defaultValue 默认值
     * @return 保存的整数值，如果不存在则返回默认值
     */
    public static int getInt(Context context, String key, int defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(key, defaultValue);
    }
    
    /**
     * 保存布尔值
     * @param context 上下文
     * @param key 键
     * @param value 值
     */
    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    
    /**
     * 获取布尔值
     * @param context 上下文
     * @param key 键
     * @param defaultValue 默认值
     * @return 保存的布尔值，如果不存在则返回默认值
     */
    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(key, defaultValue);
    }
    
    /**
     * 保存长整数值
     * @param context 上下文
     * @param key 键
     * @param value 值
     */
    public static void putLong(Context context, String key, long value) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }
    
    /**
     * 获取长整数值
     * @param context 上下文
     * @param key 键
     * @param defaultValue 默认值
     * @return 保存的长整数值，如果不存在则返回默认值
     */
    public static long getLong(Context context, String key, long defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return preferences.getLong(key, defaultValue);
    }
    
    /**
     * 删除指定键的数据
     * @param context 上下文
     * @param key 键
     */
    public static void remove(Context context, String key) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.apply();
    }
    
    /**
     * 清除所有数据
     * @param context 上下文
     */
    public static void clear(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
    
    /**
     * 检查是否包含指定的键
     * @param context 上下文
     * @param key 键
     * @return 是否包含该键
     */
    public static boolean contains(Context context, String key) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return preferences.contains(key);
    }
} 