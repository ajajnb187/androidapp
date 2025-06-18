package com.gxuwz.xinwenapp.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gxuwz.xinwenapp.R;
import com.gxuwz.xinwenapp.model.entity.NewsCategory;

import java.util.Collections;
import java.util.List;

/**
 * 新闻分类管理适配器
 * 
 * 作用：
 * 1. 显示所有新闻分类及其状态
 * 2. 支持拖拽排序
 * 3. 支持启用/禁用分类
 */
public class CategoryManagementAdapter extends RecyclerView.Adapter<CategoryManagementAdapter.CategoryViewHolder> {
    
    private final List<NewsCategory> categories;
    private final Context context;
    private OnItemDragListener dragListener;
    private OnCategoryChangeListener changeListener;
    
    public interface OnItemDragListener {
        void onStartDrag(CategoryViewHolder holder);
    }
    
    public interface OnCategoryChangeListener {
        void onCategoryEnabledChanged(NewsCategory category, boolean isEnabled);
    }
    
    public CategoryManagementAdapter(Context context, List<NewsCategory> categories) {
        this.context = context;
        this.categories = categories;
    }
    
    public void setOnItemDragListener(OnItemDragListener listener) {
        this.dragListener = listener;
    }
    
    public void setOnCategoryChangeListener(OnCategoryChangeListener listener) {
        this.changeListener = listener;
    }
    
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        NewsCategory category = categories.get(position);
        holder.tvCategoryName.setText(category.getName());
        holder.switchEnabled.setChecked(category.isEnabled());
        
        // 设置拖动手柄的触摸事件
        holder.ivDragHandle.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && dragListener != null) {
                dragListener.onStartDrag(holder);
            }
            return false;
        });
        
        // 设置开关的状态变化监听
        holder.switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (changeListener != null) {
                category.setEnabled(isChecked);
                changeListener.onCategoryEnabledChanged(category, isChecked);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }
    
    /**
     * 移动分类位置
     * @param fromPosition 起始位置
     * @param toPosition 目标位置
     * @return 是否移动成功
     */
    public boolean moveItem(int fromPosition, int toPosition) {
        if (fromPosition < 0 || fromPosition >= categories.size() ||
                toPosition < 0 || toPosition >= categories.size()) {
            return false;
        }
        
        // 交换位置
        Collections.swap(categories, fromPosition, toPosition);
        
        // 更新排序序号
        for (int i = 0; i < categories.size(); i++) {
            categories.get(i).setSortOrder(i);
        }
        
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }
    
    /**
     * 获取当前分类列表
     * @return 分类列表
     */
    public List<NewsCategory> getCategories() {
        return categories;
    }
    
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        ImageView ivDragHandle;
        SwitchCompat switchEnabled;
        
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            ivDragHandle = itemView.findViewById(R.id.iv_drag_handle);
            switchEnabled = itemView.findViewById(R.id.switch_enabled);
        }
    }
} 