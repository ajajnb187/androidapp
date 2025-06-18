package com.gxuwz.xinwenapp.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.gxuwz.xinwenapp.model.entity.NewsHistory;
import com.gxuwz.xinwenapp.model.service.NewsService;
import com.gxuwz.xinwenapp.util.SharedPreferencesUtil;
import com.gxuwz.xinwenapp.view.adapter.NewsAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 浏览历史页面
 * 
 * 作用：
 * 1. 显示用户浏览过的新闻列表
 * 2. 提供清空历史功能
 */
public class HistoryActivity extends AppCompatActivity {
    private RecyclerView rvHistory;
    private TextView tvEmpty;
    private Button btnClearHistory;
    private NewsController newsController;
    private NewsAdapter newsAdapter;
    private List<News> newsList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        
        // 初始化控制器
        newsController = new NewsController(this);
        
        // 初始化视图
        initViews();
        
        // 加载浏览历史
        loadHistory();
    }
    
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        rvHistory = findViewById(R.id.rv_history);
        tvEmpty = findViewById(R.id.tv_empty);
        btnClearHistory = findViewById(R.id.btn_clear_history);
        
        // 设置RecyclerView
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        newsAdapter = new NewsAdapter(this, newsList);
        
        // 设置点击事件，跳转到新闻详情页
        newsAdapter.setOnItemClickListener(news -> {
            Intent intent = new Intent(HistoryActivity.this, NewsDetailActivity.class);
            intent.putExtra("news_id", news.getUniqueKey());
            if (news.getUrl() != null && !news.getUrl().isEmpty()) {
                intent.putExtra("news_url", news.getUrl());
            }
            startActivity(intent);
        });
        
        rvHistory.setAdapter(newsAdapter);
        
        // 设置清空按钮点击事件
        btnClearHistory.setOnClickListener(v -> clearHistory());
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 每次恢复页面时重新加载历史记录，以便显示最新状态
        loadHistory();
    }
    
    private void loadHistory() {
        String phoneNumber = SharedPreferencesUtil.getString(this, "phone_number", "");
        if (phoneNumber.isEmpty()) {
            showEmptyView();
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示加载中
        showLoading(true);
        
        // 获取浏览历史
        newsController.getHistoryNewsList(new NewsService.ApiCallback<List<News>>() {
            @Override
            public void onSuccess(List<News> result) {
                showLoading(false);
                if (result != null && !result.isEmpty()) {
                    newsList.clear();
                    newsList.addAll(result);
                    newsAdapter.notifyDataSetChanged();
                    showContentView();
                    Log.d("HistoryActivity", "成功加载" + result.size() + "条历史记录");
                } else {
                    showEmptyView();
                    Log.d("HistoryActivity", "历史记录为空");
                }
            }
            
            @Override
            public void onError(String errorMsg) {
                showLoading(false);
                Toast.makeText(HistoryActivity.this, "获取历史记录失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                showEmptyView();
                Log.e("HistoryActivity", "获取历史记录失败: " + errorMsg);
            }
        });
    }
    
    private void clearHistory() {
        String phoneNumber = SharedPreferencesUtil.getString(this, "phone_number", "");
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示确认对话框
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("清空历史")
            .setMessage("确定要清空所有浏览历史吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                // 显示加载中
                showLoading(true);
                
                // 清空浏览历史
                newsController.clearHistory(new NewsService.ApiCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        showLoading(false);
                        if (result) {
                            newsList.clear();
                            newsAdapter.notifyDataSetChanged();
                            showEmptyView();
                            Toast.makeText(HistoryActivity.this, "历史记录已清空", Toast.LENGTH_SHORT).show();
                            Log.d("HistoryActivity", "历史记录已清空");
                        }
                    }
                    
                    @Override
                    public void onError(String errorMsg) {
                        showLoading(false);
                        Toast.makeText(HistoryActivity.this, "清空历史记录失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e("HistoryActivity", "清空历史记录失败: " + errorMsg);
                    }
                });
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    private void showEmptyView() {
        rvHistory.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
        btnClearHistory.setEnabled(false);
    }
    
    private void showContentView() {
        rvHistory.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        btnClearHistory.setEnabled(true);
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