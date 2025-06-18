package com.gxuwz.xinwenapp.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.gxuwz.xinwenapp.model.entity.NewsFavorite;

import java.util.List;

/**
 * 新闻收藏DAO
 * 
 * 作用：
 * 1. 提供新闻收藏相关的数据库操作
 * 2. 包括添加收藏、取消收藏、查询收藏等功能
 */
@Dao
public interface NewsFavoriteDao {
    /**
     * 插入新闻收藏记录
     * @param favorite 收藏记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NewsFavorite favorite);
    
    /**
     * 删除新闻收藏记录
     * @param favorite 收藏记录
     */
    @Delete
    void delete(NewsFavorite favorite);
    
    /**
     * 删除用户对指定新闻的收藏
     * @param newsId 新闻ID
     * @param phoneNumber 用户手机号
     * @return 影响的行数
     */
    @Query("DELETE FROM news_favorite WHERE news_id = :newsId AND phone_number = :phoneNumber")
    int deleteNewsFavorite(String newsId, String phoneNumber);
    
    /**
     * 获取用户收藏的所有新闻
     * @param phoneNumber 用户手机号
     * @return 收藏列表
     */
    @Query("SELECT * FROM news_favorite WHERE phone_number = :phoneNumber ORDER BY favorite_time DESC")
    List<NewsFavorite> getUserFavoriteAll(String phoneNumber);
    
    /**
     * 分页获取用户收藏的新闻
     * @param phoneNumber 用户手机号
     * @param limit 数量限制
     * @param offset 起始位置
     * @return 收藏列表
     */
    @Query("SELECT * FROM news_favorite WHERE phone_number = :phoneNumber ORDER BY favorite_time DESC LIMIT :limit OFFSET :offset")
    List<NewsFavorite> getUserFavorites(String phoneNumber, int limit, int offset);
    
    /**
     * 获取指定用户对指定新闻的收藏记录
     * @param newsId 新闻ID
     * @param phoneNumber 用户手机号
     * @return 收藏记录，如果不存在则返回null
     */
    @Query("SELECT * FROM news_favorite WHERE news_id = :newsId AND phone_number = :phoneNumber LIMIT 1")
    NewsFavorite getNewsFavorite(String newsId, String phoneNumber);
    
    /**
     * 获取新闻的收藏数量
     * @param newsId 新闻ID
     * @return 收藏数量
     */
    @Query("SELECT COUNT(*) FROM news_favorite WHERE news_id = :newsId")
    int getFavoriteCount(String newsId);
} 