package com.gxuwz.xinwenapp.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.gxuwz.xinwenapp.model.entity.User;

/**
 * 用户数据访问对象接口
 * 
 * 作用：
 * 1. 定义对User表的所有数据库操作
 * 2. 提供用户的增删改查、登录验证等功能
 * 3. Room框架会自动实现该接口
 * 
 * 调用者：
 * 1. UserRepository：用户数据仓库，协调用户数据访问
 * 2. UserService：用户业务逻辑层，处理用户相关业务
 */
@Dao
public interface UserDao {
    /**
     * 插入新用户，如果手机号已存在则中止操作
     * @param user 要插入的用户对象
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(User user);
    
    /**
     * 更新用户信息
     * @param user 要更新的用户对象
     */
    @Update
    void update(User user);
    
    /**
     * 删除用户
     * @param user 要删除的用户对象
     */
    @Delete
    void delete(User user);
    
    /**
     * 通过手机号查询用户
     * @param phoneNumber 手机号
     * @return 用户对象，如果不存在则返回null
     */
    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber")
    User getUserByPhone(String phoneNumber);
    
    /**
     * 用户登录验证
     * @param phoneNumber 手机号
     * @param password 密码
     * @return 用户对象，如果验证失败则返回null
     */
    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber AND password = :password")
    User login(String phoneNumber, String password);
    
    /**
     * 更新用户登录时间
     * @param phoneNumber 手机号
     * @param loginTime 登录时间戳
     */
    @Query("UPDATE users SET lastLoginTime = :loginTime WHERE phoneNumber = :phoneNumber")
    void updateLoginTime(String phoneNumber, long loginTime);
    
    /**
     * 修改用户密码
     * @param phoneNumber 手机号
     * @param newPassword 新密码
     */
    @Query("UPDATE users SET password = :newPassword WHERE phoneNumber = :phoneNumber")
    void updatePassword(String phoneNumber, String newPassword);
} 