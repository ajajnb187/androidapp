package com.gxuwz.xinwenapp.model.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * 新闻分类实体类
 * 
 * 作用：
 * 1. 表示新闻分类信息
 * 2. 存储分类类型、名称和排序序号
 * 
 * 调用者：
 * 1. MainActivity：处理分类标签
 * 2. NewsController：获取分类列表
 */
@Entity(tableName = "news_categories")
public class NewsCategory {
    /**
     * 分类类型编码，如"top"代表头条，"guonei"代表国内新闻
     */
    @PrimaryKey
    @NonNull
    private String type;
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 排序序号
     */
    private int sortOrder;
    
    /**
     * 是否启用
     */
    private boolean enabled;
    
    /**
     * 默认构造函数
     */
    public NewsCategory() {
        this.enabled = true;
    }
    
    /**
     * 构造函数
     * @param type 分类类型
     * @param name 分类名称
     * @param sortOrder 排序序号
     */
    @Ignore
    public NewsCategory(@NonNull String type, String name, int sortOrder) {
        this.type = type;
        this.name = name;
        this.sortOrder = sortOrder;
        this.enabled = true;
    }
    
    @NonNull
    public String getType() {
        return type;
    }
    
    public void setType(@NonNull String type) {
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
} 