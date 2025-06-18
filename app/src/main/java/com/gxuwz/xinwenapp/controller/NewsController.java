package com.gxuwz.xinwenapp.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.gxuwz.xinwenapp.model.entity.News;
import com.gxuwz.xinwenapp.model.entity.NewsCategory;
import com.gxuwz.xinwenapp.model.entity.User;
import com.gxuwz.xinwenapp.model.service.NewsService;
import com.gxuwz.xinwenapp.model.service.UserService;
import com.gxuwz.xinwenapp.util.SharedPreferencesUtil;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * 新闻控制器类
 * 
 * 作用：
 * 1. 处理新闻相关的请求
 * 2. 调用NewsService处理业务逻辑
 * 3. 协调用户登录状态和新闻交互功能
 * 
 * 调用者：
 * 1. Activity/Fragment：界面调用控制器处理新闻相关操作
 */
public class NewsController {
    private static final String TAG = "NewsController";
    private final NewsService newsService;
    private final UserService userService;
    private final Context context;
    private final Executor executor;
    private final Handler mainHandler;
    
    /**
     * 构造函数
     * @param context 应用上下文
     */
    public NewsController(Context context) {
        this.context = context;
        newsService = new NewsService(context);
        userService = new UserService(context);
        executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * 获取所有启用的新闻分类
     * @param callback 回调接口
     */
    public void getNewsCategories(NewsService.ApiCallback<List<NewsCategory>> callback) {
        newsService.getNewsCategoriesEnabled(callback);
    }
    
    /**
     * 获取新闻列表
     * @param category 新闻分类
     * @param page 页码
     * @param pageSize 每页数量
     * @param callback 回调接口
     */
    public void getNewsList(String category, int page, int pageSize, NewsService.ApiCallback<List<News>> callback) {
        Log.d(TAG, "获取新闻列表: 分类=" + category + ", 页码=" + page + ", 每页数量=" + pageSize);
        
        // 处理推荐与top类型的映射
        String apiCategory = category;
        if ("推荐".equals(category)) {
            apiCategory = "top";
            Log.d(TAG, "将UI分类'推荐'映射为API分类'top'");
        }
        
        // 使用包装回调来增加日志
        final String finalCategory = apiCategory;
        NewsService.ApiCallback<List<News>> loggingCallback = new NewsService.ApiCallback<List<News>>() {
            @Override
            public void onSuccess(List<News> result) {
                // 为每个新闻项增加标记，表明是推荐分类获取的，方便后续处理
                if ("top".equals(finalCategory) && result != null) {
                    for (News news : result) {
                        if (news.getSource() == null || news.getSource().isEmpty() 
                                || "热门推荐".equals(news.getSource())) {
                            // 让推荐分类的新闻在UI上显示为"推荐"
                            news.setSpecialTag("推荐");
                        }
                    }
                }
                
                Log.d(TAG, "获取新闻列表成功: 分类=" + finalCategory + ", 返回数量=" + (result != null ? result.size() : 0));
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
            
            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "获取新闻列表失败: 分类=" + finalCategory + ", 错误=" + errorMsg);
                if (callback != null) {
                    callback.onError(errorMsg);
                }
            }
        };
        
        newsService.getNewsList(apiCategory, page, pageSize, loggingCallback);
    }
    
    /**
     * 获取新闻详情
     * @param uniqueKey 新闻唯一标识
     * @param callback 回调接口
     */
    public void getNewsDetail(String uniqueKey, NewsService.ApiCallback<News> callback) {
        Log.d(TAG, "获取新闻详情: uniqueKey=" + uniqueKey);
        
        // 使用包装回调增加日志和特殊处理
        NewsService.ApiCallback<News> loggingCallback = new NewsService.ApiCallback<News>() {
            @Override
            public void onSuccess(News result) {
                Log.d(TAG, "获取新闻详情成功: uniqueKey=" + uniqueKey);
                
                // 特殊处理: 为推荐分类的新闻增加额外信息
                if (result != null && "top".equals(result.getCategory())) {
                    // 确保来源信息完整
                    if (result.getSource() == null || result.getSource().isEmpty()) {
                        if (result.getSpecialTag() != null && !result.getSpecialTag().isEmpty()) {
                            result.setSource(result.getSpecialTag());
                        } else {
                            result.setSource("热门推荐");
                        }
                        Log.d(TAG, "为推荐分类新闻补充来源信息: " + result.getSource());
                    }
                    
                    // 确保内容不为空
                    if (result.getContent() == null || result.getContent().isEmpty()) {
                        Log.d(TAG, "推荐新闻内容为空，尝试获取更详细内容");
                        // 可以在这里添加额外的获取内容的方法
                        // 比如使用 WebView 加载 URL 显示完整内容
                    }
                }
                
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
            
            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "获取新闻详情失败: uniqueKey=" + uniqueKey + ", 错误=" + errorMsg);
                if (callback != null) {
                    callback.onError(errorMsg);
                }
            }
        };
        
        newsService.getNewsDetail(uniqueKey, loggingCallback);
    }
    
    /**
     * 搜索新闻
     * @param keyword 关键词
     * @param callback 回调接口
     */
    public void searchNews(String keyword, NewsService.ApiCallback<List<News>> callback) {
        newsService.searchNews(keyword, callback);
    }
    
    /**
     * 记录浏览历史
     * @param newsUniqueKey 新闻ID
     * @return 历史记录ID，如果用户未登录返回-1
     */
    public long recordNewsHistory(String newsUniqueKey) {
        // 首先检查用户是否已登录
        if (!userService.isLoggedIn()) {
            String phoneNumber = SharedPreferencesUtil.getString(context, "phone_number", "");
            if (phoneNumber.isEmpty()) {
                Log.w(TAG, "用户未登录，无法记录浏览历史");
                return -1;
            }
            
            // 如果SharedPreferences中有手机号但UserService中没有，尝试设置当前用户
            try {
                User user = new User();
                user.setPhoneNumber(phoneNumber);
                userService.setCurrentUser(user);
                Log.d(TAG, "从SharedPreferences恢复用户登录状态: " + phoneNumber);
            } catch (Exception e) {
                Log.e(TAG, "恢复用户登录状态失败", e);
                return -1;
            }
        }
        
        String phoneNumber = userService.getCurrentUser().getPhoneNumber();
        try {
            // 在后台线程执行，但返回一个临时ID
            long result = newsService.recordNewsHistory(phoneNumber, newsUniqueKey);
            Log.d(TAG, "开始记录历史: newsId=" + newsUniqueKey + ", user=" + phoneNumber);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "记录历史异常: " + e.getMessage(), e);
            return -1;
        }
    }
    
    /**
     * 更新阅读时长
     * @param historyId 历史记录ID
     * @param duration 阅读时长（秒）
     */
    public void updateReadDuration(long historyId, int duration) {
        if (historyId > 0) {
            newsService.updateReadDuration(historyId, duration);
        }
    }
    
    /**
     * 获取用户浏览历史
     * @param callback 回调接口
     */
    public void getUserHistory(NewsService.ApiCallback<List<News>> callback) {
        if (userService.isLoggedIn()) {
            String phoneNumber = userService.getCurrentUser().getPhoneNumber();
            newsService.getUserHistory(phoneNumber, callback);
        } else {
            callback.onError("用户未登录");
        }
    }
    
    /**
     * 获取用户浏览历史新闻列表
     * @param callback 回调接口
     */
    public void getHistoryNewsList(NewsService.ApiCallback<List<News>> callback) {
        Log.d(TAG, "获取用户浏览历史新闻列表");
        String phoneNumber = SharedPreferencesUtil.getString(context, "phone_number", "");
        if (phoneNumber.isEmpty()) {
            Log.w(TAG, "用户未登录，无法获取浏览历史");
            if (callback != null) {
                callback.onError("用户未登录，请先登录");
            }
            return;
        }
        
        newsService.getUserHistoryNewsList(phoneNumber, 50, 0, callback);
    }
    
    /**
     * 清空用户浏览历史
     * @param callback 回调接口
     */
    public void clearHistory(NewsService.ApiCallback<Boolean> callback) {
        Log.d(TAG, "清空用户浏览历史");
        String phoneNumber = SharedPreferencesUtil.getString(context, "phone_number", "");
        if (phoneNumber.isEmpty()) {
            Log.w(TAG, "用户未登录，无法清空浏览历史");
            if (callback != null) {
                callback.onError("用户未登录，请先登录");
            }
            return;
        }
        
        newsService.clearUserHistory(phoneNumber);
        if (callback != null) {
            callback.onSuccess(true);
        }
    }
    
    /**
     * 清空用户浏览历史
     * @return 是否成功
     */
    public boolean clearUserHistory() {
        if (userService.isLoggedIn()) {
            String phoneNumber = userService.getCurrentUser().getPhoneNumber();
            newsService.clearUserHistory(phoneNumber);
            return true;
        }
        return false;
    }
    
    /**
     * 检查新闻是否已关注
     * @param newsId 新闻ID
     * @return 是否已关注
     */
    public boolean isNewsFavorited(String newsId) {
        Log.d(TAG, "检查新闻是否已关注: newsId=" + newsId);
        
        // 首先检查用户是否已登录
        if (!userService.isLoggedIn()) {
            String phoneNumber = SharedPreferencesUtil.getString(context, "phone_number", "");
            if (phoneNumber.isEmpty()) {
                Log.w(TAG, "用户未登录，无法检查关注状态");
                return false;
            }
            
            // 如果SharedPreferences中有手机号但UserService中没有，尝试设置当前用户
            try {
                User user = new User();
                user.setPhoneNumber(phoneNumber);
                userService.setCurrentUser(user);
                Log.d(TAG, "从SharedPreferences恢复用户登录状态: " + phoneNumber);
            } catch (Exception e) {
                Log.e(TAG, "恢复用户登录状态失败", e);
                return false;
            }
        }
        
        String phoneNumber = userService.getCurrentUser().getPhoneNumber();
        return newsService.isNewsFavorited(newsId, phoneNumber);
    }
    
    /**
     * 添加新闻关注（带回调）
     * @param news 新闻对象
     * @param callback 回调接口
     */
    public void addNewsFavorite(News news, NewsService.ApiCallback<Boolean> callback) {
        Log.d(TAG, "添加新闻关注: " + news.getTitle());
        
        // 首先检查用户是否已登录
        if (!userService.isLoggedIn()) {
            String phoneNumber = SharedPreferencesUtil.getString(context, "phone_number", "");
            if (phoneNumber.isEmpty()) {
                Log.w(TAG, "用户未登录，无法添加关注");
                callback.onError("请先登录");
                return;
            }
            
            // 如果SharedPreferences中有手机号但UserService中没有，尝试设置当前用户
            try {
                User user = new User();
                user.setPhoneNumber(phoneNumber);
                userService.setCurrentUser(user);
                Log.d(TAG, "从SharedPreferences恢复用户登录状态: " + phoneNumber);
            } catch (Exception e) {
                Log.e(TAG, "恢复用户登录状态失败", e);
                callback.onError("登录状态异常，请重新登录");
                return;
            }
        }
        
        String phoneNumber = userService.getCurrentUser().getPhoneNumber();
        executor.execute(() -> {
            try {
                boolean result = newsService.addNewsFavorite(news, phoneNumber);
                mainHandler.post(() -> {
                    if (result) {
                        callback.onSuccess(true);
                    } else {
                        callback.onError("添加关注失败");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "添加关注异常: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError("操作异常: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 取消新闻关注（带回调）
     * @param newsId 新闻ID
     * @param callback 回调接口
     */
    public void unfavoriteNews(String newsId, NewsService.ApiCallback<Boolean> callback) {
        Log.d(TAG, "取消新闻关注: " + newsId);
        
        // 首先检查用户是否已登录
        if (!userService.isLoggedIn()) {
            String phoneNumber = SharedPreferencesUtil.getString(context, "phone_number", "");
            if (phoneNumber.isEmpty()) {
                Log.w(TAG, "用户未登录，无法取消关注");
                callback.onError("请先登录");
                return;
            }
            
            // 如果SharedPreferences中有手机号但UserService中没有，尝试设置当前用户
            try {
                User user = new User();
                user.setPhoneNumber(phoneNumber);
                userService.setCurrentUser(user);
                Log.d(TAG, "从SharedPreferences恢复用户登录状态: " + phoneNumber);
            } catch (Exception e) {
                Log.e(TAG, "恢复用户登录状态失败", e);
                callback.onError("登录状态异常，请重新登录");
                return;
            }
        }
        
        String phoneNumber = userService.getCurrentUser().getPhoneNumber();
        executor.execute(() -> {
            try {
                boolean result = newsService.removeNewsFavorite(newsId, phoneNumber);
                mainHandler.post(() -> {
                    if (result) {
                        callback.onSuccess(true);
                    } else {
                        callback.onError("取消关注失败");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "取消关注异常: " + e.getMessage(), e);
                mainHandler.post(() -> callback.onError("操作异常: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 获取用户收藏的新闻列表
     * @param callback 回调
     */
    public void getFavoriteNewsList(NewsService.ApiCallback<List<News>> callback) {
        Log.d(TAG, "获取用户收藏的新闻列表");
        String phoneNumber = SharedPreferencesUtil.getString(context, "phone_number", "");
        if (phoneNumber.isEmpty()) {
            Log.w(TAG, "用户未登录，无法获取收藏列表");
            if (callback != null) {
                callback.onError("用户未登录，请先登录");
            }
            return;
        }
        
        newsService.getFavoriteNewsList(phoneNumber, callback);
    }
    
    /**
     * 获取所有新闻分类（包括已禁用的）
     * @param callback 回调接口
     */
    public void getAllNewsCategories(NewsService.ApiCallback<List<NewsCategory>> callback) {
        Log.d(TAG, "获取所有新闻分类");
        newsService.getAllNewsCategories(callback);
    }
    
    /**
     * 更新新闻分类设置
     * @param categories 更新后的分类列表
     * @param callback 回调接口
     */
    public void updateNewsCategories(List<NewsCategory> categories, NewsService.ApiCallback<Boolean> callback) {
        Log.d(TAG, "更新新闻分类设置: " + categories.size() + "个分类");
        newsService.updateNewsCategories(categories, callback);
    }
} 