package com.gxuwz.xinwenapp.model.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.gxuwz.xinwenapp.model.entity.User;
import com.gxuwz.xinwenapp.model.repository.UserRepository;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 用户业务逻辑服务类
 * 
 * 作用：
 * 1. 实现用户相关的业务逻辑
 * 2. 处理用户登录、注册、验证等操作
 * 3. 调用UserRepository进行数据操作
 * 
 * 调用者：
 * 1. UserController：用户控制器，处理用户请求
 * 2. 各个Activity：直接使用服务层进行业务操作
 */
public class UserService {
    private static final String TAG = "UserService";
    private final UserRepository userRepository;
    private User currentUser; // 当前登录用户
    private final Executor executor; // 线程池执行器
    private final Handler mainHandler; // 主线程Handler
    
    /**
     * API回调接口，用于异步操作的结果返回
     * @param <T> 返回数据类型
     */
    public interface ApiCallback<T> {
        /**
         * 成功回调
         * @param result 返回结果
         */
        void onSuccess(T result);
        
        /**
         * 失败回调
         * @param errorMsg 错误信息
         */
        void onError(String errorMsg);
    }
    
    /**
     * 构造函数
     * @param context 应用上下文
     */
    public UserService(Context context) {
        userRepository = new UserRepository(context);
        executor = Executors.newSingleThreadExecutor(); // 创建单线程执行器
        mainHandler = new Handler(Looper.getMainLooper()); // 创建主线程Handler
    }
    
    /**
     * 用户登录（异步）
     * @param phoneNumber 手机号
     * @param password 密码
     * @param callback 回调接口
     */
    public void login(String phoneNumber, String password, ApiCallback<User> callback) {
        Log.d(TAG, "开始登录: " + phoneNumber);
        executor.execute(() -> {
            try {
                User user = userRepository.login(phoneNumber, password);
                
                mainHandler.post(() -> {
                    if (user != null) {
                        Log.d(TAG, "登录成功: " + phoneNumber);
                        currentUser = user;
                        callback.onSuccess(user);
                    } else {
                        Log.d(TAG, "登录失败: 手机号或密码错误");
                        callback.onError("手机号或密码错误");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "登录异常: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError("登录失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 手动设置当前用户
     * @param user 用户对象
     */
    public void setCurrentUser(User user) {
        Log.d(TAG, "手动设置当前用户: " + (user != null ? user.getPhoneNumber() : "null"));
        this.currentUser = user;
    }
    
    /**
     * 用户注册（异步）
     * @param phoneNumber 手机号
     * @param password 密码
     * @param confirmPassword 确认密码
     * @param callback 回调接口
     */
    public void register(String phoneNumber, String password, String confirmPassword, ApiCallback<Integer> callback) {
        Log.d(TAG, "开始注册: " + phoneNumber);
        executor.execute(() -> {
            try {
                int result;
                
                // 验证手机号是否已注册
                if (userRepository.isPhoneRegistered(phoneNumber)) {
                    result = 1;
                    Log.d(TAG, "注册失败: 手机号已注册");
                }
                // 验证两次密码是否一致
                else if (!password.equals(confirmPassword)) {
                    result = 2;
                    Log.d(TAG, "注册失败: 两次密码不一致");
                }
                else {
                    // 注册用户
                    boolean success = userRepository.register(phoneNumber, password);
                    result = success ? 0 : 3;
                    Log.d(TAG, "注册结果: " + (success ? "成功" : "失败"));
                }
                
                final int finalResult = result;
                mainHandler.post(() -> callback.onSuccess(finalResult));
            } catch (Exception e) {
                Log.e(TAG, "注册异常: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError("注册失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 用户退出登录
     */
    public void logout() {
        Log.d(TAG, "用户退出登录");
        currentUser = null;
    }
    
    /**
     * 修改密码（异步）
     * @param phoneNumber 手机号
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param callback 回调接口
     */
    public void changePassword(String phoneNumber, String oldPassword, String newPassword, ApiCallback<Integer> callback) {
        Log.d(TAG, "开始修改密码: " + phoneNumber);
        executor.execute(() -> {
            try {
                User user = userRepository.login(phoneNumber, oldPassword);
                int result;
                
                if (user == null) {
                    result = 1; // 用户不存在
                    Log.d(TAG, "修改密码失败: 用户不存在");
                } else if (!user.getPassword().equals(oldPassword)) {
                    result = 2; // 旧密码错误
                    Log.d(TAG, "修改密码失败: 旧密码错误");
                } else {
                    boolean success = userRepository.updatePassword(phoneNumber, newPassword);
                    result = success ? 0 : 3;
                    Log.d(TAG, "修改密码结果: " + (success ? "成功" : "失败"));
                }
                
                final int finalResult = result;
                mainHandler.post(() -> callback.onSuccess(finalResult));
            } catch (Exception e) {
                Log.e(TAG, "修改密码异常: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError("修改密码失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 找回密码（重置密码）（异步）
     * @param phoneNumber 手机号
     * @param newPassword 新密码
     * @param callback 回调接口
     */
    public void resetPassword(String phoneNumber, String newPassword, ApiCallback<Boolean> callback) {
        Log.d(TAG, "开始重置密码: " + phoneNumber);
        executor.execute(() -> {
            try {
                User user = userRepository.getUserByPhone(phoneNumber);
                boolean success = false;
                
                if (user != null) {
                    success = userRepository.updatePassword(phoneNumber, newPassword);
                    Log.d(TAG, "重置密码结果: " + (success ? "成功" : "失败"));
                } else {
                    Log.d(TAG, "重置密码失败: 用户不存在");
                }
                
                final boolean finalSuccess = success;
                mainHandler.post(() -> callback.onSuccess(finalSuccess));
            } catch (Exception e) {
                Log.e(TAG, "重置密码异常: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError("重置密码失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 更新用户个人资料（异步）
     * @param user 用户对象
     * @param callback 回调接口
     */
    public void updateUserProfile(User user, ApiCallback<Boolean> callback) {
        Log.d(TAG, "开始更新用户资料: " + user.getPhoneNumber());
        executor.execute(() -> {
            try {
                boolean success = userRepository.updateUserProfile(user);
                
                if (success && currentUser != null && user.getPhoneNumber().equals(currentUser.getPhoneNumber())) {
                    currentUser = user; // 更新当前用户
                    Log.d(TAG, "更新当前用户资料成功");
                }
                
                Log.d(TAG, "更新用户资料结果: " + (success ? "成功" : "失败"));
                mainHandler.post(() -> callback.onSuccess(success));
            } catch (Exception e) {
                Log.e(TAG, "更新用户资料异常: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError("更新用户资料失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 获取当前登录用户
     * @return 当前用户，如果未登录则返回null
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * 检查用户是否已登录
     * @return 是否已登录
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * 从数据库刷新当前用户信息（异步）
     * @param callback 回调接口
     */
    public void refreshCurrentUser(ApiCallback<User> callback) {
        if (currentUser == null) {
            Log.d(TAG, "刷新用户信息失败: 用户未登录");
            callback.onError("用户未登录");
            return;
        }
        
        Log.d(TAG, "开始刷新用户信息: " + currentUser.getPhoneNumber());
        executor.execute(() -> {
            try {
                User refreshedUser = userRepository.getUserByPhone(currentUser.getPhoneNumber());
                
                mainHandler.post(() -> {
                    if (refreshedUser != null) {
                        currentUser = refreshedUser;
                        Log.d(TAG, "刷新用户信息成功");
                        callback.onSuccess(currentUser);
                    } else {
                        Log.d(TAG, "刷新用户信息失败: 用户不存在");
                        callback.onError("用户不存在");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "刷新用户信息异常: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError("刷新用户信息失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 获取用户信息（异步）
     * @param phoneNumber 手机号
     * @param callback 回调接口
     */
    public void getUserByPhone(String phoneNumber, ApiCallback<User> callback) {
        Log.d(TAG, "开始获取用户信息: " + phoneNumber);
        executor.execute(() -> {
            try {
                User user = userRepository.getUserByPhone(phoneNumber);
                
                mainHandler.post(() -> {
                    if (user != null) {
                        Log.d(TAG, "获取用户信息成功");
                        callback.onSuccess(user);
                    } else {
                        Log.d(TAG, "获取用户信息失败: 用户不存在");
                        callback.onError("用户不存在");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "获取用户信息异常: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError("获取用户信息失败: " + e.getMessage()));
            }
        });
    }
} 