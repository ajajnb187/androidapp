package com.gxuwz.xinwenapp.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.gxuwz.xinwenapp.model.entity.News;

import java.util.List;

/**
 * 新闻数据访问对象接口
 * 
 * 作用：
 * 1. 定义对News表的所有数据库操作
 * 2. 提供新闻的增删改查、分类查询等功能
 * 3. Room框架会自动实现该接口
 * 
 * 调用者：
 * 1. NewsRepository：新闻数据仓库，协调API和本地缓存
 * 2. NewsService：新闻业务逻辑层，处理新闻相关业务
 */
@Dao
public interface NewsDao {
    /**
     * 批量插入新闻，如果新闻ID已存在则替换
     * @param newsList 要插入的新闻列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<News> newsList);
    
    /**
     * 插入单条新闻，如果新闻ID已存在则替换
     * @param news 要插入的新闻对象
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(News news);
    
    /**
     * 更新新闻信息
     * @param news 要更新的新闻对象
     */
    @Update
    void update(News news);
    
    /**
     * 按分类获取新闻列表，支持分页，按日期降序排序
     * @param category 新闻分类
     * @param limit 每页数量
     * @param offset 起始位置
     * @return 新闻列表
     */
    @Query("SELECT * FROM news WHERE category = :category ORDER BY date DESC, cacheTime DESC LIMIT :limit OFFSET :offset")
    List<News> getNewsByCategory(String category, int limit, int offset);
    
    /**
     * 检查新闻是否已存在
     * @param title 新闻标题
     * @param category 新闻分类
     * @return 存在的新闻数量
     */
    @Query("SELECT COUNT(*) FROM news WHERE title = :title AND category = :category")
    int checkNewsExists(String title, String category);
    
    /**
     * 按分类获取新闻列表，支持分页，去除重复标题
     * @param category 新闻分类
     * @param limit 每页数量
     * @param offset 起始位置
     * @return 新闻列表
     */
    @Query("SELECT * FROM news WHERE category = :category GROUP BY title ORDER BY date DESC, cacheTime DESC LIMIT :limit OFFSET :offset")
    List<News> getNewsByCategoryNoDuplicates(String category, int limit, int offset);
    
    /**
     * 通过新闻ID获取新闻详情
     * @param uniqueKey 新闻ID
     * @return 新闻对象，如果不存在则返回null
     */
    @Query("SELECT * FROM news WHERE uniqueKey = :uniqueKey")
    News getNewsById(String uniqueKey);
    
    /**
     * 按关键字搜索新闻
     * @param keyword 搜索关键词
     * @return 符合条件的新闻列表
     */
    @Query("SELECT * FROM news WHERE title LIKE '%' || :keyword || '%' ORDER BY date DESC")
    List<News> searchNewsByKeyword(String keyword);
    
    /**
     * 更新新闻内容
     * @param uniqueKey 新闻ID
     * @param content 新闻内容
     */
    @Query("UPDATE news SET content = :content WHERE uniqueKey = :uniqueKey")
    void updateContent(String uniqueKey, String content);
    
    /**
     * 删除过期的新闻缓存
     * @param expireTime 过期时间戳
     */
    @Query("DELETE FROM news WHERE cacheTime < :expireTime")
    void deleteExpiredNews(long expireTime);
    
    /**
     * 删除指定分类的所有新闻
     * @param category 新闻分类
     * @return 删除的行数
     */
    @Query("DELETE FROM news WHERE category = :category")
    int deleteNewsByCategory(String category);
} 