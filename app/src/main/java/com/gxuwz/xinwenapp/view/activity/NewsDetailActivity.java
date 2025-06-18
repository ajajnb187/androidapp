package com.gxuwz.xinwenapp.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.gxuwz.xinwenapp.R;
import com.gxuwz.xinwenapp.controller.NewsController;
import com.gxuwz.xinwenapp.model.entity.News;
import com.gxuwz.xinwenapp.model.service.NewsService;
import com.gxuwz.xinwenapp.util.DateUtil;
import com.gxuwz.xinwenapp.util.SharedPreferencesUtil;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.UUID;

/**
 * 新闻详情页Activity
 * 
 * 作用：
 * 1. 显示新闻详情内容
 * 2. 支持使用WebView加载新闻网页
 * 3. 显示新闻基本信息
 * 4. 支持关注和分享新闻
 * 
 * 调用者：
 * NewsListFragment：新闻列表项点击后跳转
 */
public class NewsDetailActivity extends AppCompatActivity {
    private static final String TAG = "NewsDetailActivity";
    
    private Toolbar toolbar;
    private TextView tvTitle;
    private TextView tvSource;
    private TextView tvDate;
    private ImageView ivNewsPicture;
    private WebView webView;
    private ProgressBar progressBar;
    
    private NewsController newsController;
    private String newsId;
    private long startTime; // 记录阅读开始时间
    private long historyId = -1; // 历史记录ID
    private News currentNews;
    private String newsUrl;
    
    private Executor executor;
    private Handler mainHandler;
    
    private ImageView ivFavorite;
    private boolean isFavorited = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        
        // 初始化线程池和主线程Handler
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        // 初始化NewsController
        newsController = new NewsController(this);
        
        // 获取传入的参数
        Intent intent = getIntent();
        if (intent != null) {
            newsId = intent.getStringExtra("news_id");
            newsUrl = intent.getStringExtra("news_url");
            
            // 如果从通知或外部链接打开，可能只有URL没有ID
            if (newsId == null && newsUrl != null) {
                // 生成一个临时ID
                newsId = UUID.randomUUID().toString();
                Log.d(TAG, "从URL创建临时ID: " + newsId);
            }
        }
        
        // 记录开始阅读时间
        startTime = System.currentTimeMillis();
        
        // 初始化视图
        initViews();
        
        // 加载新闻详情
        loadNewsDetail(newsId);
        
        // 记录浏览历史在loadNewsDetail成功回调中进行，避免重复记录
    }
    
    /**
     * 初始化视图
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tv_title);
        tvSource = findViewById(R.id.tv_source);
        tvDate = findViewById(R.id.tv_date);
        ivNewsPicture = findViewById(R.id.iv_picture);
        webView = findViewById(R.id.web_view);
        progressBar = findViewById(R.id.progress_bar);
        
        // 初始化关注和分享按钮
        ivFavorite = findViewById(R.id.iv_favorite);
        ImageView ivShare = findViewById(R.id.iv_share);
        
        // 设置Toolbar
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        
        // 设置WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
            
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(NewsDetailActivity.this, "页面加载错误: " + description, Toast.LENGTH_SHORT).show();
            }
        });
        
        // 设置关注按钮点击事件
        ivFavorite.setOnClickListener(v -> toggleFollow());
        
        // 设置分享按钮点击事件
        ivShare.setOnClickListener(v -> shareNews());
    }
    
    /**
     * 显示或隐藏加载状态
     * @param show 是否显示
     */
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    /**
     * 加载新闻详情
     * @param uniqueKey 新闻唯一标识
     */
    private void loadNewsDetail(String uniqueKey) {
        Log.d(TAG, "加载新闻详情: uniqueKey=" + uniqueKey);
        showLoading(true);
        
        newsController.getNewsDetail(uniqueKey, new NewsService.ApiCallback<News>() {
            @Override
            public void onSuccess(News news) {
                Log.d(TAG, "新闻详情加载成功: " + news.getTitle());
                currentNews = news;
                displayNewsDetail(news);
                
                // 记录浏览历史
                if (news != null && news.getUniqueKey() != null && !news.getUniqueKey().isEmpty()) {
                    executor.execute(() -> {
                        try {
                            historyId = newsController.recordNewsHistory(news.getUniqueKey());
                            Log.d(TAG, "记录浏览历史: newsId=" + news.getUniqueKey() + ", historyId=" + historyId);
                        } catch (Exception e) {
                            Log.e(TAG, "记录浏览历史失败: " + e.getMessage(), e);
                        }
                    });
                }
                
                showLoading(false);
            }
            
            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "新闻详情加载失败: " + errorMsg);
                showLoading(false);
                
                // 处理错误：尝试使用WebView直接加载URL
                if (currentNews != null && currentNews.getUrl() != null && !currentNews.getUrl().isEmpty()) {
                    Log.d(TAG, "尝试使用WebView加载URL: " + currentNews.getUrl());
                    loadNewsWithWebView(currentNews.getUrl());
                } else if (newsUrl != null && !newsUrl.isEmpty()) {
                    Log.d(TAG, "尝试使用传入的URL加载: " + newsUrl);
                    loadNewsWithWebView(newsUrl);
                } else {
                    Toast.makeText(NewsDetailActivity.this, "加载新闻失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    /**
     * 使用WebView加载新闻URL
     * @param url 新闻URL
     */
    private void loadNewsWithWebView(String url) {
        // 清空原有内容
        tvTitle.setText("");
        tvSource.setText("");
        tvDate.setText("");
        
        // 直接使用已有的WebView加载URL
        if (webView != null) {
            webView.loadUrl(url);
            webView.setVisibility(View.VISIBLE);
            Log.d(TAG, "使用WebView加载URL: " + url);
        } else {
            Log.e(TAG, "WebView为空，无法加载URL");
            Toast.makeText(this, "无法加载网页", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 显示新闻详情
     * @param news 新闻对象
     */
    private void displayNewsDetail(News news) {
        if (news != null) {
            // 设置标题、来源、日期等信息
            tvTitle.setText(news.getTitle());
            
            // 设置来源信息
            String source = news.getSource();
            if (source == null || source.isEmpty()) {
                source = "未知来源";
            }
            tvSource.setText(source);
            
            // 设置日期
            String date = news.getDate();
            if (date == null || date.isEmpty()) {
                date = DateUtil.formatTime(news.getPublishTime());
            }
            tvDate.setText(date);
            
            // 加载图片
            if (news.getPicUrl() != null && !news.getPicUrl().isEmpty()) {
                loadNewsPicture(news.getPicUrl());
            } else {
                ivNewsPicture.setVisibility(View.GONE);
            }
            
            // 加载内容
            String content = news.getContent();
            if (content != null && !content.isEmpty()) {
                // 使用WebView加载HTML内容
                String htmlContent = generateHtmlContent(content);
                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
            } else if (news.getUrl() != null && !news.getUrl().isEmpty()) {
                // 如果没有内容但有URL，直接加载URL
                webView.loadUrl(news.getUrl());
            } else {
                // 没有内容也没有URL，显示提示
                webView.loadDataWithBaseURL(null, "<html><body><h2>暂无内容</h2><p>该新闻暂无详细内容</p></body></html>", "text/html", "UTF-8", null);
            }
            
            // 检查关注状态
            checkFavoriteStatus();
        }
    }
    
    /**
     * 生成HTML内容
     * @param content 原始内容
     * @return 格式化的HTML内容
     */
    private String generateHtmlContent(String content) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        html.append("<style>");
        html.append("body { font-family: sans-serif; margin: 15px; line-height: 1.6; color: #333; }");
        html.append("img { max-width: 100%; height: auto; margin: 10px 0; }");
        html.append("p { margin: 10px 0; font-size: 16px; }");
        html.append("</style></head><body>");
        
        // 检查是否已经是HTML格式
        if (!content.contains("<") || !content.contains(">")) {
            // 按换行符分割并添加段落标签
            String[] paragraphs = content.split("\n");
            for (String paragraph : paragraphs) {
                if (!paragraph.trim().isEmpty()) {
                    html.append("<p>").append(paragraph.trim()).append("</p>");
                }
            }
        } else {
            // 已经是HTML格式
            html.append(content);
        }
        
        html.append("</body></html>");
        return html.toString();
    }
    
    /**
     * 更新阅读时长
     */
    private void updateReadDuration() {
        if (historyId != -1) {
            long endTime = System.currentTimeMillis();
            int duration = (int) ((endTime - startTime) / 1000); // 转换为秒
            Log.d(TAG, "更新阅读时长: " + duration + "秒");
            newsController.updateReadDuration(historyId, duration);
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
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        updateReadDuration();
    }
    
    /**
     * 分享新闻
     */
    private void shareNews() {
        if (currentNews != null) {
            String shareContent = currentNews.getTitle();
            if (currentNews.getUrl() != null && !currentNews.getUrl().isEmpty()) {
                shareContent += "\n" + currentNews.getUrl();
            } else {
                shareContent += "\n来自新闻App的分享";
            }
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "分享新闻");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
            
            startActivity(Intent.createChooser(shareIntent, "分享新闻"));
            Log.d(TAG, "分享新闻: " + currentNews.getTitle());
        } else {
            Toast.makeText(this, "新闻内容加载中，请稍后再试", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 切换关注状态
     */
    private void toggleFollow() {
        if (currentNews == null || currentNews.getUniqueKey() == null) {
            Toast.makeText(this, "新闻信息不完整，无法关注", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (isFavorited) {
            // 取消关注
            newsController.unfavoriteNews(currentNews.getUniqueKey(), new NewsService.ApiCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    if (result) {
                        isFavorited = false;
                        updateFollowStatus();
                        Toast.makeText(NewsDetailActivity.this, "已取消关注", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "取消关注成功: " + currentNews.getTitle());
                    } else {
                        Toast.makeText(NewsDetailActivity.this, "取消关注失败，请重试", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "取消关注失败: " + currentNews.getTitle());
                    }
                }
                
                @Override
                public void onError(String errorMsg) {
                    Toast.makeText(NewsDetailActivity.this, "操作失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "取消关注异常: " + errorMsg);
                }
            });
        } else {
            // 添加关注
            newsController.addNewsFavorite(currentNews, new NewsService.ApiCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    if (result) {
                        isFavorited = true;
                        updateFollowStatus();
                        Toast.makeText(NewsDetailActivity.this, "已关注", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "关注成功: " + currentNews.getTitle());
                    } else {
                        Toast.makeText(NewsDetailActivity.this, "关注失败，请重试", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "关注失败: " + currentNews.getTitle());
                    }
                }
                
                @Override
                public void onError(String errorMsg) {
                    Toast.makeText(NewsDetailActivity.this, "操作失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "关注异常: " + errorMsg);
                }
            });
        }
    }
    
    /**
     * 更新关注按钮状态
     */
    private void updateFollowStatus() {
        if (ivFavorite != null) {
            if (isFavorited) {
                ivFavorite.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                ivFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            }
        }
    }
    
    /**
     * 检查是否已关注
     */
    private void checkFavoriteStatus() {
        if (currentNews != null && currentNews.getUniqueKey() != null) {
            executor.execute(() -> {
                try {
                    boolean favorited = newsController.isNewsFavorited(currentNews.getUniqueKey());
                    mainHandler.post(() -> {
                        isFavorited = favorited;
                        updateFollowStatus();
                        Log.d(TAG, "检查关注状态: " + (favorited ? "已关注" : "未关注"));
                    });
                } catch (Exception e) {
                    Log.e(TAG, "检查关注状态异常", e);
                }
            });
        }
    }
    
    /**
     * 加载新闻图片
     * @param picUrl 图片URL
     */
    private void loadNewsPicture(String picUrl) {
        if (picUrl != null && !picUrl.isEmpty()) {
            Log.d(TAG, "加载新闻图片: " + picUrl);
            
            // 检查URL是否为HTTPS
            if (picUrl.startsWith("http://")) {
                // 尝试将HTTP替换为HTTPS
                picUrl = picUrl.replace("http://", "https://");
                Log.d(TAG, "将HTTP图片URL替换为HTTPS");
            }
            
            String finalPicUrl = picUrl;
            Glide.with(this)
                    .load(finalPicUrl)
                    .placeholder(R.drawable.ic_news)
                    .error(R.drawable.ic_news)
                    .timeout(3000)
                    .fallback(R.drawable.ic_news)
                    .into(ivNewsPicture);
            ivNewsPicture.setVisibility(View.VISIBLE);
        } else {
            // 如果没有图片，隐藏ImageView
            ivNewsPicture.setVisibility(View.GONE);
            Log.d(TAG, "新闻无图片");
        }
    }
} 