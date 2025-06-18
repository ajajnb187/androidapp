package com.gxuwz.xinwenapp.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 新闻收藏实体类
 * 
 * 作用：
 * 1. 存储用户收藏的新闻信息
 * 2. 与数据库表对应
 */
@Entity(
    tableName = "news_favorite", 
    indices = {@Index(value = {"news_id", "phone_number"}, unique = true)}
)
public class NewsFavorite {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "news_id")
    private String newsId;
    
    @ColumnInfo(name = "phone_number")
    private String phoneNumber;
    
    @ColumnInfo(name = "favorite_time")
    private long favoriteTime;
    
    public NewsFavorite() {
        // 默认构造函数
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getNewsId() {
        return newsId;
    }
    
    public void setNewsId(String newsId) {
        this.newsId = newsId;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public long getFavoriteTime() {
        return favoriteTime;
    }
    
    public void setFavoriteTime(long favoriteTime) {
        this.favoriteTime = favoriteTime;
    }
} 