package com.gxuwz.xinwenapp.view.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.gxuwz.xinwenapp.R;
import com.gxuwz.xinwenapp.controller.UserController;
import com.gxuwz.xinwenapp.view.adapter.SplashAdAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 启动页Activity
 * 
 * 作用：
 * 1. 应用启动的首页
 * 2. 展示启动页广告
 * 3. 根据用户登录状态跳转到对应页面
 * 
 * 调用者：
 * 由系统启动
 */
public class SplashActivity extends AppCompatActivity implements SplashAdAdapter.OnAdClickListener {
    private ViewPager2 viewPager;
    private TextView tvSkipCounter;
    private Button btnSkip;
    private UserController userController;
    private Handler handler;
    
    // 广告展示总时长（毫秒）
    private static final long AD_DURATION = 5000;
    // 倒计时更新间隔（毫秒）
    private static final long COUNTER_INTERVAL = 1000;
    // 广告自动滚动间隔（毫秒）
    private static final long AD_SCROLL_INTERVAL = 2000;
    
    // App包名定义
    private static final String TAOBAO_PACKAGE = "com.taobao.taobao";
    private static final String DOUYIN_PACKAGE = "com.ss.android.ugc.aweme";
    private static final String MEITUAN_PACKAGE = "com.sankuai.meituan";
    
    private int remainingSeconds = 5;
    private int currentAdPosition = 0;
    private List<Integer> adResources; // 改为资源ID列表
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        userController = new UserController(this);
        handler = new Handler(Looper.getMainLooper());
        
        initViews();
        setupAds();
        startCountdown();
        startAutoScroll();
    }
    
    private void initViews() {
        viewPager = findViewById(R.id.viewPagerAds);
        tvSkipCounter = findViewById(R.id.tvSkipCounter);
        btnSkip = findViewById(R.id.btnSkip);
        
        // 设置跳过按钮点击事件
        tvSkipCounter.setOnClickListener(v -> skipToNextActivity());
    }
    
    private void setupAds() {
        // 使用本地广告资源
        adResources = new ArrayList<>();
        adResources.add(R.drawable.ad_618);
        adResources.add(R.drawable.ad_douyin);
        adResources.add(R.drawable.ad_meituan);
        
        SplashAdAdapter adapter = new SplashAdAdapter(this, adResources, this);
        viewPager.setAdapter(adapter);
        
        // 监听页面切换
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentAdPosition = position;
                super.onPageSelected(position);
            }
        });
    }
    
    /**
     * 开始自动滚动广告
     */
    private void startAutoScroll() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adResources != null && adResources.size() > 1) {
                    // 计算下一个位置，循环滚动
                    int nextPosition = (currentAdPosition + 1) % adResources.size();
                    viewPager.setCurrentItem(nextPosition, true);
                }
                // 继续下一次滚动
                handler.postDelayed(this, AD_SCROLL_INTERVAL);
            }
        }, AD_SCROLL_INTERVAL);
    }
    
    private void startCountdown() {
        updateCounterText();
        
        // 使用Handler延迟跳转
        handler.postDelayed(this::skipToNextActivity, AD_DURATION);
        
        // 每秒更新倒计时
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                remainingSeconds--;
                updateCounterText();
                
                if (remainingSeconds > 0) {
                    handler.postDelayed(this, COUNTER_INTERVAL);
                }
            }
        }, COUNTER_INTERVAL);
    }
    
    private void updateCounterText() {
        tvSkipCounter.setText(String.format("跳过 %ds", remainingSeconds));
    }
    
    private void skipToNextActivity() {
        // 清除所有待执行的延迟任务
        handler.removeCallbacksAndMessages(null);
        
        // 根据登录状态决定跳转页面
        Intent intent;
        if (userController.isLoggedIn()) {
            // 已登录，跳转到主页
            intent = new Intent(this, MainActivity.class);
        } else {
            // 未登录，跳转到登录页
            intent = new Intent(this, LoginActivity.class);
        }
        
        startActivity(intent);
        finish();
    }
    
    /**
     * 广告点击事件处理
     */
    @Override
    public void onAdClick(int position) {
        handler.removeCallbacksAndMessages(null); // 停止倒计时
        
        switch (position) {
            case 0: // 618活动
                tryOpenApp(TAOBAO_PACKAGE, "淘宝");
                break;
            case 1: // 抖音
                tryOpenApp(DOUYIN_PACKAGE, "抖音");
                break;
            case 2: // 美团
                tryOpenApp(MEITUAN_PACKAGE, "美团");
                break;
        }
    }
    
    /**
     * 尝试打开应用或引导用户下载
     * 
     * @param packageName 应用包名
     * @param appName 应用名称
     */
    private void tryOpenApp(String packageName, String appName) {
        try {
            // 检查应用是否已安装
            getPackageManager().getPackageInfo(packageName, 0);
            
            // 已安装，尝试多种方式打开应用
            boolean opened = false;
            
            // 方式1：使用getLaunchIntentForPackage
            try {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                if (launchIntent != null) {
                    Toast.makeText(this, "正在打开" + appName, Toast.LENGTH_SHORT).show();
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(launchIntent);
                    opened = true;
                    finish(); // 关闭当前活动
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // 方式2：如果方式1失败，使用显式Intent
            if (!opened) {
                try {
                    Intent intent = new Intent();
                    intent.setPackage(packageName);
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    opened = true;
                    finish(); // 关闭当前活动
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // 如果都失败了，显示提示
            if (!opened) {
                Toast.makeText(this, appName + "应用无法打开", Toast.LENGTH_SHORT).show();
                skipToNextActivity();
            }
            
        } catch (PackageManager.NameNotFoundException e) {
            // 未安装，尝试通过多种方式打开下载渠道
            Toast.makeText(this, appName + "未安装，正在跳转至下载页面", Toast.LENGTH_SHORT).show();
            tryOpenDownloadChannels(packageName, appName);
        }
    }
    
    /**
     * 尝试通过多种渠道引导用户下载应用
     * 
     * @param packageName 应用包名
     * @param appName 应用名称
     */
    private void tryOpenDownloadChannels(String packageName, String appName) {
        boolean opened = false;
        
        // 直接打开网页下载链接，避免应用市场scheme问题
        String webUrl;
        
        // 根据不同应用选择合适的下载页面
        switch (packageName) {
            case "com.taobao.taobao":
                webUrl = "https://m.taobao.com/";
                break;
            case DOUYIN_PACKAGE:
                webUrl = "https://www.douyin.com/download";
                break;
            case MEITUAN_PACKAGE:
                webUrl = "https://www.meituan.com/mobile/download/";
                break;
            default:
                webUrl = "https://m.coolapk.com/search?q=" + packageName;
        }
        
        try {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(webIntent);
            finish(); // 关闭当前活动
            opened = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 都失败了，显示提示
        if (!opened) {
            Toast.makeText(this, "无法打开" + appName + "应用或下载渠道，请手动下载", Toast.LENGTH_LONG).show();
            // 失败时回到默认流程
            skipToNextActivity();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 防止内存泄漏
        handler.removeCallbacksAndMessages(null);
    }
} 