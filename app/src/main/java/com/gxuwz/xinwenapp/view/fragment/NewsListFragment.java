package com.gxuwz.xinwenapp.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gxuwz.xinwenapp.R;
import com.gxuwz.xinwenapp.controller.NewsController;
import com.gxuwz.xinwenapp.model.entity.News;
import com.gxuwz.xinwenapp.model.entity.NewsCategory;
import com.gxuwz.xinwenapp.model.service.NewsService;
import com.gxuwz.xinwenapp.view.activity.CategoryManagementActivity;
import com.gxuwz.xinwenapp.view.activity.NewsDetailActivity;
import com.gxuwz.xinwenapp.view.adapter.CategoryAdapter;
import com.gxuwz.xinwenapp.view.adapter.NewsAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 新闻列表Fragment
 * 
 * 作用：
 * 1. 显示新闻列表
 * 2. 支持下拉刷新和上拉加载更多
 * 3. 切换新闻分类
 * 
 * 调用者：
 * MainActivity：作为主界面的一个页面
 */
public class NewsListFragment extends Fragment implements NewsAdapter.OnItemClickListener, CategoryAdapter.OnCategoryClickListener {
    private static final String TAG = "NewsListFragment";
    private static final int REQUEST_CATEGORY_MANAGEMENT = 1001;
    
    private RecyclerView rvNews;
    private RecyclerView rvCategories;
    private SwipeRefreshLayout refreshLayout;
    private FloatingActionButton fabCategoryManage;
    
    private NewsAdapter newsAdapter;
    private CategoryAdapter categoryAdapter;
    private NewsController newsController;
    
    private List<News> newsList;
    private List<NewsCategory> categoryList;
    
    private String currentCategory = "top"; // 默认分类为推荐
    private int currentPage = 1;
    private static final int PAGE_SIZE = 20;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private boolean isViewCreated = false; // 标记视图是否已创建
    private boolean pendingRefresh = false; // 标记是否有待处理的刷新请求
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: 创建Fragment视图");
        View view = inflater.inflate(R.layout.fragment_news_list, container, false);
        
        newsController = new NewsController(requireContext());
        newsList = new ArrayList<>();
        categoryList = new ArrayList<>();
        
        initViews(view);
        isViewCreated = true;
        
        // 加载分类
        loadCategories();
        
        // 处理待处理的刷新请求
        if (pendingRefresh) {
            Log.d(TAG, "onCreateView: 处理待处理的刷新请求");
            pendingRefresh = false;
            loadNews(true);
        }
        
        return view;
    }
    
    private void initViews(View view) {
        Log.d(TAG, "initViews: 初始化视图组件");
        rvNews = view.findViewById(R.id.rv_news);
        rvCategories = view.findViewById(R.id.rv_categories);
        refreshLayout = view.findViewById(R.id.refresh_layout);
        fabCategoryManage = view.findViewById(R.id.fab_category_manage);
        
        // 设置新闻列表
        LinearLayoutManager newsLayoutManager = new LinearLayoutManager(requireContext());
        rvNews.setLayoutManager(newsLayoutManager);
        
        // 设置新闻适配器
        newsAdapter = new NewsAdapter(requireContext(), newsList);
        newsAdapter.setOnItemClickListener(this);
        rvNews.setAdapter(newsAdapter);
        
        // 设置分类列表
        LinearLayoutManager categoryLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        rvCategories.setLayoutManager(categoryLayoutManager);
        
        // 设置分类适配器
        categoryAdapter = new CategoryAdapter(requireContext(), categoryList);
        categoryAdapter.setOnCategoryClickListener(this);
        rvCategories.setAdapter(categoryAdapter);
        
        // 设置下拉刷新监听
        refreshLayout.setOnRefreshListener(this::refreshNews);
        
        // 设置分类管理按钮点击事件
        fabCategoryManage.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CategoryManagementActivity.class);
            startActivityForResult(intent, REQUEST_CATEGORY_MANAGEMENT);
        });
        
        // 设置上拉加载更多监听
        rvNews.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                if (dy > 0) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    
                    if (!isLoading && hasMoreData && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5) {
                        // 加载更多
                        loadMoreNews();
                    }
                }
            }
        });
    }
    
    /**
     * 加载分类列表
     */
    private void loadCategories() {
        Log.d(TAG, "loadCategories: 加载新闻分类");
        newsController.getNewsCategories(new NewsService.ApiCallback<List<NewsCategory>>() {
            @Override
            public void onSuccess(List<NewsCategory> result) {
                if (result != null && !result.isEmpty()) {
                    Log.d(TAG, "加载新闻分类成功: " + result.size() + "个分类");
                    categoryList.clear();
                    categoryList.addAll(result);
                    categoryAdapter.notifyDataSetChanged();
                    
                    // 默认选中第一个分类
                    if (!categoryList.isEmpty()) {
                        NewsCategory firstCategory = categoryList.get(0);
                        currentCategory = firstCategory.getType();
                        Log.d(TAG, "默认选中第一个分类: " + firstCategory.getName() + " (type=" + firstCategory.getType() + ")");
                        refreshNews();
                    }
                } else {
                    Log.e(TAG, "加载新闻分类成功，但列表为空");
                    Toast.makeText(requireContext(), "没有可用的新闻分类", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "加载新闻分类失败: " + errorMsg);
                Toast.makeText(requireContext(), "加载分类失败: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onCategoryClick(NewsCategory category, int position) {
        Log.d(TAG, "点击分类: " + category.getName() + " (type=" + category.getType() + ")");
        switchCategory(category.getType());
    }
    
    /**
     * 切换新闻分类
     * @param category 新闻分类类型
     */
    public void switchCategory(String category) {
        Log.d(TAG, "切换新闻分类: 从 " + currentCategory + " 到 " + category);
        if (category.equals(currentCategory)) {
            // 如果是同一分类，刷新数据
            refreshNews();
        } else {
            // 切换分类
            currentCategory = category;
            currentPage = 1;
            hasMoreData = true;
            
            // 只有在视图已创建的情况下才加载数据
            if (isViewCreated) {
                // 清空当前列表并显示加载进度
                if (newsList != null) {
                    newsList.clear();
                    if (newsAdapter != null) {
                        newsAdapter.notifyDataSetChanged();
                    }
                }
                
                // 显示加载进度
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(true);
                }
                
                loadNews(true);
            } else {
                Log.d(TAG, "switchCategory: 视图未创建，标记待刷新");
                pendingRefresh = true;
            }
        }
    }
    
    /**
     * 刷新新闻列表
     */
    public void refreshNews() {
        Log.d(TAG, "刷新新闻列表: 分类=" + currentCategory);
        currentPage = 1;
        hasMoreData = true;
        
        // 只有在视图已创建的情况下才加载数据
        if (isViewCreated) {
            loadNews(true);
        } else {
            Log.d(TAG, "refreshNews: 视图未创建，标记待刷新");
            pendingRefresh = true;
        }
    }
    
    /**
     * 加载更多新闻
     */
    private void loadMoreNews() {
        if (!isLoading && hasMoreData) {
            Log.d(TAG, "加载更多新闻: 分类=" + currentCategory + ", 页码=" + (currentPage + 1));
            currentPage++;
            loadNews(false);
        }
    }
    
    /**
     * 加载新闻数据
     * @param isRefresh 是否为刷新操作
     */
    private void loadNews(boolean isRefresh) {
        if (isLoading) return;
        
        // 检查Fragment是否处于活动状态并且视图已创建
        if (!isAdded() || !isViewCreated) {
            Log.d(TAG, "loadNews: Fragment未添加或视图未创建，取消加载");
            return;
        }
        
        isLoading = true;
        Log.d(TAG, "开始加载新闻: 分类=" + currentCategory + ", 页码=" + currentPage + ", 刷新=" + isRefresh);
        
        if (isRefresh && refreshLayout != null) {
            refreshLayout.setRefreshing(true);
        }
        
        // 对于推荐类型，指定type为top
        String category = currentCategory;
        if ("推荐".equals(category)) {
            category = "top";
            Log.d(TAG, "将'推荐'分类映射为API参数'top'");
        }
        
        final String finalCategory = category;
        newsController.getNewsList(category, currentPage, PAGE_SIZE, new NewsService.ApiCallback<List<News>>() {
            @Override
            public void onSuccess(List<News> result) {
                // 再次检查Fragment是否处于活动状态
                if (!isAdded() || !isViewCreated) {
                    Log.d(TAG, "请求成功回调时Fragment未添加或视图未创建，忽略结果");
                    isLoading = false;
                    return;
                }
                
                isLoading = false;
                Log.d(TAG, "加载新闻成功: 分类=" + finalCategory + ", 返回数量=" + (result != null ? result.size() : 0));
                
                if (isRefresh && refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                    newsList.clear();
                }
                
                if (result != null && !result.isEmpty()) {
                    // 确保所有新闻都有分类信息
                    for (News news : result) {
                        if (news.getCategory() == null || news.getCategory().isEmpty()) {
                            news.setCategory(finalCategory);
                            Log.d(TAG, "设置新闻分类: " + news.getTitle() + " -> " + finalCategory);
                        }
                    }
                    
                    // 添加新闻到列表
                    newsList.addAll(result);
                    newsAdapter.notifyDataSetChanged();
                    Log.d(TAG, "新闻列表更新完成，当前总数: " + newsList.size());
                } else {
                    hasMoreData = false;
                    if (currentPage > 1) {
                        currentPage--;
                    }
                    Log.d(TAG, "没有更多新闻数据");
                    
                    // 如果是刷新操作且结果为空，显示提示
                    if (isRefresh && (newsList == null || newsList.isEmpty())) {
                        Toast.makeText(requireContext(), "暂无新闻数据，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            
            @Override
            public void onError(String errorMsg) {
                // 再次检查Fragment是否处于活动状态
                if (!isAdded() || !isViewCreated) {
                    Log.d(TAG, "请求失败回调时Fragment未添加或视图未创建，忽略错误");
                    isLoading = false;
                    return;
                }
                
                isLoading = false;
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }
                
                Log.e(TAG, "加载新闻失败: " + errorMsg);
                
                // 显示友好的错误提示
                String friendlyErrorMsg;
                if (errorMsg.contains("API请求次数已达上限")) {
                    friendlyErrorMsg = "今日API请求次数已达上限，请明天再试";
                } else if (errorMsg.contains("Failed to connect")) {
                    friendlyErrorMsg = "网络连接失败，请检查网络设置";
                } else if (errorMsg.contains("timeout")) {
                    friendlyErrorMsg = "网络请求超时，请稍后重试";
                } else {
                    // 其他错误，保留原始错误信息但限制长度
                    if (errorMsg.length() > 50) {
                        friendlyErrorMsg = "加载失败: " + errorMsg.substring(0, 50) + "...";
                    } else {
                        friendlyErrorMsg = "加载失败: " + errorMsg;
                    }
                }
                
                Toast.makeText(requireContext(), friendlyErrorMsg, Toast.LENGTH_SHORT).show();
                
                // 如果是加载更多失败，恢复页码
                if (currentPage > 1) {
                    currentPage--;
                }
            }
        });
    }
    
    @Override
    public void onItemClick(News news) {
        Log.d(TAG, "点击新闻: " + news.getTitle());
        Intent intent = new Intent(requireContext(), NewsDetailActivity.class);
        intent.putExtra("news_id", news.getUniqueKey());
        startActivity(intent);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CATEGORY_MANAGEMENT && resultCode == requireActivity().RESULT_OK) {
            // 分类管理返回，重新加载分类
            loadCategories();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewCreated = false;
        Log.d(TAG, "onDestroyView");
    }
} 