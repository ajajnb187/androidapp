package com.gxuwz.xinwenapp.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gxuwz.xinwenapp.model.entity.User;
import com.gxuwz.xinwenapp.model.service.UserService;
import com.gxuwz.xinwenapp.model.service.UserService.ApiCallback;
import com.gxuwz.xinwenapp.util.SharedPreferencesUtil;

/**
 * 用户控制器类
 * 
 * 作用：
 * 1. 处理用户相关的请求
 * 2. 调用UserService处理业务逻辑
 * 3. 管理用户登录状态和会话
 * 
 * 调用者：
 * 1. Activity/Fragment：用户界面调用控制器处理用户操作
 */
public class UserController {
    private static final String TAG = "UserController";
    private final UserService userService;
    private final Context context;
    
    /**
     * 构造函数
     * @param context 应用上下文
     */
    public UserController(Context context) {
        this.context = context;
        userService = new UserService(context);
        
        // 恢复登录状态
        restoreLoginState();
    }
    
    /**
     * 恢复登录状态
     */
    private void restoreLoginState() {
        String phoneNumber = SharedPreferencesUtil.getString(context, "phone_number", null);
        Log.d(TAG, "尝试恢复登录状态，手机号：" + (phoneNumber != null ? phoneNumber : "null"));
        
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            userService.getUserByPhone(phoneNumber, new ApiCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    Log.d(TAG, "恢复登录状态成功：" + result.getPhoneNumber());
                    // 手动设置当前用户，确保UserService知道当前登录用户
                    userService.setCurrentUser(result);
                }
                
                @Override
                public void onError(String errorMsg) {
                    Log.e(TAG, "恢复登录状态失败：" + errorMsg);
                    // 如果数据库中用户被删除，清除本地登录状态
                    clearLoginState();
                }
            });
        }
    }
    
    /**
     * 保存登录状态
     * @param user 用户对象
     */
    private void saveLoginState(User user) {
        if (user != null) {
            Log.d(TAG, "保存登录状态：" + user.getPhoneNumber());
            SharedPreferencesUtil.putString(context, "phone_number", user.getPhoneNumber());
            SharedPreferencesUtil.putString(context, "username", user.getUsername());
        }
    }
    
    /**
     * 清除登录状态
     */
    private void clearLoginState() {
        Log.d(TAG, "清除登录状态");
        SharedPreferencesUtil.remove(context, "phone_number");
        SharedPreferencesUtil.remove(context, "username");
    }
    
    /**
     * 用户登录
     * @param phoneNumber 手机号
     * @param password 密码
     * @param callback 登录结果回调
     */
    public void login(String phoneNumber, String password, ApiCallback<User> callback) {
        Log.d(TAG, "开始登录：" + phoneNumber);
        userService.login(phoneNumber, password, new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    Log.d(TAG, "登录成功：" + user.getPhoneNumber());
                    saveLoginState(user);
                }
                callback.onSuccess(user);
            }
            
            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "登录失败：" + errorMsg);
                callback.onError(errorMsg);
            }
        });
    }
    
    /**
     * 用户注册
     * @param phoneNumber 手机号
     * @param password 密码
     * @param confirmPassword 确认密码
     * @param callback 注册结果回调
     */
    public void register(String phoneNumber, String password, String confirmPassword, ApiCallback<Integer> callback) {
        Log.d(TAG, "开始注册：" + phoneNumber);
        userService.register(phoneNumber, password, confirmPassword, callback);
    }
    
    /**
     * 用户退出登录
     */
    public void logout() {
        Log.d(TAG, "用户退出登录");
        userService.logout();
        clearLoginState();
    }
    
    /**
     * 获取用户信息
     * @param userId 用户ID（手机号）
     * @param callback 回调接口
     */
    public void getUserInfo(String userId, ApiCallback<User> callback) {
        Log.d(TAG, "获取用户信息：" + userId);
        // 如果是当前登录用户，直接返回
        User currentUser = userService.getCurrentUser();
        if (currentUser != null && currentUser.getPhoneNumber().equals(userId)) {
            Log.d(TAG, "直接使用当前登录用户信息");
            callback.onSuccess(currentUser);
            return;
        }
        
        // 否则从数据库查询
        Log.d(TAG, "从数据库查询用户信息");
        userService.getUserByPhone(userId, callback);
    }
    
    /**
     * 修改密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param callback 修改结果回调
     */
    public void changePassword(String oldPassword, String newPassword, ApiCallback<Integer> callback) {
        User user = userService.getCurrentUser();
        if (user == null) {
            callback.onError("用户未登录");
            return;
        }
        userService.changePassword(user.getPhoneNumber(), oldPassword, newPassword, callback);
    }
    
    /**
     * 找回密码（重置密码）
     * @param phoneNumber 手机号
     * @param newPassword 新密码
     * @param callback 重置结果回调
     */
    public void resetPassword(String phoneNumber, String newPassword, ApiCallback<Boolean> callback) {
        userService.resetPassword(phoneNumber, newPassword, callback);
    }
    
    /**
     * 更新用户个人资料
     * @param user 用户对象
     * @param callback 更新结果回调
     */
    public void updateUserProfile(User user, ApiCallback<Boolean> callback) {
        userService.updateUserProfile(user, new ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result && userService.isLoggedIn()) {
                    // 更新成功且已登录，更新保存的登录状态
                    saveLoginState(user);
                }
                callback.onSuccess(result);
            }
            
            @Override
            public void onError(String errorMsg) {
                callback.onError(errorMsg);
            }
        });
    }
    
    /**
     * 获取当前登录用户
     * @return 当前用户，如果未登录则返回null
     */
    public User getCurrentUser() {
        return userService.getCurrentUser();
    }
    
    /**
     * 检查用户是否已登录
     * @return 是否已登录
     */
    public boolean isLoggedIn() {
        return userService.isLoggedIn();
    }
    
    /**
     * 从数据库刷新当前用户信息
     * @param callback 刷新结果回调
     */
    public void refreshCurrentUser(ApiCallback<User> callback) {
        userService.refreshCurrentUser(new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                // 更新保存的登录状态
                saveLoginState(user);
                callback.onSuccess(user);
            }
            
            @Override
            public void onError(String errorMsg) {
                callback.onError(errorMsg);
            }
        });
    }
} 