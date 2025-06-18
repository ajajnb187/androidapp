package com.gxuwz.xinwenapp.view.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gxuwz.xinwenapp.R;
import com.gxuwz.xinwenapp.model.entity.News;
import com.gxuwz.xinwenapp.util.DateUtil;

import java.util.List;

/**
 * 新闻列表适配器
 * 
 * 作用：
 * 1. 为RecyclerView提供新闻列表项的视图
 * 2. 管理新闻列表数据
 * 3. 处理新闻列表项的点击事件
 * 
 * 调用者：
 * NewsListFragment：新闻列表Fragment中使用
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private static final String TAG = "NewsAdapter";
    private Context context;
    private List<News> newsList;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;
    
    /**
     * 构造函数
     * @param context 上下文
     * @param newsList 新闻列表数据
     */
    public NewsAdapter(Context context, List<News> newsList) {
        this.context = context;
        this.newsList = newsList;
    }
    
    /**
     * 设置列表项点击监听器
     * @param listener 监听器接口
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    /**
     * 设置列表项长按监听器
     * @param listener 长按监听器接口
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }
    
    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = newsList.get(position);
        Log.d(TAG, "绑定新闻项: position=" + position + ", title=" + news.getTitle() + ", category=" + news.getCategory());
        
        // 设置新闻标题
        String title = news.getTitle();
        if (title == null || title.isEmpty()) {
            title = "无标题";
            Log.w(TAG, "新闻标题为空: position=" + position);
        }
        holder.tvTitle.setText(title);
        
        // 设置新闻来源和时间
        String source = news.getSource();
        if (source == null || source.isEmpty()) {
            source = news.getAuthor();
            if (source == null || source.isEmpty()) {
                // 使用分类作为来源
                String category = news.getCategory();
                if ("top".equals(category)) {
                    if (news.getSpecialTag() != null && !news.getSpecialTag().isEmpty()) {
                        source = news.getSpecialTag(); // 使用特殊标签作为来源
                    } else {
                        source = "热门推荐";
                    }
                } else if ("guonei".equals(category)) {
                    source = "国内新闻";
                } else if ("guoji".equals(category)) {
                    source = "国际新闻";
                } else if ("yule".equals(category)) {
                    source = "娱乐新闻";
                } else if ("tiyu".equals(category)) {
                    source = "体育新闻";
                } else {
                    source = "未知来源";
                }
                Log.w(TAG, "新闻来源为空，使用分类: position=" + position + ", category=" + category);
            }
        }
        
        // 如果是推荐分类，但有特殊标签，在来源前添加标签
        if ("top".equals(news.getCategory()) && news.getSpecialTag() != null && !news.getSpecialTag().isEmpty() 
                && !news.getSpecialTag().equals(source)) {
            source = news.getSpecialTag() + " · " + source;
            Log.d(TAG, "为推荐新闻添加特殊标签: " + news.getSpecialTag());
        }
        
        String date = "";
        if (news.getPublishTime() != null && !news.getPublishTime().isEmpty()) {
            date = DateUtil.formatDate(news.getPublishTime());
        } else if (news.getDate() != null && !news.getDate().isEmpty()) {
            date = DateUtil.formatDate(news.getDate());
        } else {
            date = "未知时间";
            Log.w(TAG, "新闻时间为空: position=" + position);
        }
        
        String sourceAndDate = source + " · " + date;
        holder.tvSourceAndDate.setText(sourceAndDate);
        Log.d(TAG, "新闻来源和时间: " + sourceAndDate);
        
        // 加载新闻图片
        if (news.getPicUrl() != null && !news.getPicUrl().isEmpty()) {
            Log.d(TAG, "加载新闻图片: " + news.getPicUrl());
            Glide.with(context)
                    .load(news.getPicUrl())
                    .placeholder(R.drawable.ic_news)
                    .error(R.drawable.ic_news)
                    .timeout(3000)
                    .fallback(R.drawable.ic_news)
                    .into(holder.ivPicture);
            holder.ivPicture.setVisibility(View.VISIBLE);
        } else {
            // 如果没有图片，隐藏ImageView
            holder.ivPicture.setVisibility(View.GONE);
            Log.d(TAG, "新闻无图片");
        }
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                Log.d(TAG, "新闻项点击: position=" + position + ", title=" + news.getTitle());
                listener.onItemClick(news);
            }
        });
        
        // 设置长按事件
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                Log.d(TAG, "新闻项长按: position=" + position + ", title=" + news.getTitle());
                return longClickListener.onItemLongClick(position, news);
            }
            return false;
        });
    }
    
    @Override
    public int getItemCount() {
        return newsList == null ? 0 : newsList.size();
    }
    
    /**
     * 新闻ViewHolder
     */
    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvSourceAndDate;
        ImageView ivPicture;
        
        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSourceAndDate = itemView.findViewById(R.id.tv_source_and_date);
            ivPicture = itemView.findViewById(R.id.iv_picture);
        }
    }
    
    /**
     * 列表项点击监听器接口
     */
    public interface OnItemClickListener {
        void onItemClick(News news);
    }
    
    /**
     * 列表项长按监听器接口
     */
    public interface OnItemLongClickListener {
        boolean onItemLongClick(int position, News news);
    }
} 