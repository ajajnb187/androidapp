package com.gxuwz.xinwenapp.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.gxuwz.xinwenapp.R;

import java.util.Random;

/**
 * 版本更新页面
 * 
 * 作用：
 * 1. 检查应用是否有新版本
 * 2. 提供下载更新功能
 */
public class UpdateActivity extends AppCompatActivity {
    private static final String CURRENT_VERSION = "1.0.0";
    private static final String LATEST_VERSION = "1.0.0"; // 模拟最新版本，实际应从服务器获取
    
    private TextView tvCurrentVersion;
    private TextView tvLatestVersion;
    private TextView tvUpdateContent;
    private ProgressBar progressBar;
    private TextView tvProgress;
    private Button btnCheckUpdate;
    private Button btnDownload;
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isChecking = false;
    private boolean isDownloading = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        
        initViews();
        setupListeners();
    }
    
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        tvCurrentVersion = findViewById(R.id.tv_current_version);
        tvLatestVersion = findViewById(R.id.tv_latest_version);
        tvUpdateContent = findViewById(R.id.tv_update_content);
        progressBar = findViewById(R.id.progress_bar);
        tvProgress = findViewById(R.id.tv_progress);
        btnCheckUpdate = findViewById(R.id.btn_check_update);
        btnDownload = findViewById(R.id.btn_download);
        
        // 设置当前版本
        tvCurrentVersion.setText("当前版本：v" + CURRENT_VERSION);
        tvLatestVersion.setText("最新版本：检查中...");
    }
    
    private void setupListeners() {
        btnCheckUpdate.setOnClickListener(v -> checkUpdate());
        
        btnDownload.setOnClickListener(v -> {
            if (!isDownloading) {
                startDownload();
            }
        });
    }
    
    private void checkUpdate() {
        if (isChecking) {
            return;
        }
        
        isChecking = true;
        btnCheckUpdate.setEnabled(false);
        btnCheckUpdate.setText("检查中...");
        
        // 模拟网络请求延迟
        handler.postDelayed(() -> {
            isChecking = false;
            btnCheckUpdate.setEnabled(true);
            btnCheckUpdate.setText("检查更新");
            
            // 显示版本信息
            tvLatestVersion.setText("最新版本：v" + LATEST_VERSION);
            
            // 比较版本号
            if (compareVersions(LATEST_VERSION, CURRENT_VERSION) > 0) {
                // 有新版本
                tvUpdateContent.setText("1. 修复已知问题\n2. 优化用户体验\n3. 新增更多功能");
                btnDownload.setVisibility(View.VISIBLE);
                btnDownload.setEnabled(true);
                Toast.makeText(this, "发现新版本", Toast.LENGTH_SHORT).show();
            } else {
                // 已是最新版本
                tvUpdateContent.setText("当前已是最新版本");
                btnDownload.setVisibility(View.GONE);
                Toast.makeText(this, "当前已是最新版本", Toast.LENGTH_SHORT).show();
            }
        }, 2000);
    }
    
    private void startDownload() {
        isDownloading = true;
        btnDownload.setEnabled(false);
        btnDownload.setText("下载中...");
        progressBar.setVisibility(View.VISIBLE);
        tvProgress.setVisibility(View.VISIBLE);
        
        // 模拟下载进度
        new Thread(() -> {
            for (int i = 0; i <= 100; i += 5) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                final int progress = i;
                handler.post(() -> {
                    progressBar.setProgress(progress);
                    tvProgress.setText("下载中：" + progress + "%");
                    
                    if (progress >= 100) {
                        completeDownload();
                    }
                });
            }
        }).start();
    }
    
    private void completeDownload() {
        isDownloading = false;
        btnDownload.setText("安装");
        btnDownload.setEnabled(true);
        
        // 修改按钮点击事件为安装
        btnDownload.setOnClickListener(v -> {
            // 实际应用中，这里应该打开下载的APK文件进行安装
            // 这里模拟打开应用市场
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "未找到应用市场", Toast.LENGTH_SHORT).show();
            }
        });
        
        Toast.makeText(this, "下载完成，请点击安装", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 比较版本号大小
     * @param version1 版本1
     * @param version2 版本2
     * @return 如果version1 > version2返回1，如果version1 < version2返回-1，相等返回0
     */
    private int compareVersions(String version1, String version2) {
        String[] v1 = version1.split("\\.");
        String[] v2 = version2.split("\\.");
        
        int length = Math.max(v1.length, v2.length);
        
        for (int i = 0; i < length; i++) {
            int num1 = i < v1.length ? Integer.parseInt(v1[i]) : 0;
            int num2 = i < v2.length ? Integer.parseInt(v2[i]) : 0;
            
            if (num1 > num2) {
                return 1;
            } else if (num1 < num2) {
                return -1;
            }
        }
        
        return 0;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 