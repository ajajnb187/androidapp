package com.gxuwz.xinwenapp.view.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gxuwz.xinwenapp.R;
import com.gxuwz.xinwenapp.controller.NewsController;
import com.gxuwz.xinwenapp.model.entity.NewsCategory;
import com.gxuwz.xinwenapp.model.service.NewsService;
import com.gxuwz.xinwenapp.view.adapter.CategoryManagementAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 新闻分类管理Activity
 * 
 * 作用：
 * 1. 显示所有新闻分类
 * 2. 支持拖拽排序
 * 3. 支持启用/禁用分类
 * 4. 保存分类设置
 */
public class CategoryManagementActivity extends AppCompatActivity implements CategoryManagementAdapter.OnCategoryChangeListener {
    private static final String TAG = "CategoryManagement";
    
    private RecyclerView rvCategories;
    private FloatingActionButton fabSave;
    private Toolbar toolbar;
    
    private NewsController newsController;
    private CategoryManagementAdapter adapter;
    private List<NewsCategory> categories;
    private ItemTouchHelper itemTouchHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);
        
        newsController = new NewsController(this);
        categories = new ArrayList<>();
        
        initViews();
        loadCategories();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvCategories = findViewById(R.id.rv_categories);
        fabSave = findViewById(R.id.fab_save);
        
        // 设置Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // 设置RecyclerView
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryManagementAdapter(this, categories);
        adapter.setOnCategoryChangeListener(this);
        rvCategories.setAdapter(adapter);
        
        // 设置拖拽排序
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvCategories);
        
        adapter.setOnItemDragListener(holder -> itemTouchHelper.startDrag(holder));
        
        // 设置保存按钮点击事件
        fabSave.setOnClickListener(v -> saveCategories());
    }
    
    private void loadCategories() {
        Log.d(TAG, "加载所有新闻分类");
        newsController.getAllNewsCategories(new NewsService.ApiCallback<List<NewsCategory>>() {
            @Override
            public void onSuccess(List<NewsCategory> result) {
                Log.d(TAG, "加载新闻分类成功: " + result.size() + "个分类");
                categories.clear();
                categories.addAll(result);
                adapter.notifyDataSetChanged();
            }
            
            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "加载新闻分类失败: " + errorMsg);
                Toast.makeText(CategoryManagementActivity.this, "加载分类失败: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void saveCategories() {
        Log.d(TAG, "保存新闻分类设置");
        List<NewsCategory> updatedCategories = adapter.getCategories();
        
        newsController.updateNewsCategories(updatedCategories, new NewsService.ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    Log.d(TAG, "保存新闻分类成功");
                    Toast.makeText(CategoryManagementActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Log.e(TAG, "保存新闻分类失败");
                    Toast.makeText(CategoryManagementActivity.this, "保存失败，请重试", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "保存新闻分类错误: " + errorMsg);
                Toast.makeText(CategoryManagementActivity.this, "保存失败: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onCategoryEnabledChanged(NewsCategory category, boolean isEnabled) {
        Log.d(TAG, "分类状态变更: " + category.getName() + " -> " + (isEnabled ? "启用" : "禁用"));
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * ItemTouchHelper回调类，处理拖拽排序
     */
    private static class ItemTouchHelperCallback extends ItemTouchHelper.Callback {
        private final CategoryManagementAdapter adapter;
        
        public ItemTouchHelperCallback(CategoryManagementAdapter adapter) {
            this.adapter = adapter;
        }
        
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            return makeMovementFlags(dragFlags, 0);
        }
        
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = source.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            return adapter.moveItem(fromPosition, toPosition);
        }
        
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            // 不支持滑动删除
        }
        
        @Override
        public boolean isLongPressDragEnabled() {
            return false; // 禁用长按拖拽，使用拖拽手柄
        }
    }
} 