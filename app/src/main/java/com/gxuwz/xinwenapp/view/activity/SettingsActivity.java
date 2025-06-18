package com.gxuwz.xinwenapp.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.gxuwz.xinwenapp.R;
import com.gxuwz.xinwenapp.controller.UserController;
import com.gxuwz.xinwenapp.util.SharedPreferencesUtil;

import java.io.File;
import java.text.DecimalFormat;

/**
 * 设置页面
 * 
 * 作用：
 * 1. 提供应用程序各种设置选项
 * 2. 包括个人资料设置、应用设置、关于信息等
 */
public class SettingsActivity extends AppCompatActivity {
    private static final String APP_VERSION = "1.0.0";
    private UserController userController;
    
    private LinearLayout layoutEditProfile;
    private LinearLayout layoutChangePassword;
    private LinearLayout layoutNotification;
    private LinearLayout layoutClearCache;
    private LinearLayout layoutCheckUpdate;
    private LinearLayout layoutAboutUs;
    private LinearLayout layoutFeedback;
    
    private SwitchCompat switchNotification;
    private TextView tvCacheSize;
    private TextView tvVersion;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        userController = new UserController(this);
        
        initViews();
        setupListeners();
        loadSettings();
    }
    
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        layoutEditProfile = findViewById(R.id.layout_edit_profile);
        layoutChangePassword = findViewById(R.id.layout_change_password);
        layoutNotification = findViewById(R.id.layout_notification);
        layoutClearCache = findViewById(R.id.layout_clear_cache);
        layoutCheckUpdate = findViewById(R.id.layout_check_update);
        layoutAboutUs = findViewById(R.id.layout_about_us);
        layoutFeedback = findViewById(R.id.layout_feedback);
        
        switchNotification = findViewById(R.id.switch_notification);
        tvCacheSize = findViewById(R.id.tv_cache_size);
        tvVersion = findViewById(R.id.tv_version);
        
        // 设置版本号
        tvVersion.setText("v" + APP_VERSION);
    }
    
    private void setupListeners() {
        layoutEditProfile.setOnClickListener(v -> {
            if (userController.isLoggedIn()) {
                Intent intent = new Intent(this, EditProfileActivity.class);
                startActivityForResult(intent, 100);
            } else {
                Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }
        });
        
        layoutChangePassword.setOnClickListener(v -> {
            if (userController.isLoggedIn()) {
                Intent intent = new Intent(this, ChangePasswordActivity.class);
                startActivityForResult(intent, 101);
            } else {
                Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }
        });
        
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferencesUtil.putBoolean(this, "notification_enabled", isChecked);
            Toast.makeText(this, isChecked ? "已开启推送通知" : "已关闭推送通知", Toast.LENGTH_SHORT).show();
        });
        
        layoutClearCache.setOnClickListener(v -> {
            showClearCacheDialog();
        });
        
        layoutCheckUpdate.setOnClickListener(v -> {
            startActivity(new Intent(this, UpdateActivity.class));
        });
        
        layoutAboutUs.setOnClickListener(v -> {
            Toast.makeText(this, "关于我们功能即将上线", Toast.LENGTH_SHORT).show();
        });
        
        layoutFeedback.setOnClickListener(v -> {
            Toast.makeText(this, "意见反馈功能即将上线", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void loadSettings() {
        // 加载通知设置
        boolean notificationEnabled = SharedPreferencesUtil.getBoolean(this, "notification_enabled", true);
        switchNotification.setChecked(notificationEnabled);
        
        // 计算缓存大小
        calculateCacheSize();
    }
    
    private void calculateCacheSize() {
        long size = 0;
        try {
            size = getFolderSize(getCacheDir());
            size += getFolderSize(getExternalCacheDir());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String cacheSize = formatFileSize(size);
        tvCacheSize.setText(cacheSize);
    }
    
    private long getFolderSize(File file) {
        long size = 0;
        try {
            if (file != null && file.exists()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isDirectory()) {
                            size += getFolderSize(f);
                        } else {
                            size += f.length();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }
    
    private String formatFileSize(long size) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString;
        if (size < 1024) {
            fileSizeString = df.format((double) size) + "B";
        } else if (size < 1048576) {
            fileSizeString = df.format((double) size / 1024) + "KB";
        } else if (size < 1073741824) {
            fileSizeString = df.format((double) size / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) size / 1073741824) + "GB";
        }
        return fileSizeString;
    }
    
    private void clearCache() {
        try {
            deleteDir(getCacheDir());
            if (getExternalCacheDir() != null) {
                deleteDir(getExternalCacheDir());
            }
            calculateCacheSize();
            Toast.makeText(this, "缓存已清除", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "清除缓存失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir == null || dir.delete();
    }
    
    private void showClearCacheDialog() {
        new AlertDialog.Builder(this)
                .setTitle("清除缓存")
                .setMessage("确定要清除所有缓存吗？")
                .setPositiveButton("确定", (dialog, which) -> clearCache())
                .setNegativeButton("取消", null)
                .show();
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                // 编辑个人资料成功
                Toast.makeText(this, "个人资料已更新", Toast.LENGTH_SHORT).show();
            } else if (requestCode == 101) {
                // 修改密码成功，需要重新登录
                Toast.makeText(this, "密码已修改，请重新登录", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
} 