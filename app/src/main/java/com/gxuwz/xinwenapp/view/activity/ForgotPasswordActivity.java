package com.gxuwz.xinwenapp.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import com.gxuwz.xinwenapp.R;
import com.gxuwz.xinwenapp.controller.UserController;
import com.gxuwz.xinwenapp.model.service.UserService.ApiCallback;
import com.gxuwz.xinwenapp.util.PermissionUtil;
import com.gxuwz.xinwenapp.util.SmsUtil;

/**
 * 找回密码Activity
 * 
 * 作用：
 * 1. 通过手机验证码重置密码
 * 2. 验证手机号
 * 
 * 调用者：
 * LoginActivity：忘记密码入口
 */
public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView ivBack;
    private EditText etPhone;
    private EditText etVerificationCode;
    private Button btnSendCode;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnResetPassword;
    
    private UserController userController;
    private CountDownTimer countDownTimer;
    
    // 验证码倒计时总时长（毫秒）
    private static final long CODE_COUNTDOWN_TOTAL = 60000;
    // 验证码倒计时间隔（毫秒）
    private static final long CODE_COUNTDOWN_INTERVAL = 1000;
    // 权限请求码
    private static final int SMS_PERMISSION_REQUEST_CODE = 100;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 102;
    
    // 需要的短信权限
    private static final String[] SMS_PERMISSIONS = {
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
    };
    
    // 需要的通知权限
    private static final String[] NOTIFICATION_PERMISSIONS = {
            Manifest.permission.POST_NOTIFICATIONS
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        
        userController = new UserController(this);
        
        initViews();
        
        // 检查并请求通知权限
        checkNotificationPermission();
    }
    
    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etPhone = findViewById(R.id.etPhone);
        etVerificationCode = findViewById(R.id.etVerificationCode);
        btnSendCode = findViewById(R.id.btnSendCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        
        ivBack.setOnClickListener(this);
        btnSendCode.setOnClickListener(this);
        btnResetPassword.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.ivBack) {
            finish();
        } else if (id == R.id.btnSendCode) {
            if (checkAndRequestSmsPermissions() && checkNotificationPermission()) {
                sendVerificationCode();
            }
        } else if (id == R.id.btnResetPassword) {
            resetPassword();
        }
    }
    
    /**
     * 检查并请求短信权限
     * @return 是否已有权限
     */
    private boolean checkAndRequestSmsPermissions() {
        if (!PermissionUtil.hasPermissions(this, SMS_PERMISSIONS)) {
            PermissionUtil.requestPermissions(this, SMS_PERMISSION_REQUEST_CODE, SMS_PERMISSIONS);
            return false;
        }
        return true;
    }
    
    /**
     * 检查并请求通知权限
     * @return 是否已有权限
     */
    private boolean checkNotificationPermission() {
        // 检查通知是否已启用
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            // 显示对话框解释为什么需要通知权限
            new AlertDialog.Builder(this)
                    .setTitle("需要通知权限")
                    .setMessage("为了显示验证码通知，我们需要通知权限。请在接下来的对话框中授予权限。")
                    .setPositiveButton("确定", (dialog, which) -> {
                        // Android 13及以上需要请求POST_NOTIFICATIONS权限
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ActivityCompat.requestPermissions(this, 
                                    NOTIFICATION_PERMISSIONS, 
                                    NOTIFICATION_PERMISSION_REQUEST_CODE);
                        } else {
                            // 对于较旧的Android版本，打开通知设置
                            openNotificationSettings();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return false;
        }
        
        // 检查悬浮窗权限（用于顶部弹出通知）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("需要悬浮窗权限")
                    .setMessage("为了在顶部显示验证码通知，我们需要悬浮窗权限。请在接下来的设置中授予权限。")
                    .setPositiveButton("确定", (dialog, which) -> {
                        // 请求悬浮窗权限
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return false;
        }
        
        return true;
    }
    
    /**
     * 打开通知设置
     */
    private void openNotificationSettings() {
        Intent intent = new Intent();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        } else {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", getPackageName());
            intent.putExtra("app_uid", getApplicationInfo().uid);
        }
        
        startActivity(intent);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (PermissionUtil.areAllPermissionsGranted(grantResults)) {
                // 权限已授予，检查通知权限
                if (checkNotificationPermission()) {
                    sendVerificationCode();
                }
            } else {
                // 权限被拒绝
                Toast.makeText(this, "需要短信权限才能发送验证码", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (PermissionUtil.areAllPermissionsGranted(grantResults)) {
                // 通知权限已授予，检查短信权限
                if (checkAndRequestSmsPermissions()) {
                    sendVerificationCode();
                }
            } else {
                // 权限被拒绝
                Toast.makeText(this, "需要通知权限才能显示验证码通知", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                // 悬浮窗权限已授予，检查其他权限
                if (checkAndRequestSmsPermissions() && NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                    sendVerificationCode();
                }
            } else {
                Toast.makeText(this, "需要悬浮窗权限才能显示顶部通知", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void sendVerificationCode() {
        String phone = etPhone.getText().toString().trim();
        
        // 输入验证
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 手机号格式验证
        if (!isValidPhoneNumber(phone)) {
            Toast.makeText(this, "请输入有效的手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 启动倒计时
        startCountdown();
        
        // 使用SmsUtil发送验证码到模拟器
        SmsUtil.sendVerificationCode(this, phone);
    }
    
    private void resetPassword() {
        String phone = etPhone.getText().toString().trim();
        String code = etVerificationCode.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        // 输入验证
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "请输入新密码", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "请确认密码", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 手机号格式验证
        if (!isValidPhoneNumber(phone)) {
            Toast.makeText(this, "请输入有效的手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 密码长度验证
        if (newPassword.length() < 6) {
            Toast.makeText(this, "密码长度不能少于6位", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 两次密码一致性验证
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 验证码验证
        if (!SmsUtil.verifyCode(phone, code)) {
            Toast.makeText(this, "验证码错误或已过期", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 禁用重置密码按钮，避免重复点击
        btnResetPassword.setEnabled(false);
        
        // 调用控制器重置密码（异步）
        userController.resetPassword(phone, newPassword, new ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                // 恢复重置密码按钮
                btnResetPassword.setEnabled(true);
                
                if (success) {
                    // 重置成功，清除验证码
                    SmsUtil.clearVerificationCode(phone);
                    
                    Toast.makeText(ForgotPasswordActivity.this, "密码重置成功，请登录", Toast.LENGTH_SHORT).show();
                    
                    // 返回登录页
                    finish();
                } else {
                    // 重置失败
                    Toast.makeText(ForgotPasswordActivity.this, "密码重置失败，该手机号未注册", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onError(String errorMsg) {
                // 恢复重置密码按钮
                btnResetPassword.setEnabled(true);
                
                // 显示错误信息
                Toast.makeText(ForgotPasswordActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void startCountdown() {
        btnSendCode.setEnabled(false);
        
        // 取消旧的倒计时
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        countDownTimer = new CountDownTimer(CODE_COUNTDOWN_TOTAL, CODE_COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                btnSendCode.setText(secondsRemaining + "秒后重发");
            }
            
            @Override
            public void onFinish() {
                btnSendCode.setEnabled(true);
                btnSendCode.setText("获取验证码");
            }
        }.start();
    }
    
    /**
     * 验证手机号格式
     * @param phone 手机号
     * @return 是否有效
     */
    private boolean isValidPhoneNumber(String phone) {
        // 简单的手机号验证：11位数字，以1开头
        return phone.length() == 11 && phone.startsWith("1");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 取消倒计时
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
} 