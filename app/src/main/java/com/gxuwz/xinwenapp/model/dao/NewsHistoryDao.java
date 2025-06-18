package com.gxuwz.xinwenapp.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.gxuwz.xinwenapp.model.entity.NewsHistory;

import java.util.List;

/**
 * 新闻浏览历史数据访问对象接口
 * 
 * 作用：
 * 1. 定义对NewsHistory表的所有数据库操作
 * 2. 提供浏览历史的增删改查功能
 * 3. Room框架会自动实现该接口
 * 
 * 调用者：
 * 1. NewsRepository：数据仓库层，管理浏览历史数据
 * 2. NewsService：业务逻辑层，处理浏览历史相关业务
 * 3. NewsDetailActivity：记录用户的新闻阅读行为
 * 4. ProfileFragment：展示用户的浏览历史
 */
@Dao
public interface NewsHistoryDao {
    /**
     * 插入浏览历史记录
     * @param newsHistory 浏览历史对象
     * @return 插入记录的ID
     */
    @Insert
    long insert(NewsHistory newsHistory);
    
    /**
     * 更新浏览历史记录
     * @param newsHistory 要更新的历史记录
     */
    @Update
    void update(NewsHistory newsHistory);
    
    /**
     * 删除单条浏览历史记录
     * @param newsHistory 要删除的历史记录
     */
    @Delete
    void delete(NewsHistory newsHistory);
    
    /**
     * 获取指定用户的所有浏览历史，按浏览时间倒序排列
     * @param phoneNumber 用户手机号
     * @return 浏览历史列表
     */
    @Query("SELECT * FROM news_history WHERE phoneNumber = :phoneNumber ORDER BY readTime DESC")
    List<NewsHistory> getHistoryByUser(String phoneNumber);
    
    /**
     * 获取指定用户的浏览历史，支持分页
     * @param phoneNumber 用户手机号
     * @param limit 每页数量
     * @param offset 起始位置
     * @return 浏览历史列表
     */
    @Query("SELECT * FROM news_history WHERE phoneNumber = :phoneNumber ORDER BY readTime DESC LIMIT :limit OFFSET :offset")
    List<NewsHistory> getHistoryByUserPaged(String phoneNumber, int limit, int offset);
    
    /**
     * 获取用户对特定新闻的浏览记录
     * @param phoneNumber 用户手机号
     * @param newsUniqueKey 新闻ID
     * @return 浏览历史记录，如果不存在则返回null
     */
    @Query("SELECT * FROM news_history WHERE phoneNumber = :phoneNumber AND newsUniqueKey = :newsUniqueKey LIMIT 1")
    NewsHistory getHistoryByUserAndNews(String phoneNumber, String newsUniqueKey);
    
    /**
     * 更新新闻阅读时长
     * @param id 记录ID
     * @param duration 阅读时长（秒）
     */
    @Query("UPDATE news_history SET readDuration = :duration WHERE id = :id")
    void updateReadDuration(long id, int duration);
    
    /**
     * 清空用户的浏览历史
     * @param phoneNumber 用户手机号
     */
    @Query("DELETE FROM news_history WHERE phoneNumber = :phoneNumber")
    void clearHistoryByUser(String phoneNumber);
    
    /**
     * 删除指定时间之前的历史记录
     * @param phoneNumber 用户手机号
     * @param timestamp 时间戳
     */
    @Query("DELETE FROM news_history WHERE phoneNumber = :phoneNumber AND readTime < :timestamp")
    void deleteHistoryBefore(String phoneNumber, long timestamp);
} 