package com.gxuwz.xinwenapp.model.repository;

import android.content.Context;

import com.gxuwz.xinwenapp.model.AppDatabase;
import com.gxuwz.xinwenapp.model.dao.UserDao;
import com.gxuwz.xinwenapp.model.entity.User;

/**
 * 用户数据仓库类
 * 
 * 作用：
 * 1. 协调UserDao和业务逻辑
 * 2. 处理用户数据的缓存和持久化
 * 3. 提供用户相关的数据操作方法
 * 
 * 调用者：
 * 1. UserService：用户业务逻辑服务类
 * 2. UserController：用户控制器类
 */
public class UserRepository {
    private final UserDao userDao;
    
    /**
     * 构造函数
     * @param context 应用上下文
     */
    public UserRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        userDao = database.userDao();
    }
    
    /**
     * 通过手机号查询用户
     * @param phoneNumber 手机号
     * @return 用户对象，如果不存在则返回null
     */
    public User getUserByPhone(String phoneNumber) {
        return userDao.getUserByPhone(phoneNumber);
    }
    
    /**
     * 用户注册
     * @param phoneNumber 手机号
     * @param password 密码
     * @return 是否注册成功
     */
    public boolean register(String phoneNumber, String password) {
        try {
            User user = new User(phoneNumber, password);
            userDao.insert(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 用户登录
     * @param phoneNumber 手机号
     * @param password 密码
     * @return 用户对象，如果验证失败则返回null
     */
    public User login(String phoneNumber, String password) {
        User user = userDao.login(phoneNumber, password);
        if (user != null) {
            // 更新最后登录时间
            userDao.updateLoginTime(phoneNumber, System.currentTimeMillis());
        }
        return user;
    }
    
    /**
     * 更新用户资料
     * @param user 用户对象
     * @return 是否更新成功
     */
    public boolean updateUserProfile(User user) {
        try {
            userDao.update(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 修改密码
     * @param phoneNumber 手机号
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    public boolean updatePassword(String phoneNumber, String newPassword) {
        try {
            userDao.updatePassword(phoneNumber, newPassword);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 检查手机号是否已注册
     * @param phoneNumber 手机号
     * @return 是否已注册
     */
    public boolean isPhoneRegistered(String phoneNumber) {
        return userDao.getUserByPhone(phoneNumber) != null;
    }
} 