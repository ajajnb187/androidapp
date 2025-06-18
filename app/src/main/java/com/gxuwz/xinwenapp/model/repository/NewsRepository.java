package com.gxuwz.xinwenapp.model.repository;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.gxuwz.xinwenapp.model.AppDatabase;
import com.gxuwz.xinwenapp.model.dao.NewsCategoryDao;
import com.gxuwz.xinwenapp.model.dao.NewsDao;
import com.gxuwz.xinwenapp.model.dao.NewsFavoriteDao;
import com.gxuwz.xinwenapp.model.dao.NewsHistoryDao;
import com.gxuwz.xinwenapp.model.entity.News;
import com.gxuwz.xinwenapp.model.entity.NewsCategory;
import com.gxuwz.xinwenapp.model.entity.NewsFavorite;
import com.gxuwz.xinwenapp.model.entity.NewsHistory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 新闻数据仓库类
 * 
 * 作用：
 * 1. 协调新闻相关的多个DAO
 * 2. 处理新闻数据的缓存和网络请求
 * 3. 提供新闻相关的数据操作方法
 * 
 * 调用者：
 * 1. NewsService：新闻业务逻辑服务类
 * 2. NewsController：新闻控制器类
 */
public class NewsRepository {
    private static final String TAG = "NewsRepository";
    private final NewsDao newsDao;
    private final NewsCategoryDao categoryDao;
    private final NewsHistoryDao historyDao;
    private final NewsFavoriteDao favoriteDao;
    private final Executor executor;
    
    // 新闻缓存过期时间：24小时
    private static final long NEWS_CACHE_EXPIRE_TIME = 24 * 60 * 60 * 1000;
    
    /**
     * 构造函数
     * @param context 应用上下文
     */
    public NewsRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        newsDao = database.newsDao();
        categoryDao = database.newsCategoryDao();
        historyDao = database.newsHistoryDao();
        favoriteDao = database.newsFavoriteDao();
        executor = Executors.newFixedThreadPool(4);
    }
    
    /**
     * 初始化默认新闻分类
     */
    public void initDefaultCategories() {
        executor.execute(() -> {
            List<NewsCategory> categories = new ArrayList<>();
            categories.add(new NewsCategory("top", "推荐", 0));
            categories.add(new NewsCategory("guonei", "国内", 1));
            categories.add(new NewsCategory("guoji", "国际", 2));
            categories.add(new NewsCategory("yule", "娱乐", 3));
            categories.add(new NewsCategory("tiyu", "体育", 4));
            categories.add(new NewsCategory("junshi", "军事", 5));
            categories.add(new NewsCategory("keji", "科技", 6));
            categories.add(new NewsCategory("caijing", "财经", 7));
            categories.add(new NewsCategory("youxi", "游戏", 8));
            categories.add(new NewsCategory("qiche", "汽车", 9));
            categories.add(new NewsCategory("jiankang", "健康", 10));
            categoryDao.insertAll(categories);
        });
    }
    
    /**
     * 获取所有启用的新闻分类
     * @return 新闻分类列表
     */
    public List<NewsCategory> getEnabledCategories() {
        return categoryDao.getEnabledCategories();
    }
    
    /**
     * 获取所有新闻分类
     * @return 新闻分类列表
     */
    public List<NewsCategory> getAllCategories() {
        return categoryDao.getAllCategories();
    }
    
    /**
     * 保存新闻列表到本地缓存
     * @param newsList 新闻列表
     */
    public void saveNewsToCache(List<News> newsList) {
        if (newsList != null && !newsList.isEmpty()) {
            executor.execute(() -> {
                try {
                    // 处理可能的空uniqueKey字段
                    List<News> validNewsList = new ArrayList<>();
                    for (News news : newsList) {
                        // 检查uniqueKey是否为null或空
                        if (news.getUniqueKey() == null || news.getUniqueKey().trim().isEmpty()) {
                            // 生成唯一ID作为uniqueKey
                            String uniqueId = UUID.randomUUID().toString();
                            news.setUniqueKey(uniqueId);
                            Log.d(TAG, "Generated uniqueKey for news: " + news.getTitle());
                        }
                        
                        // 检查是否已存在相同标题和分类的新闻
                        int existsCount = newsDao.checkNewsExists(news.getTitle(), news.getCategory());
                        if (existsCount == 0) {
                            // 设置缓存时间
                            news.setCacheTime(System.currentTimeMillis());
                            validNewsList.add(news);
                            Log.d(TAG, "Adding new news to cache: " + news.getTitle());
                        } else {
                            Log.d(TAG, "Skipping duplicate news: " + news.getTitle());
                        }
                    }
                    
                    // 插入有效的新闻列表
                    if (!validNewsList.isEmpty()) {
                        newsDao.insertAll(validNewsList);
                        Log.d(TAG, "Saved " + validNewsList.size() + " news items to cache");
                    } else {
                        Log.d(TAG, "No new news items to save to cache");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error saving news to cache", e);
                }
            });
        }
    }
    
    /**
     * 通过分类获取新闻列表（从本地缓存）
     * @param category 新闻分类
     * @param limit 数量限制
     * @param offset 起始位置
     * @return 新闻列表
     */
    public List<News> getNewsByCategory(String category, int limit, int offset) {
        return newsDao.getNewsByCategory(category, limit, offset);
    }
    
    /**
     * 通过分类获取不重复的新闻列表（从本地缓存）
     * @param category 新闻分类
     * @param limit 数量限制
     * @param offset 起始位置
     * @return 无重复标题的新闻列表
     */
    public List<News> getNewsByCategoryNoDuplicates(String category, int limit, int offset) {
        return newsDao.getNewsByCategoryNoDuplicates(category, limit, offset);
    }
    
    /**
     * 获取新闻详情
     * @param uniqueKey 新闻ID
     * @return 新闻对象
     */
    public News getNewsDetail(String uniqueKey) {
        return newsDao.getNewsById(uniqueKey);
    }
    
    /**
     * 更新新闻内容
     * @param uniqueKey 新闻ID
     * @param content 新闻内容
     */
    public void updateNewsContent(String uniqueKey, String content) {
        executor.execute(() -> newsDao.updateContent(uniqueKey, content));
    }
    
    /**
     * 搜索新闻
     * @param keyword 关键词
     * @return 匹配的新闻列表
     */
    public List<News> searchNews(String keyword) {
        return newsDao.searchNewsByKeyword(keyword);
    }
    
    /**
     * 清理过期的新闻缓存
     */
    public void clearExpiredNewsCache() {
        long expireTime = System.currentTimeMillis() - NEWS_CACHE_EXPIRE_TIME;
        executor.execute(() -> newsDao.deleteExpiredNews(expireTime));
    }
    
    /**
     * 删除指定分类的所有新闻
     * @param category 新闻分类
     */
    public void deleteNewsByCategory(String category) {
        executor.execute(() -> {
            try {
                int count = newsDao.deleteNewsByCategory(category);
                Log.d(TAG, "已删除分类 " + category + " 的 " + count + " 条新闻");
            } catch (Exception e) {
                Log.e(TAG, "删除分类 " + category + " 的新闻失败: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 记录用户浏览历史
     * @param phoneNumber 用户手机号
     * @param newsUniqueKey 新闻ID
     * @return 记录ID
     */
    public long recordNewsHistory(String phoneNumber, String newsUniqueKey) {
        try {
            // 在后台线程中执行数据库操作
            executor.execute(() -> {
                try {
                    // 先检查是否已存在该新闻的浏览记录
                    NewsHistory existingHistory = historyDao.getHistoryByUserAndNews(phoneNumber, newsUniqueKey);
                    
                    if (existingHistory != null) {
                        // 如果已存在，则更新浏览时间
                        existingHistory.setReadTime(System.currentTimeMillis());
                        historyDao.update(existingHistory);
                        Log.d(TAG, "更新浏览历史时间: newsId=" + newsUniqueKey + ", user=" + phoneNumber + ", historyId=" + existingHistory.getId());
                    } else {
                        // 如果不存在，则创建新记录
                        NewsHistory history = new NewsHistory(phoneNumber, newsUniqueKey);
                        history.setReadTime(System.currentTimeMillis());
                        long id = historyDao.insert(history);
                        Log.d(TAG, "记录浏览历史成功: newsId=" + newsUniqueKey + ", user=" + phoneNumber + ", historyId=" + id);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "记录浏览历史失败: " + e.getMessage(), e);
                }
            });
            
            // 由于操作是异步的，我们无法立即返回实际ID
            // 返回1表示操作已开始
            return 1;
        } catch (Exception e) {
            Log.e(TAG, "记录浏览历史异常: " + e.getMessage(), e);
            return -1;
        }
    }
    
    /**
     * 更新阅读时长
     * @param historyId 历史记录ID
     * @param duration 阅读时长（秒）
     */
    public void updateReadDuration(long historyId, int duration) {
        executor.execute(() -> historyDao.updateReadDuration(historyId, duration));
    }
    
    /**
     * 获取用户浏览历史
     * @param phoneNumber 用户手机号
     * @param limit 数量限制
     * @param offset 起始位置
     * @return 浏览历史列表
     */
    public List<NewsHistory> getUserHistory(String phoneNumber, int limit, int offset) {
        return historyDao.getHistoryByUserPaged(phoneNumber, limit, offset);
    }
    
    /**
     * 清空用户浏览历史
     * @param phoneNumber 用户手机号
     */
    public void clearUserHistory(String phoneNumber) {
        executor.execute(() -> historyDao.clearHistoryByUser(phoneNumber));
    }
    
    /**
     * 添加新闻关注
     * @param phoneNumber 用户手机号
     * @param newsId 新闻ID
     * @return 是否成功
     */
    public boolean addFavorite(String phoneNumber, String newsId) {
        try {
            final NewsFavorite favorite = new NewsFavorite();
            favorite.setNewsId(newsId);
            favorite.setPhoneNumber(phoneNumber);
            favorite.setFavoriteTime(System.currentTimeMillis());
            
            // 在后台线程中执行数据库操作
            executor.execute(() -> {
                try {
                    favoriteDao.insert(favorite);
                    Log.d(TAG, "添加关注成功: newsId=" + newsId + ", user=" + phoneNumber);
                } catch (Exception e) {
                    Log.e(TAG, "添加关注失败(后台线程): " + e.getMessage(), e);
                }
            });
            return true;
        } catch (Exception e) {
            Log.e(TAG, "添加关注失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 取消新闻关注
     * @param phoneNumber 用户手机号
     * @param newsId 新闻ID
     * @return 删除的行数
     */
    public int removeFavorite(String phoneNumber, String newsId) {
        // 创建一个计数器来跟踪删除的行数
        final int[] rowsDeleted = {0};
        
        try {
            // 在后台线程中执行数据库操作
            executor.execute(() -> {
                try {
                    int result = favoriteDao.deleteNewsFavorite(newsId, phoneNumber);
                    rowsDeleted[0] = result;
                    Log.d(TAG, "取消关注成功: newsId=" + newsId + ", user=" + phoneNumber + ", 结果=" + result);
                } catch (Exception e) {
                    Log.e(TAG, "取消关注失败(后台线程): " + e.getMessage(), e);
                }
            });
            
            // 由于操作是异步的，我们无法立即返回结果
            // 这里假设操作会成功，返回1表示预期会删除一行
            return 1;
        } catch (Exception e) {
            Log.e(TAG, "取消关注失败: " + e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 检查新闻是否已关注
     * @param newsId 新闻ID
     * @param phoneNumber 用户手机号
     * @return 关注对象，如果不存在则返回null
     */
    public NewsFavorite getNewsFavorite(String newsId, String phoneNumber) {
        return favoriteDao.getNewsFavorite(newsId, phoneNumber);
    }
    
    /**
     * 获取用户关注的新闻列表
     * @param phoneNumber 用户手机号
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 新闻列表
     */
    public List<News> getUserFavorites(String phoneNumber, int limit, int offset) {
        List<NewsFavorite> favorites = favoriteDao.getUserFavorites(phoneNumber, limit, offset);
        List<News> newsList = new ArrayList<>();
        
        for (NewsFavorite favorite : favorites) {
            News news = newsDao.getNewsById(favorite.getNewsId());
            if (news != null) {
                // 设置特殊标签，表示这是关注的新闻
                news.setSpecialTag("关注");
                newsList.add(news);
            }
        }
        
        return newsList;
    }
    
    /**
     * 更新分类信息
     * @param category 分类对象
     */
    public void updateCategory(NewsCategory category) {
        executor.execute(() -> {
            try {
                categoryDao.update(category);
                Log.d(TAG, "更新分类成功: " + category.getName() + ", 启用状态: " + category.isEnabled() + ", 排序: " + category.getSortOrder());
            } catch (Exception e) {
                Log.e(TAG, "更新分类失败: " + category.getName(), e);
            }
        });
    }
} 