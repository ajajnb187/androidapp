package com.gxuwz.xinwenapp.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.gxuwz.xinwenapp.R;
import com.gxuwz.xinwenapp.view.activity.RegisterActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 短信工具类
 * 
 * 作用：
 * 1. 模拟发送验证码短信到模拟器
 * 2. 生成随机验证码
 * 3. 验证验证码是否正确
 * 4. 在通知栏显示短信通知
 * 
 * 调用者：
 * 1. RegisterActivity：注册页面发送验证码
 * 2. ForgotPasswordActivity：找回密码页面发送验证码
 */
public class SmsUtil {
    // 存储手机号和对应的验证码
    private static final Map<String, String> verificationCodes = new HashMap<>();
    
    // 验证码有效期（毫秒）
    private static final long CODE_EXPIRATION = 5 * 60 * 1000; // 5分钟
    
    // 验证码长度
    private static final int CODE_LENGTH = 6;
    
    // 通知渠道ID
    private static final String CHANNEL_ID = "sms_notification_channel";
    
    // 通知ID - 使用随机ID以确保多个通知都能显示
    private static int getNotificationId() {
        return (int) (System.currentTimeMillis() % 10000);
    }
    
    /**
     * 生成随机验证码
     * @return 6位数字验证码
     */
    public static String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    /**
     * 发送验证码到模拟器
     * @param context 上下文
     * @param phoneNumber 手机号
     * @return 生成的验证码
     */
    public static String sendVerificationCode(Context context, String phoneNumber) {
        String code = generateVerificationCode();
        verificationCodes.put(phoneNumber, code);
        
        // 在模拟器中插入短信
        insertSmsToSimulator(context, phoneNumber, "【新闻App】您的验证码是：" + code + "，5分钟内有效，请勿泄露给他人。");
        
        // 显示短信通知
        showSmsNotification(context, code, phoneNumber);
        
        // 设置验证码过期
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (verificationCodes.containsKey(phoneNumber) && verificationCodes.get(phoneNumber).equals(code)) {
                verificationCodes.remove(phoneNumber);
            }
        }, CODE_EXPIRATION);
        
        return code;
    }
    
    /**
     * 验证验证码是否正确
     * @param phoneNumber 手机号
     * @param code 用户输入的验证码
     * @return 是否正确
     */
    public static boolean verifyCode(String phoneNumber, String code) {
        if (verificationCodes.containsKey(phoneNumber)) {
            String storedCode = verificationCodes.get(phoneNumber);
            return storedCode != null && storedCode.equals(code);
        }
        return false;
    }
    
    /**
     * 清除验证码
     * @param phoneNumber 手机号
     */
    public static void clearVerificationCode(String phoneNumber) {
        verificationCodes.remove(phoneNumber);
    }
    
    /**
     * 将短信插入到模拟器
     * @param context 上下文
     * @param phoneNumber 手机号
     * @param message 短信内容
     */
    private static void insertSmsToSimulator(Context context, String phoneNumber, String message) {
        try {
            // 短信收件箱的Uri
            Uri uri = Uri.parse("content://sms/inbox");
            
            // 准备短信数据
            ContentValues values = new ContentValues();
            values.put("address", "10086"); // 发送方号码，这里用10086模拟
            values.put("body", message); // 短信内容
            values.put("read", 0); // 0表示未读
            values.put("date", System.currentTimeMillis()); // 当前时间
            
            // 插入短信
            context.getContentResolver().insert(uri, values);
            
            // 显示提示
            Toast.makeText(context, "验证码已发送，请查看通知栏", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "发送验证码失败，请检查权限", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 显示短信通知
     * @param context 上下文
     * @param code 验证码
     * @param phoneNumber 手机号
     */
    private static void showSmsNotification(Context context, String code, String phoneNumber) {
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        // 创建通知渠道（Android 8.0及以上需要）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "短信通知",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("用于显示短信验证码的通知渠道");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            channel.setShowBadge(true);
            channel.setBypassDnd(true); // 绕过勿扰模式
            notificationManager.createNotificationChannel(channel);
        }
        
        // 创建返回到当前Activity的意图
        Intent intent = new Intent(context, context.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                intent, 
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        
        // 获取系统默认通知声音
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        
        // 创建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("10086 - 验证码短信")
                .setContentText("【新闻App】您的验证码是：" + code + "，5分钟内有效")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("【新闻App】您的验证码是：" + code + "，5分钟内有效，请勿泄露给他人。"))
                .setPriority(NotificationCompat.PRIORITY_MAX) // 最高优先级
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 200, 500, 200, 500})
                .setLights(Color.RED, 1000, 300)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 在锁屏上也显示完整通知
                .setFullScreenIntent(pendingIntent, true); // 使用全屏意图，在顶部弹出通知
        
        // 发送通知
        notificationManager.notify(getNotificationId(), builder.build());
        
        // 为了确保通知被看到，我们再次发送一个Toast提示
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Toast.makeText(context, "验证码：" + code, Toast.LENGTH_LONG).show();
        }, 1000);
    }
} 