package com.gxuwz.xinwenapp.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限请求工具类
 * 
 * 作用：
 * 1. 检查权限是否已授权
 * 2. 请求运行时权限
 * 
 * 调用者：
 * 1. 需要请求权限的Activity
 */
public class PermissionUtil {
    
    /**
     * 检查是否已经授予了指定权限
     * @param context 上下文
     * @param permission 权限
     * @return 是否已授权
     */
    public static boolean hasPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
    
    /**
     * 检查是否已经授予了指定权限列表
     * @param context 上下文
     * @param permissions 权限列表
     * @return 是否全部已授权
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * 请求权限
     * @param activity Activity
     * @param permissions 权限列表
     * @param requestCode 请求码
     * @return 是否全部已授权
     */
    public static boolean requestPermissions(Activity activity, int requestCode, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity != null && permissions != null) {
            List<String> permissionsToRequest = new ArrayList<>();
            
            // 检查哪些权限需要请求
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
            
            if (!permissionsToRequest.isEmpty()) {
                // 请求权限
                ActivityCompat.requestPermissions(
                        activity,
                        permissionsToRequest.toArray(new String[0]),
                        requestCode
                );
                return false;
            }
        }
        return true;
    }
    
    /**
     * 处理权限请求结果
     * @param grantResults 授权结果
     * @return 是否全部已授权
     */
    public static boolean areAllPermissionsGranted(int[] grantResults) {
        if (grantResults.length == 0) {
            return false;
        }
        
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
} 