package com.gxuwz.xinwenapp.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.gxuwz.xinwenapp.model.entity.NewsCategory;

import java.util.List;

/**
 * 新闻分类数据访问对象接口
 * 
 * 作用：
 * 1. 定义对NewsCategory表的所有数据库操作
 * 2. 提供新闻分类的增删改查功能
 * 3. Room框架会自动实现该接口
 * 
 * 调用者：
 * 1. NewsRepository：新闻数据仓库，管理新闻分类数据
 * 2. NewsService：新闻业务逻辑层，处理分类相关业务
 * 3. MainActivity：获取新闻分类列表，设置TabLayout
 */
@Dao
public interface NewsCategoryDao {
    /**
     * 插入单个新闻分类，如果分类已存在则替换
     * @param category 要插入的新闻分类
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NewsCategory category);
    
    /**
     * 批量插入新闻分类，如果分类已存在则替换
     * @param categories 要插入的新闻分类列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<NewsCategory> categories);
    
    /**
     * 更新新闻分类信息
     * @param category 要更新的分类对象
     */
    @Update
    void update(NewsCategory category);
    
    /**
     * 获取所有新闻分类，按显示顺序排序
     * @return 新闻分类列表
     */
    @Query("SELECT * FROM news_categories ORDER BY sortOrder")
    List<NewsCategory> getAllCategories();
    
    /**
     * 获取所有启用的新闻分类，按显示顺序排序
     * @return 启用的新闻分类列表
     */
    @Query("SELECT * FROM news_categories WHERE enabled = 1 ORDER BY sortOrder")
    List<NewsCategory> getEnabledCategories();
    
    /**
     * 通过类型获取分类信息
     * @param type 分类类型（例如top、guonei等）
     * @return 分类对象，如果不存在则返回null
     */
    @Query("SELECT * FROM news_categories WHERE type = :type")
    NewsCategory getCategoryByType(String type);
    
    /**
     * 更新分类显示顺序
     * @param type 分类类型
     * @param sortOrder 新的显示顺序
     */
    @Query("UPDATE news_categories SET sortOrder = :sortOrder WHERE type = :type")
    void updateDisplayOrder(String type, int sortOrder);
    
    /**
     * 启用或禁用分类
     * @param type 分类类型
     * @param enabled 是否启用
     */
    @Query("UPDATE news_categories SET enabled = :enabled WHERE type = :type")
    void updateEnabledStatus(String type, boolean enabled);
} 