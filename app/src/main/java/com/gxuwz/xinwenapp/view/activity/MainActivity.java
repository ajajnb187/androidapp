package com.gxuwz.xinwenapp.view.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.gxuwz.xinwenapp.R;
import com.gxuwz.xinwenapp.controller.NewsController;
import com.gxuwz.xinwenapp.view.adapter.MainFragmentAdapter;
import com.gxuwz.xinwenapp.view.fragment.NewsListFragment;
import com.gxuwz.xinwenapp.view.fragment.ProfileFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 主界面Activity
 * 
 * 作用：
 * 1. 主界面容器，包含底部导航栏
 * 2. 管理新闻列表和个人中心Fragment
 * 
 * 调用者：
 * 1. SplashActivity：启动页跳转
 * 2. LoginActivity：登录成功后跳转
 */
public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    private static final String TAG = "MainActivity";
    
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigation;
    
    private NewsController newsController;
    private List<Fragment> fragmentList;
    private MainFragmentAdapter mainAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: 初始化主界面");
        
        newsController = new NewsController(this);
        
        initViews();
        setupFragments();
    }
    
    private void initViews() {
        viewPager = findViewById(R.id.view_pager);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        
        // 设置底部导航点击监听
        bottomNavigation.setOnItemSelectedListener(this);
        
        // 禁用ViewPager2的滑动
        viewPager.setUserInputEnabled(false);
        
        // ViewPager2页面切换监听
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "ViewPager页面切换: position=" + position);
                // 同步底部导航的选中项
                if (position == 0) {
                    bottomNavigation.setSelectedItemId(R.id.nav_news);
                } else if (position == 1) {
                    bottomNavigation.setSelectedItemId(R.id.nav_profile);
                }
            }
        });
    }
    
    private void setupFragments() {
        Log.d(TAG, "setupFragments: 设置Fragment");
        fragmentList = new ArrayList<>();
        fragmentList.add(new NewsListFragment());
        fragmentList.add(new ProfileFragment());
        
        // 设置ViewPager2的适配器
        mainAdapter = new MainFragmentAdapter(this, fragmentList);
        viewPager.setAdapter(mainAdapter);
        
        // 默认显示新闻页面
        viewPager.setCurrentItem(0);
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.nav_news) {
            Log.d(TAG, "切换到新闻页面");
            viewPager.setCurrentItem(0);
            return true;
        } else if (itemId == R.id.nav_profile) {
            Log.d(TAG, "切换到个人中心页面");
            viewPager.setCurrentItem(1);
            return true;
        }
        
        return false;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }
} 