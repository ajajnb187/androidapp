package com.gxuwz.xinwenapp.model.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * 新闻实体类
 * 
 * 作用：
 * 1. 表示新闻数据
 * 2. 存储新闻标题、内容、图片、来源等信息
 * 
 * 调用者：
 * 1. NewsAdapter：显示新闻列表项
 * 2. NewsDetailActivity：显示新闻详情
 * 3. NewsController：处理新闻数据
 */
@Entity(tableName = "news")
public class News {
    /**
     * 新闻唯一标识符
     */
    @PrimaryKey
    @NonNull
    private String uniqueKey;
    
    /**
     * 新闻标题
     */
    private String title;
    
    /**
     * 新闻日期
     */
    private String date;
    
    /**
     * 新闻分类
     */
    private String category;
    
    /**
     * 新闻作者/来源
     */
    private String author;
    
    /**
     * 新闻来源网站
     */
    private String source;
    
    /**
     * 新闻网址
     */
    private String url;
    
    /**
     * 新闻图片网址
     */
    private String picUrl;
    
    /**
     * 新闻内容
     */
    private String content;
    
    /**
     * 发布时间
     */
    private String publishTime;
    
    /**
     * 缓存时间，用于判断数据过期
     */
    private long cacheTime;
    
    /**
     * 特殊标签，如"推荐"等
     */
    private String specialTag;
    
    /**
     * 默认构造函数
     */
    public News() {
    }
    
    /**
     * 构造函数
     * @param uniqueKey 新闻唯一标识符
     * @param title 新闻标题
     * @param source 新闻来源
     * @param publishTime 发布时间
     */
    @Ignore
    public News(@NonNull String uniqueKey, String title, String source, String publishTime) {
        this.uniqueKey = uniqueKey;
        this.title = title;
        this.source = source;
        this.publishTime = publishTime;
        this.cacheTime = System.currentTimeMillis();
    }

    /**
     * 获取新闻ID
     * @return 新闻唯一标识
     */
    @NonNull
    public String getUniqueKey() {
        return uniqueKey;
    }

    /**
     * 设置新闻ID
     * @param uniqueKey 新闻唯一标识
     */
    public void setUniqueKey(@NonNull String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    /**
     * 获取新闻标题
     * @return 新闻标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置新闻标题
     * @param title 新闻标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取新闻日期
     * @return 新闻日期
     */
    public String getDate() {
        return date;
    }

    /**
     * 设置新闻日期
     * @param date 新闻日期
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * 获取新闻分类
     * @return 新闻分类
     */
    public String getCategory() {
        return category;
    }

    /**
     * 设置新闻分类
     * @param category 新闻分类
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 获取新闻作者/来源
     * @return 新闻作者/来源
     */
    public String getAuthor() {
        return author;
    }

    /**
     * 设置新闻作者/来源
     * @param author 新闻作者/来源
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * 获取新闻来源网站
     * @return 新闻来源网站
     */
    public String getSource() {
        return source;
    }

    /**
     * 设置新闻来源网站
     * @param source 新闻来源网站
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 获取新闻网址
     * @return 新闻网址
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置新闻网址
     * @param url 新闻网址
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取新闻图片网址
     * @return 新闻图片网址
     */
    public String getPicUrl() {
        return picUrl;
    }

    /**
     * 设置新闻图片网址
     * @param picUrl 新闻图片网址
     */
    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    /**
     * 获取新闻内容
     * @return 新闻内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置新闻内容
     * @param content 新闻内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取发布时间
     * @return 发布时间
     */
    public String getPublishTime() {
        return publishTime;
    }

    /**
     * 设置发布时间
     * @param publishTime 发布时间
     */
    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }
    
    public long getCacheTime() {
        return cacheTime;
    }
    
    public void setCacheTime(long cacheTime) {
        this.cacheTime = cacheTime;
    }

    /**
     * 获取特殊标签
     * @return 特殊标签
     */
    public String getSpecialTag() {
        return specialTag;
    }
    
    /**
     * 设置特殊标签
     * @param specialTag 特殊标签
     */
    public void setSpecialTag(String specialTag) {
        this.specialTag = specialTag;
    }
    
    /**
     * 重写equals方法，用于比较两个新闻是否相同
     * 主要比较标题和分类，用于去重
     * @param o 要比较的对象
     * @return 是否相同
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        News news = (News) o;
        
        // 比较标题和分类
        if (title != null ? !title.equals(news.title) : news.title != null) return false;
        return category != null ? category.equals(news.category) : news.category == null;
    }
    
    /**
     * 重写hashCode方法，与equals方法一致
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (category != null ? category.hashCode() : 0);
        return result;
    }
} 