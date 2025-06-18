package com.gxuwz.xinwenapp.model.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * 用户实体类
 * 
 * 作用：
 * 1. 定义用户信息的数据结构
 * 2. 映射到数据库中的users表
 * 3. 存储用户账号、密码、个人信息等数据
 * 
 * 调用者：
 * 1. UserDao：数据库操作层，进行CRUD操作
 * 2. UserRepository：数据仓库层，协调DAO和业务逻辑
 * 3. UserService：业务逻辑层，处理用户相关业务
 * 4. UserController：控制器层，处理用户请求
 */
@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    private String phoneNumber;
    
    private String password;
    private String nickname;
    private String avatar;
    private String gender;
    private String birthday;
    private String email;
    private long registerTime;
    private long lastLoginTime;
    
    /**
     * 默认构造函数
     */
    public User() {
    }
    
    /**
     * 创建新用户
     * 
     * @param phoneNumber 手机号码（主键）
     * @param password 密码
     */
    @Ignore
    public User(@NonNull String phoneNumber, String password) {
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.registerTime = System.currentTimeMillis();
        this.lastLoginTime = System.currentTimeMillis();
        this.nickname = "用户" + phoneNumber.substring(phoneNumber.length() - 4);
    }

    /**
     * 获取用户手机号
     * @return 手机号
     */
    @NonNull
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * 设置用户手机号
     * @param phoneNumber 手机号
     */
    public void setPhoneNumber(@NonNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * 获取用户密码
     * @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置用户密码
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取用户昵称
     * @return 昵称
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 设置用户昵称
     * @param nickname 昵称
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 获取用户头像链接
     * @return 头像链接
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     * 设置用户头像链接
     * @param avatar 头像链接
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     * 获取用户性别
     * @return 性别
     */
    public String getGender() {
        return gender;
    }

    /**
     * 设置用户性别
     * @param gender 性别
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * 获取用户生日
     * @return 生日
     */
    public String getBirthday() {
        return birthday;
    }

    /**
     * 设置用户生日
     * @param birthday 生日
     */
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
    
    /**
     * 获取用户邮箱
     * @return 邮箱
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * 设置用户邮箱
     * @param email 邮箱
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 获取用户注册时间
     * @return 注册时间戳
     */
    public long getRegisterTime() {
        return registerTime;
    }

    /**
     * 设置用户注册时间
     * @param registerTime 注册时间戳
     */
    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }

    /**
     * 获取用户最后登录时间
     * @return 最后登录时间戳
     */
    public long getLastLoginTime() {
        return lastLoginTime;
    }

    /**
     * 设置用户最后登录时间
     * @param lastLoginTime 最后登录时间戳
     */
    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
    
    /**
     * 获取用户名（兼容方法，返回昵称）
     * @return 用户名
     */
    public String getUsername() {
        return nickname;
    }
    
    /**
     * 获取头像URL（兼容方法，返回头像）
     * @return 头像URL
     */
    public String getAvatarUrl() {
        return avatar;
    }
} 