package com.gxuwz.xinwenapp.model.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * 新闻浏览历史实体类
 * 
 * 作用：
 * 1. 定义用户浏览新闻历史的数据结构
 * 2. 映射到数据库中的news_history表
 * 3. 记录用户阅读的新闻、阅读时间等数据
 * 4. 用于个人中心展示浏览历史
 * 
 * 调用者：
 * 1. NewsHistoryDao：数据库操作层，进行CRUD操作
 * 2. NewsRepository：数据仓库层，管理浏览历史
 * 3. NewsService：业务逻辑层，记录和查询浏览历史
 * 4. NewsController：控制器层，处理浏览历史相关请求
 * 5. 个人中心Fragment：展示用户浏览历史
 */
@Entity(tableName = "news_history",
        foreignKeys = {
            @ForeignKey(entity = User.class,
                    parentColumns = "phoneNumber",
                    childColumns = "phoneNumber",
                    onDelete = ForeignKey.CASCADE)
        },
        indices = {
            @Index("phoneNumber"),
            @Index("newsUniqueKey")
        })
public class NewsHistory {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @NonNull
    private String phoneNumber;
    
    @NonNull
    private String newsUniqueKey;
    
    private long readTime;
    private int readDuration;
    
    /**
     * 创建新闻浏览历史记录
     * 
     * @param phoneNumber 用户手机号
     * @param newsUniqueKey 新闻ID
     */
    public NewsHistory(@NonNull String phoneNumber, @NonNull String newsUniqueKey) {
        this.phoneNumber = phoneNumber;
        this.newsUniqueKey = newsUniqueKey;
        this.readTime = System.currentTimeMillis();
        this.readDuration = 0;
    }
    
    /**
     * 获取记录ID
     * @return 记录ID
     */
    public long getId() {
        return id;
    }
    
    /**
     * 设置记录ID
     * @param id 记录ID
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * 获取用户手机号
     * @return 用户手机号
     */
    @NonNull
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * 设置用户手机号
     * @param phoneNumber 用户手机号
     */
    public void setPhoneNumber(@NonNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * 获取新闻ID
     * @return 新闻唯一标识
     */
    @NonNull
    public String getNewsUniqueKey() {
        return newsUniqueKey;
    }

    /**
     * 设置新闻ID
     * @param newsUniqueKey 新闻唯一标识
     */
    public void setNewsUniqueKey(@NonNull String newsUniqueKey) {
        this.newsUniqueKey = newsUniqueKey;
    }

    /**
     * 获取阅读时间
     * @return 阅读时间戳
     */
    public long getReadTime() {
        return readTime;
    }

    /**
     * 设置阅读时间
     * @param readTime 阅读时间戳
     */
    public void setReadTime(long readTime) {
        this.readTime = readTime;
    }

    /**
     * 获取阅读时长（秒）
     * @return 阅读时长
     */
    public int getReadDuration() {
        return readDuration;
    }

    /**
     * 设置阅读时长（秒）
     * @param readDuration 阅读时长
     */
    public void setReadDuration(int readDuration) {
        this.readDuration = readDuration;
    }
} 