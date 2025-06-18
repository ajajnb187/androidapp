package com.gxuwz.xinwenapp.model.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.gxuwz.xinwenapp.model.entity.News;
import com.gxuwz.xinwenapp.model.entity.NewsCategory;
import com.gxuwz.xinwenapp.model.entity.NewsFavorite;
import com.gxuwz.xinwenapp.model.entity.NewsHistory;
import com.gxuwz.xinwenapp.model.repository.NewsRepository;
import com.gxuwz.xinwenapp.util.DateUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 新闻业务逻辑服务类
 * 
 * 作用：
 * 1. 实现新闻相关的业务逻辑
 * 2. 处理新闻获取、分类管理、收藏、历史等操作
 * 3. 调用NewsRepository进行数据操作
 * 4. 调用网络API获取新闻数据
 * 
 * 调用者：
 * 1. NewsController：新闻控制器，处理新闻请求
 * 2. 各个Activity/Fragment：直接使用服务层进行业务操作
 */
public class NewsService {
    private static final String TAG = "NewsService";
    private static final String API_KEY = "6a0064d9515777a1b81a9b64582d28f9"; // 聚合数据API密钥
    private static final String NEWS_LIST_API = "http://v.juhe.cn/toutiao/index";
    private static final String NEWS_DETAIL_API = "http://v.juhe.cn/toutiao/content";
    
    private final NewsRepository newsRepository;
    private final OkHttpClient client;
    private final Gson gson;
    private final Executor executor;
    private final Handler mainHandler;
    
    /**
     * 网络请求回调接口
     * @param <T> 响应数据类型
     */
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMsg);
    }
    
    /**
     * 构造函数
     * @param context 应用上下文
     */
    public NewsService(Context context) {
        newsRepository = new NewsRepository(context);
        client = new OkHttpClient();
        gson = new Gson();
        executor = Executors.newFixedThreadPool(4);
        mainHandler = new Handler(Looper.getMainLooper());
        
        // 初始化默认新闻分类
        initDefaultCategories();
        
        // 定期清理过期缓存
        scheduleCacheCleaning();
    }
    
    /**
     * 初始化默认新闻分类
     */
    private void initDefaultCategories() {
        executor.execute(() -> {
            // 检查是否已有分类数据
            List<NewsCategory> categories = newsRepository.getEnabledCategories();
            if (categories == null || categories.isEmpty()) {
                Log.d(TAG, "初始化默认新闻分类");
                newsRepository.initDefaultCategories();
            } else {
                Log.d(TAG, "已有新闻分类，跳过初始化");
            }
        });
    }
    
    /**
     * 定期清理过期缓存
     */
    private void scheduleCacheCleaning() {
        executor.execute(() -> {
            Log.d(TAG, "清理过期新闻缓存");
            newsRepository.clearExpiredNewsCache();
        });
    }
    
    /**
     * 获取所有启用的新闻分类
     * @param callback 回调接口
     */
    public void getNewsCategoriesEnabled(ApiCallback<List<NewsCategory>> callback) {
        Log.d(TAG, "获取启用的新闻分类");
        executor.execute(() -> {
            List<NewsCategory> categories = newsRepository.getEnabledCategories();
            mainHandler.post(() -> {
                Log.d(TAG, "获取到 " + (categories != null ? categories.size() : 0) + " 个启用分类");
                callback.onSuccess(categories);
            });
        });
    }
    
    /**
     * 获取新闻列表
     * @param category 新闻分类
     * @param page 页码
     * @param pageSize 每页数量
     * @param callback 回调接口
     */
    public void getNewsList(String category, int page, int pageSize, ApiCallback<List<News>> callback) {
        Log.d(TAG, "获取新闻列表: 分类=" + category + ", 页码=" + page + ", 每页数量=" + pageSize);
        
        // 对于推荐分类(top)，使用国内(guonei)分类的数据，确保有图片和完整来源
        String fetchCategory = category;
        final boolean isTopCategory = "top".equals(category);
        if (isTopCategory) {
            fetchCategory = "guonei";
            Log.d(TAG, "推荐分类使用国内分类数据: " + fetchCategory);
        }
        
        // 保留原始分类用于返回
        final String finalCategory = category;
        final String finalFetchCategory = fetchCategory;
        
        // 直接从网络获取数据
        fetchNewsFromNetwork(finalFetchCategory, page, pageSize, new ApiCallback<List<News>>() {
            @Override
            public void onSuccess(List<News> result) {
                // 如果是top分类，将获取的guonei新闻的分类修改为top
                if (isTopCategory) {
                    for (News news : result) {
                        news.setCategory(finalCategory);
                    }
                    Log.d(TAG, "将从网络获取的国内分类新闻重新标记为推荐分类");
                }
                callback.onSuccess(result);
            }
            
            @Override
            public void onError(String errorMsg) {
                callback.onError(errorMsg);
            }
        });
    }
    
    /**
     * 从网络获取新闻列表
     * @param category 新闻分类
     * @param page 页码
     * @param pageSize 每页数量
     * @param callback 回调接口
     */
    private void fetchNewsFromNetwork(String category, int page, int pageSize, ApiCallback<List<News>> callback) {
        Log.d(TAG, "从网络获取新闻列表: 分类=" + category + ", 页码=" + page + ", 每页数量=" + pageSize);
        
        // 从网络API获取新闻列表
        HttpUrl.Builder urlBuilder = HttpUrl.parse(NEWS_LIST_API).newBuilder();
        urlBuilder.addQueryParameter("key", API_KEY);
        urlBuilder.addQueryParameter("type", category);
        urlBuilder.addQueryParameter("page", String.valueOf(page));
        urlBuilder.addQueryParameter("page_size", String.valueOf(pageSize));
        // 只返回有内容详情的新闻
        urlBuilder.addQueryParameter("is_filter", "1");
        
        String url = urlBuilder.build().toString();
        Log.d(TAG, "请求URL: " + url);
        
        // 使用OkHttp发起网络请求
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        // 发起异步请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String errorMsg = "获取新闻失败: " + e.getMessage();
                Log.e(TAG, errorMsg, e);
                mainHandler.post(() -> callback.onError(errorMsg));
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        String errorMsg = "服务器响应错误: " + response.code();
                        Log.e(TAG, errorMsg);
                        mainHandler.post(() -> callback.onError(errorMsg));
                        return;
                    }
                    
                    String responseBody = response.body().string();
                    Log.d(TAG, "服务器响应: " + responseBody.substring(0, Math.min(responseBody.length(), 200)) + "...");
                    
                    // 解析JSON
                    List<News> newsList = parseNewsJson(responseBody, category);
                    
                    // 检查图片和来源，特别关注推荐分类
                    if (newsList != null && !newsList.isEmpty()) {
                        for (News news : newsList) {
                            // 补充来源信息如果没有
                            if (news.getSource() == null || news.getSource().isEmpty()) {
                                if ("top".equals(category)) {
                                    news.setSource("热门推荐");
                                    Log.d(TAG, "为推荐新闻补充来源: 热门推荐");
                                } else if ("guonei".equals(category)) {
                                    news.setSource("国内新闻");
                                    Log.d(TAG, "为国内新闻补充来源: 国内新闻");
                                } else if ("guoji".equals(category)) {
                                    news.setSource("国际新闻");
                                } else if ("yule".equals(category)) {
                                    news.setSource("娱乐新闻");
                                } else {
                                    news.setSource(category + "新闻");
                                }
                            }
                        }
                    }
                    
                    // 返回结果
                    mainHandler.post(() -> callback.onSuccess(newsList));
                } catch (Exception e) {
                    String errorMsg = "解析新闻数据失败: " + e.getMessage();
                    Log.e(TAG, errorMsg, e);
                    mainHandler.post(() -> callback.onError(errorMsg));
                } finally {
                    response.close();
                }
            }
        });
    }
    
    /**
     * 解析新闻JSON数据
     * @param jsonStr JSON字符串
     * @param category 新闻分类
     * @return 新闻列表
     */
    private List<News> parseNewsJson(String jsonStr, String category) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonStr).getAsJsonObject();
            int errorCode = jsonObject.get("error_code").getAsInt();
            
            if (errorCode == 0) {
                // 请求成功
                JsonObject result = jsonObject.getAsJsonObject("result");
                JsonArray dataArray = result.getAsJsonArray("data");
                
                if (dataArray == null || dataArray.size() == 0) {
                    Log.w(TAG, "API返回数据为空数组: 分类=" + category);
                    return new ArrayList<>();
                }
                
                List<News> newsList = new ArrayList<>();
                
                for (JsonElement element : dataArray) {
                    try {
                        JsonObject newsObj = element.getAsJsonObject();
                        
                        News news = new News();
                        
                        // 设置必要字段
                        String uniqueKey = newsObj.has("uniquekey") ? 
                                (newsObj.get("uniquekey").isJsonNull() ? "" : newsObj.get("uniquekey").getAsString()) : "";
                        news.setUniqueKey(uniqueKey);
                        
                        String title = newsObj.has("title") ? 
                                (newsObj.get("title").isJsonNull() ? "" : newsObj.get("title").getAsString()) : "";
                        news.setTitle(title);
                        
                        String date = newsObj.has("date") ? 
                                (newsObj.get("date").isJsonNull() ? "" : newsObj.get("date").getAsString()) : "";
                        // 如果date为空，使用当前时间
                        if (date.isEmpty()) {
                            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                            Log.d(TAG, "为新闻设置当前时间: " + date);
                        }
                        news.setDate(date);
                        
                        // 明确设置分类，确保不为空
                        news.setCategory(category);
                        
                        String author = newsObj.has("author_name") ? 
                                (newsObj.get("author_name").isJsonNull() ? "" : newsObj.get("author_name").getAsString()) : "";
                        news.setAuthor(author);
                        
                        String url = newsObj.has("url") ? 
                                (newsObj.get("url").isJsonNull() ? "" : newsObj.get("url").getAsString()) : "";
                        news.setUrl(url);
                        
                        String source = author;
                        news.setSource(source);
                        
                        news.setPublishTime(date);
                        
                        // 设置图片URL - 检查多种可能的字段名
                        if (newsObj.has("thumbnail_pic_s") && !newsObj.get("thumbnail_pic_s").isJsonNull()) {
                            news.setPicUrl(newsObj.get("thumbnail_pic_s").getAsString());
                        } else if (newsObj.has("thumbnail_pic") && !newsObj.get("thumbnail_pic").isJsonNull()) {
                            news.setPicUrl(newsObj.get("thumbnail_pic").getAsString());
                        } else if (newsObj.has("pic_url") && !newsObj.get("pic_url").isJsonNull()) {
                            news.setPicUrl(newsObj.get("pic_url").getAsString());
                        }
                        
                        // 检查图片URL是否为HTTPS
                        String picUrl = news.getPicUrl();
                        if (picUrl != null && !picUrl.isEmpty()) {
                            // 检查URL是否为HTTPS
                            if (picUrl.startsWith("http://")) {
                                // 尝试将HTTP替换为HTTPS
                                String httpsUrl = picUrl.replace("http://", "https://");
                                news.setPicUrl(httpsUrl);
                                Log.d(TAG, "将HTTP图片URL替换为HTTPS: " + httpsUrl);
                            }
                        }
                        
                        // 检查uniqueKey是否为空
                        if (news.getUniqueKey() == null || news.getUniqueKey().trim().isEmpty()) {
                            // 使用标题和时间戳生成唯一ID
                            String uniqueId = (news.getTitle() + System.currentTimeMillis()).hashCode() + "";
                            news.setUniqueKey(uniqueId);
                            Log.d(TAG, "为新闻生成唯一ID: " + uniqueId);
                        }
                        
                        // 跳过标题为空的新闻
                        if (news.getTitle() == null || news.getTitle().trim().isEmpty()) {
                            Log.w(TAG, "跳过标题为空的新闻项");
                            continue;
                        }
                        
                        newsList.add(news);
                    } catch (Exception e) {
                        Log.e(TAG, "解析单条新闻数据失败: " + e.getMessage(), e);
                        // 继续处理下一条新闻
                    }
                }
                
                Log.d(TAG, "解析到 " + newsList.size() + " 条新闻");
                return newsList;
            } else {
                // 请求失败
                String reason = jsonObject.get("reason").getAsString();
                Log.e(TAG, "API错误: " + reason + ", 错误码: " + errorCode);
                
                // API请求限制错误，抛出友好错误信息
                if (errorCode == 10012 || reason.contains("超过每日可允许请求次数")) {
                    throw new Exception("今日API请求次数已达上限，请明天再试");
                }
                
                throw new Exception(reason);
            }
        } catch (Exception e) {
            Log.e(TAG, "解析JSON数据失败: " + e.getMessage(), e);
            throw new RuntimeException("解析JSON数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取新闻详情
     * @param uniqueKey 新闻ID
     * @param callback 回调接口
     */
    public void getNewsDetail(String uniqueKey, ApiCallback<News> callback) {
        Log.d(TAG, "获取新闻详情: uniqueKey=" + uniqueKey);
        // 先从本地获取
        executor.execute(() -> {
            News cachedNews = newsRepository.getNewsDetail(uniqueKey);
            if (cachedNews != null && cachedNews.getContent() != null && !cachedNews.getContent().isEmpty()) {
                // 有缓存详情
                Log.d(TAG, "从本地缓存获取到新闻详情");
                mainHandler.post(() -> callback.onSuccess(cachedNews));
            } else if (cachedNews != null) {
                Log.d(TAG, "本地缓存中没有新闻内容，从网络获取详情");
                // 有缓存基础信息但没有详情内容，从网络获取详情
                fetchNewsDetailFromNetwork(uniqueKey, cachedNews, callback);
            } else {
                Log.d(TAG, "本地无缓存，从网络获取新闻详情");
                // 无缓存，从网络获取
                fetchNewsDetailFromNetwork(uniqueKey, null, callback);
            }
        });
    }
    
    /**
     * 从网络获取新闻详情
     * @param uniqueKey 新闻ID
     * @param cachedNews 缓存的新闻对象（可能为null）
     * @param callback 回调接口
     */
    private void fetchNewsDetailFromNetwork(String uniqueKey, News cachedNews, ApiCallback<News> callback) {
        Log.d(TAG, "开始从网络获取新闻详情: uniqueKey=" + uniqueKey);
        
        // 构建请求URL
        HttpUrl.Builder urlBuilder = HttpUrl.parse(NEWS_DETAIL_API).newBuilder();
        urlBuilder.addQueryParameter("key", API_KEY);
        urlBuilder.addQueryParameter("uniquekey", uniqueKey);
        
        String url = urlBuilder.build().toString();
        Log.d(TAG, "请求URL: " + url);
        
        Request request = new Request.Builder()
                .url(url)
                .build();
                
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "获取新闻详情网络请求失败: " + e.getMessage(), e);
                
                // 如果有缓存的基础信息，也返回给用户
                if (cachedNews != null) {
                    mainHandler.post(() -> {
                        Log.d(TAG, "网络请求失败，返回缓存的基础信息");
                        callback.onSuccess(cachedNews);
                    });
                } else {
                    mainHandler.post(() -> callback.onError("网络请求失败: " + e.getMessage()));
                }
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "获取新闻详情服务器响应错误: " + response.code());
                    
                    // 如果有缓存的基础信息，也返回给用户
                    if (cachedNews != null) {
                        mainHandler.post(() -> {
                            Log.d(TAG, "服务器响应错误，返回缓存的基础信息");
                            callback.onSuccess(cachedNews);
                        });
                    } else {
                        mainHandler.post(() -> callback.onError("服务器响应错误: " + response.code()));
                    }
                    return;
                }
                
                String jsonStr = response.body().string();
                Log.d(TAG, "获取到新闻详情响应: " + jsonStr.substring(0, Math.min(200, jsonStr.length())) + "...");
                
                try {
                    JsonObject jsonObject = JsonParser.parseString(jsonStr).getAsJsonObject();
                    int errorCode = jsonObject.get("error_code").getAsInt();
                    
                    if (errorCode == 0) {
                        // 请求成功
                        JsonObject resultObj = jsonObject.getAsJsonObject("result");
                        
                        News news;
                        if (cachedNews != null) {
                            // 使用缓存的基本信息
                            news = cachedNews;
                        } else {
                            // 创建新的新闻对象
                            news = new News();
                            news.setUniqueKey(uniqueKey);
                            
                            // 从detail中获取基本信息
                            if (resultObj.has("detail")) {
                                JsonObject detailObj = resultObj.getAsJsonObject("detail");
                                
                                if (detailObj.has("title"))
                                    news.setTitle(detailObj.get("title").getAsString());
                                
                                if (detailObj.has("date"))
                                    news.setDate(detailObj.get("date").getAsString());
                                
                                if (detailObj.has("category"))
                                    news.setCategory(detailObj.get("category").getAsString());
                                
                                if (detailObj.has("author_name"))
                                    news.setAuthor(detailObj.get("author_name").getAsString());
                                
                                if (detailObj.has("url"))
                                    news.setUrl(detailObj.get("url").getAsString());
                                
                                if (detailObj.has("thumbnail_pic_s"))
                                    news.setPicUrl(detailObj.get("thumbnail_pic_s").getAsString());
                                
                                if (detailObj.has("author_name"))
                                    news.setSource(detailObj.get("author_name").getAsString());
                                
                                if (detailObj.has("date"))
                                    news.setPublishTime(detailObj.get("date").getAsString());
                            }
                        }
                        
                        // 设置内容
                        if (resultObj.has("content")) {
                            String content = resultObj.get("content").getAsString();
                            news.setContent(content);
                            Log.d(TAG, "获取到新闻内容，长度: " + content.length());
                            
                            // 更新到数据库
                            if (news.getUniqueKey() != null && !news.getUniqueKey().isEmpty()) {
                                newsRepository.updateNewsContent(news.getUniqueKey(), content);
                                Log.d(TAG, "更新新闻内容到数据库");
                            }
                        }
                        
                        // 更新缓存时间
                        news.setCacheTime(System.currentTimeMillis());
                        
                        // 返回结果
                        final News finalNews = news;
                        mainHandler.post(() -> callback.onSuccess(finalNews));
                    } else {
                        // 请求失败
                        String reason = jsonObject.get("reason").getAsString();
                        Log.e(TAG, "获取新闻详情API错误: " + reason + ", 错误码: " + errorCode);
                        
                        // 如果是错误码223502（暂查询不到相关新闻详情）
                        if (errorCode == 223502) {
                            if (cachedNews != null) {
                                // 如果有缓存，返回缓存信息
                                mainHandler.post(() -> {
                                    Log.d(TAG, "无法获取详情，返回缓存的基础信息");
                                    callback.onSuccess(cachedNews);
                                });
                            } else {
                                // 处理推荐分类新闻详情获取失败的情况
                                // 尝试从URL直接加载
                                if (cachedNews != null && cachedNews.getUrl() != null && !cachedNews.getUrl().isEmpty()) {
                                    Log.d(TAG, "尝试使用URL作为内容: " + cachedNews.getUrl());
                                    
                                    // 创建HTML内容，让WebView直接加载URL
                                    String htmlContent = "<p>请点击 <a href='" + cachedNews.getUrl() + "'>这里</a> 查看完整新闻内容</p>";
                                    cachedNews.setContent(htmlContent);
                                    
                                    final News urlNews = cachedNews;
                                    mainHandler.post(() -> callback.onSuccess(urlNews));
                                } else {
                                    mainHandler.post(() -> callback.onError("无法获取新闻详情: " + reason));
                                }
                            }
                        } else {
                            mainHandler.post(() -> callback.onError(reason));
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析新闻详情响应数据失败: " + e.getMessage(), e);
                    
                    // 如果有缓存的基础信息，也返回给用户
                    if (cachedNews != null) {
                        mainHandler.post(() -> {
                            Log.d(TAG, "解析失败，返回缓存的基础信息");
                            callback.onSuccess(cachedNews);
                        });
                    } else {
                        mainHandler.post(() -> callback.onError("解析响应数据失败: " + e.getMessage()));
                    }
                }
            }
        });
    }
    
    /**
     * 搜索新闻
     * @param keyword 关键词
     * @param callback 回调接口
     */
    public void searchNews(String keyword, ApiCallback<List<News>> callback) {
        Log.d(TAG, "搜索新闻: 关键词=" + keyword);
        executor.execute(() -> {
            List<News> results = newsRepository.searchNews(keyword);
            Log.d(TAG, "搜索结果: " + (results != null ? results.size() : 0) + " 条新闻");
            mainHandler.post(() -> callback.onSuccess(results));
        });
    }
    
    /**
     * 记录用户浏览历史
     * @param phoneNumber 用户手机号
     * @param newsUniqueKey 新闻ID
     * @return 记录ID
     */
    public long recordNewsHistory(String phoneNumber, String newsUniqueKey) {
        Log.d(TAG, "记录用户浏览历史: 用户=" + phoneNumber + ", 新闻ID=" + newsUniqueKey);
        
        try {
            // 在后台线程中执行数据库操作
            return newsRepository.recordNewsHistory(phoneNumber, newsUniqueKey);
        } catch (Exception e) {
            Log.e(TAG, "记录用户浏览历史失败: " + e.getMessage(), e);
            return -1;
        }
    }
    
    /**
     * 更新阅读时长
     * @param historyId 历史记录ID
     * @param duration 阅读时长（秒）
     */
    public void updateReadDuration(long historyId, int duration) {
        Log.d(TAG, "更新阅读时长: 历史ID=" + historyId + ", 时长=" + duration + "秒");
        newsRepository.updateReadDuration(historyId, duration);
    }
    
    /**
     * 获取用户浏览历史
     * @param phoneNumber 用户手机号
     * @param callback 回调接口
     */
    public void getUserHistory(String phoneNumber, ApiCallback<List<News>> callback) {
        Log.d(TAG, "获取用户浏览历史: 用户=" + phoneNumber);
        executor.execute(() -> {
            List<NewsHistory> historyList = newsRepository.getUserHistory(phoneNumber, 50, 0);
            List<News> newsList = new ArrayList<>();
            
            for (NewsHistory history : historyList) {
                News news = newsRepository.getNewsDetail(history.getNewsUniqueKey());
                if (news != null) {
                    newsList.add(news);
                }
            }
            
            Log.d(TAG, "获取到 " + newsList.size() + " 条历史记录");
            mainHandler.post(() -> callback.onSuccess(newsList));
        });
    }
    
    /**
     * 获取用户浏览历史新闻列表
     * @param phoneNumber 用户手机号
     * @param limit 数量限制
     * @param offset 偏移量
     * @param callback 回调接口
     */
    public void getUserHistoryNewsList(String phoneNumber, int limit, int offset, ApiCallback<List<News>> callback) {
        Log.d(TAG, "获取用户浏览历史新闻列表: 用户=" + phoneNumber + ", 限制=" + limit + ", 偏移=" + offset);
        executor.execute(() -> {
            try {
                // 获取历史记录
                List<NewsHistory> historyList = newsRepository.getUserHistory(phoneNumber, limit, offset);
                Log.d(TAG, "获取到历史记录数量: " + historyList.size());
                
                if (historyList.isEmpty()) {
                    mainHandler.post(() -> callback.onSuccess(new ArrayList<>()));
                    return;
                }
                
                // 根据历史记录获取新闻详情
                List<News> newsList = new ArrayList<>();
                Set<String> processedNewsIds = new HashSet<>(); // 用于去重
                
                for (NewsHistory history : historyList) {
                    String newsId = history.getNewsUniqueKey();
                    
                    // 如果已经处理过这个新闻ID，则跳过
                    if (processedNewsIds.contains(newsId)) {
                        Log.d(TAG, "跳过重复的历史新闻: " + newsId);
                        continue;
                    }
                    
                    Log.d(TAG, "从历史记录获取新闻: " + newsId);
                    News news = newsRepository.getNewsDetail(newsId);
                    
                    if (news != null) {
                        // 设置特殊标签，表示这是历史记录
                        news.setSpecialTag("历史");
                        newsList.add(news);
                        processedNewsIds.add(newsId); // 标记为已处理
                        Log.d(TAG, "添加历史新闻: " + news.getTitle());
                    } else {
                        Log.w(TAG, "历史记录对应的新闻不存在: " + newsId);
                    }
                }
                
                Log.d(TAG, "获取到 " + newsList.size() + " 条历史记录新闻");
                mainHandler.post(() -> callback.onSuccess(newsList));
            } catch (Exception e) {
                Log.e(TAG, "获取用户浏览历史新闻列表失败: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError("获取浏览历史失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 清空用户浏览历史
     * @param phoneNumber 用户手机号
     */
    public void clearUserHistory(String phoneNumber) {
        Log.d(TAG, "清空用户浏览历史: 用户=" + phoneNumber);
        newsRepository.clearUserHistory(phoneNumber);
    }
    
    /**
     * 检查新闻是否已关注
     * @param newsId 新闻ID
     * @param phoneNumber 用户手机号
     * @return 是否已关注
     */
    public boolean isNewsFavorited(String newsId, String phoneNumber) {
        try {
            // 这个方法可能会在主线程调用，但它只是查询操作，不会阻塞太久
            NewsFavorite favorite = newsRepository.getNewsFavorite(newsId, phoneNumber);
            return favorite != null;
        } catch (Exception e) {
            Log.e(TAG, "检查新闻关注状态失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 添加新闻关注
     * @param news 新闻对象
     * @param phoneNumber 用户手机号
     * @return 是否成功
     */
    public boolean addNewsFavorite(News news, String phoneNumber) {
        try {
            // 先保存新闻到数据库（这个操作已经在后台线程执行）
            newsRepository.saveNewsToCache(List.of(news));
            
            // 添加关注记录（这个操作已修改为在后台线程执行）
            boolean result = newsRepository.addFavorite(phoneNumber, news.getUniqueKey());
            Log.d(TAG, "添加新闻关注成功: newsId=" + news.getUniqueKey() + ", user=" + phoneNumber);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "添加新闻关注失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 取消新闻关注
     * @param newsId 新闻ID
     * @param phoneNumber 用户手机号
     * @return 是否成功
     */
    public boolean removeNewsFavorite(String newsId, String phoneNumber) {
        try {
            int rowsDeleted = newsRepository.removeFavorite(phoneNumber, newsId);
            Log.d(TAG, "取消新闻关注: newsId=" + newsId + ", user=" + phoneNumber + ", 结果=" + (rowsDeleted > 0));
            return rowsDeleted > 0;
        } catch (Exception e) {
            Log.e(TAG, "取消新闻关注失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取用户关注的新闻列表
     * @param phoneNumber 用户手机号
     * @param callback 回调
     */
    public void getFavoriteNewsList(String phoneNumber, ApiCallback<List<News>> callback) {
        executor.execute(() -> {
            try {
                List<News> favoriteNews = newsRepository.getUserFavorites(phoneNumber, 50, 0);
                
                // 更新特殊标签为"关注"而不是"收藏"
                for (News news : favoriteNews) {
                    news.setSpecialTag("关注");
                }
                
                Log.d(TAG, "获取用户关注新闻: user=" + phoneNumber + ", 数量=" + favoriteNews.size());
                mainHandler.post(() -> callback.onSuccess(favoriteNews));
            } catch (Exception e) {
                Log.e(TAG, "获取用户关注新闻失败: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError("获取关注新闻失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 获取所有新闻分类（包括已禁用的）
     * @param callback 回调接口
     */
    public void getAllNewsCategories(ApiCallback<List<NewsCategory>> callback) {
        Log.d(TAG, "获取所有新闻分类");
        executor.execute(() -> {
            try {
                List<NewsCategory> categories = newsRepository.getAllCategories();
                mainHandler.post(() -> {
                    Log.d(TAG, "获取到 " + (categories != null ? categories.size() : 0) + " 个分类");
                    callback.onSuccess(categories);
                });
            } catch (Exception e) {
                Log.e(TAG, "获取新闻分类异常", e);
                mainHandler.post(() -> callback.onError("获取分类失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 更新新闻分类设置
     * @param categories 更新后的分类列表
     * @param callback 回调接口
     */
    public void updateNewsCategories(List<NewsCategory> categories, ApiCallback<Boolean> callback) {
        Log.d(TAG, "更新新闻分类设置: " + categories.size() + "个分类");
        executor.execute(() -> {
            try {
                for (NewsCategory category : categories) {
                    newsRepository.updateCategory(category);
                }
                mainHandler.post(() -> {
                    Log.d(TAG, "更新新闻分类成功");
                    callback.onSuccess(true);
                });
            } catch (Exception e) {
                Log.e(TAG, "更新新闻分类异常", e);
                mainHandler.post(() -> callback.onError("更新分类失败: " + e.getMessage()));
            }
        });
    }
} 