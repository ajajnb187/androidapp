package com.gxuwz.xinwenapp;

import android.app.Application;
import android.content.Context;

import com.gxuwz.xinwenapp.model.AppDatabase;

/**
 * 应用程序全局类
 * 
 * 作用：
 * 1. 应用初始化
 * 2. 提供全局访问点
 * 3. 管理应用生命周期
 */
public class XinwenApplication extends Application {
    private static XinwenApplication instance;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // 预初始化数据库
        initDatabase();
    }
    
    /**
     * 预初始化数据库
     */
    private void initDatabase() {
        new Thread(() -> {
            AppDatabase.getInstance(this);
        }).start();
    }
    
    /**
     * 获取应用实例
     * @return 应用实例
     */
    public static XinwenApplication getInstance() {
        return instance;
    }
    
    /**
     * 获取应用上下文
     * @return 应用上下文
     */
    public static Context getAppContext() {
        return instance.getApplicationContext();
    }
} 