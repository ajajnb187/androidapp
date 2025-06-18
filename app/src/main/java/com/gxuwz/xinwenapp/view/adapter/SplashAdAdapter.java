package com.gxuwz.xinwenapp.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gxuwz.xinwenapp.R;

import java.util.List;

/**
 * 启动页广告适配器
 * 
 * 作用：
 * 1. 为ViewPager2提供广告数据
 * 2. 加载和显示广告图片
 * 
 * 调用者：
 * SplashActivity：展示启动页广告
 */
public class SplashAdAdapter extends RecyclerView.Adapter<SplashAdAdapter.AdViewHolder> {
    
    private Context context;
    private List<Integer> adResources;
    private OnAdClickListener onAdClickListener;
    
    public interface OnAdClickListener {
        void onAdClick(int position);
    }
    
    /**
     * 构造函数
     * @param context 上下文
     * @param adResources 广告图片资源列表
     * @param listener 广告点击事件监听器
     */
    public SplashAdAdapter(Context context, List<Integer> adResources, OnAdClickListener listener) {
        this.context = context;
        this.adResources = adResources;
        this.onAdClickListener = listener;
    }
    
    @NonNull
    @Override
    public AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_splash_ad, parent, false);
        return new AdViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AdViewHolder holder, int position) {
        holder.ivAd.setImageResource(adResources.get(position));
        
        holder.ivAd.setOnClickListener(v -> {
            if (onAdClickListener != null) {
                onAdClickListener.onAdClick(position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return adResources != null ? adResources.size() : 0;
    }
    
    /**
     * 视图持有者类
     */
    static class AdViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAd;
        
        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAd = itemView.findViewById(R.id.ivAd);
        }
    }
} 