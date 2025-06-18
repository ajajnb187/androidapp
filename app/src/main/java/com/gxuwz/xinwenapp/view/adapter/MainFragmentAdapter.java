package com.gxuwz.xinwenapp.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

/**
 * 主界面Fragment适配器
 * 
 * 作用：
 * 1. 为主界面的ViewPager2提供Fragment
 * 2. 管理新闻列表和个人中心Fragment切换
 * 
 * 调用者：
 * MainActivity：主界面管理Fragment切换
 */
public class MainFragmentAdapter extends FragmentStateAdapter {
    private List<Fragment> fragmentList;
    
    /**
     * 构造函数
     * @param activity 宿主Activity
     * @param fragmentList Fragment列表
     */
    public MainFragmentAdapter(@NonNull FragmentActivity activity, List<Fragment> fragmentList) {
        super(activity);
        this.fragmentList = fragmentList;
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }
    
    @Override
    public int getItemCount() {
        return fragmentList.size();
    }
} 