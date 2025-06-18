package com.gxuwz.xinwenapp.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gxuwz.xinwenapp.R;
import com.gxuwz.xinwenapp.controller.NewsController;
import com.gxuwz.xinwenapp.model.entity.News;
import com.gxuwz.xinwenapp.model.service.NewsService;
import com.gxuwz.xinwenapp.util.SharedPreferencesUtil;
import com.gxuwz.xinwenapp.view.adapter.NewsAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 关注列表页面
 * 
 * 作用：
 * 1. 显示用户关注的新闻列表
 * 2. 支持取消关注操作
 */
public class FollowsActivity extends AppCompatActivity {
    private RecyclerView rvFollows;
    private TextView tvEmpty;
    private NewsController newsController;
    private NewsAdapter newsAdapter;
    private List<News> newsList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follows);
        
        // 初始化控制器
        newsController = new NewsController(this);
        
        // 初始化视图
        initViews();
        
        // 加载关注列表
        loadFollows();
    }
    
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        rvFollows = findViewById(R.id.rv_follows);
        tvEmpty = findViewById(R.id.tv_empty);
        
        // 设置RecyclerView
        rvFollows.setLayoutManager(new LinearLayoutManager(this));
        newsAdapter = new NewsAdapter(this, newsList);
        
        // 设置点击事件，跳转到新闻详情页
        newsAdapter.setOnItemClickListener(news -> {
            Intent intent = new Intent(FollowsActivity.this, NewsDetailActivity.class);
            intent.putExtra("news_id", news.getUniqueKey());
            if (news.getUrl() != null && !news.getUrl().isEmpty()) {
                intent.putExtra("news_url", news.getUrl());
            }
            startActivity(intent);
        });
        
        // 设置长按取消关注
        newsAdapter.setOnItemLongClickListener((position, news) -> {
            cancelFollow(position, news);
            return true;
        });
        
        rvFollows.setAdapter(newsAdapter);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 每次恢复页面时重新加载关注列表，以便显示最新状态
        loadFollows();
    }
    
    private void loadFollows() {
        String phoneNumber = SharedPreferencesUtil.getString(this, "phone_number", "");
        if (phoneNumber.isEmpty()) {
            showEmptyView();
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示加载中
        showLoading(true);
        
        // 获取关注列表
        newsController.getFavoriteNewsList(new NewsService.ApiCallback<List<News>>() {
            @Override
            public void onSuccess(List<News> result) {
                showLoading(false);
                if (result != null && !result.isEmpty()) {
                    newsList.clear();
                    newsList.addAll(result);
                    newsAdapter.notifyDataSetChanged();
                    showContentView();
                    Log.d("FollowsActivity", "成功加载" + result.size() + "条关注新闻");
                } else {
                    showEmptyView();
                    Log.d("FollowsActivity", "关注列表为空");
                }
            }
            
            @Override
            public void onError(String errorMsg) {
                showLoading(false);
                Toast.makeText(FollowsActivity.this, "获取关注列表失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                showEmptyView();
                Log.e("FollowsActivity", "获取关注列表失败: " + errorMsg);
            }
        });
    }
    
    private void cancelFollow(int position, News news) {
        newsController.unfavoriteNews(news.getUniqueKey(), new NewsService.ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    newsList.remove(position);
                    newsAdapter.notifyItemRemoved(position);
                    newsAdapter.notifyItemRangeChanged(position, newsList.size());
                    
                    if (newsList.isEmpty()) {
                        showEmptyView();
                    }
                    
                    Toast.makeText(FollowsActivity.this, "已取消关注", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onError(String errorMsg) {
                Toast.makeText(FollowsActivity.this, "取消关注失败: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showEmptyView() {
        rvFollows.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
    }
    
    private void showContentView() {
        rvFollows.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }
    
    private void showLoading(boolean show) {
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
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