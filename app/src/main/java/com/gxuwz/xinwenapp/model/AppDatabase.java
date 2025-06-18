package com.gxuwz.xinwenapp.model;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.gxuwz.xinwenapp.model.dao.NewsDao;
import com.gxuwz.xinwenapp.model.dao.UserDao;
import com.gxuwz.xinwenapp.model.entity.News;
import com.gxuwz.xinwenapp.model.entity.User;
import com.gxuwz.xinwenapp.model.entity.NewsCategory;
import com.gxuwz.xinwenapp.model.entity.NewsHistory;
import com.gxuwz.xinwenapp.model.entity.NewsFavorite;
import com.gxuwz.xinwenapp.model.dao.NewsCategoryDao;
import com.gxuwz.xinwenapp.model.dao.NewsHistoryDao;
import com.gxuwz.xinwenapp.model.dao.NewsFavoriteDao;

/**
 * 应用程序数据库类
 * 
 * 作用：
 * 1. 定义整个应用程序的Room数据库
 * 2. 提供单例模式访问数据库的方法
 * 3. 管理所有DAO对象
 * 
 * 调用者：
 * 1. 应用程序Repository层：通过getInstance获取数据库实例并访问各个Dao
 * 2. 应用程序启动类：初始化数据库
 */
@Database(entities = {User.class, News.class, NewsCategory.class, NewsHistory.class, NewsFavorite.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "xinwen_db";
    private static AppDatabase instance;
    
    /**
     * 获取用户数据访问对象
     * @return UserDao实例
     */
    public abstract UserDao userDao();
    
    /**
     * 获取新闻数据访问对象
     * @return NewsDao实例
     */
    public abstract NewsDao newsDao();
    
    /**
     * 获取新闻分类数据访问对象
     * @return NewsCategoryDao实例
     */
    public abstract NewsCategoryDao newsCategoryDao();
    
    /**
     * 获取新闻浏览历史数据访问对象
     * @return NewsHistoryDao实例
     */
    public abstract NewsHistoryDao newsHistoryDao();
    
    /**
     * 获取新闻收藏数据访问对象
     * @return NewsFavoriteDao实例
     */
    public abstract NewsFavoriteDao newsFavoriteDao();
    
    /**
     * 获取数据库单例实例
     * 
     * @param context 应用程序上下文
     * @return 数据库实例
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration() // 如果版本变更，直接删除重建数据库
                    .build();
        }
        return instance;
    }
} 