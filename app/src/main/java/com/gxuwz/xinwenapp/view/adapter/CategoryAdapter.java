package com.gxuwz.xinwenapp.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gxuwz.xinwenapp.R;
import com.gxuwz.xinwenapp.model.entity.NewsCategory;

import java.util.List;

/**
 * 新闻分类适配器
 * 
 * 作用：
 * 1. 显示新闻分类标签
 * 2. 处理分类标签的点击事件
 * 3. 管理选中状态
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    
    private final List<NewsCategory> categories;
    private final Context context;
    private int selectedPosition = 0;
    private OnCategoryClickListener listener;
    
    public interface OnCategoryClickListener {
        void onCategoryClick(NewsCategory category, int position);
    }
    
    public CategoryAdapter(Context context, List<NewsCategory> categories) {
        this.context = context;
        this.categories = categories;
    }
    
    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_tab, parent, false);
        return new CategoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        NewsCategory category = categories.get(position);
        holder.tvCategory.setText(category.getName());
        
        // 设置选中状态
        boolean isSelected = position == selectedPosition;
        holder.tvCategory.setSelected(isSelected);
        
        // 根据选中状态设置文本颜色
        if (isSelected) {
            holder.tvCategory.setTextColor(ContextCompat.getColor(context, R.color.category_tab_text_selected));
        } else {
            holder.tvCategory.setTextColor(ContextCompat.getColor(context, R.color.category_tab_text));
        }
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position != selectedPosition) {
                int previousSelected = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);
                listener.onCategoryClick(category, position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }
    
    public void setSelectedPosition(int position) {
        if (position >= 0 && position < getItemCount() && position != selectedPosition) {
            int previousSelected = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
        }
    }
    
    public int getSelectedPosition() {
        return selectedPosition;
    }
    
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory;
        
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = (TextView) itemView;
        }
    }
}