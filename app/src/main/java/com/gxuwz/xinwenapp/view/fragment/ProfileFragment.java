package com.gxuwz.xinwenapp.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.gxuwz.xinwenapp.R;
import com.gxuwz.xinwenapp.controller.NewsController;
import com.gxuwz.xinwenapp.controller.UserController;
import com.gxuwz.xinwenapp.model.entity.News;
import com.gxuwz.xinwenapp.model.entity.User;
import com.gxuwz.xinwenapp.model.service.NewsService;
import com.gxuwz.xinwenapp.model.service.UserService;
import com.gxuwz.xinwenapp.util.SharedPreferencesUtil;
import com.gxuwz.xinwenapp.view.activity.EditProfileActivity;
import com.gxuwz.xinwenapp.view.activity.FollowsActivity;
import com.gxuwz.xinwenapp.view.activity.HistoryActivity;
import com.gxuwz.xinwenapp.view.activity.LoginActivity;
import com.gxuwz.xinwenapp.view.activity.SettingsActivity;
import com.gxuwz.xinwenapp.view.activity.UpdateActivity;

import java.util.List;

/**
 * 个人中心Fragment
 * 
 * 作用：
 * 1. 显示用户信息
 * 2. 提供用户相关操作：如退出登录等
 * 
 * 调用者：
 * MainActivity：作为主界面的一个页面
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ProfileFragment";
    
    // UI组件
    private ImageView ivAvatar;
    private TextView tvUsername;
    private TextView tvPhone;
    private TextView tvGender;
    private TextView tvBirthday;
    private TextView tvEmail;
    private TextView tvHistoryTitle;
    private TextView tvFavoritesTitle;
    private TextView tvSettings;
    private TextView tvCheckUpdate;
    private Button btnLogout;
    private Button btnEditProfile;
    
    // 控制器
    private UserController userController;
    private NewsController newsController;
    
    // 数据
    private User currentUser;
    private static final String APP_VERSION = "1.0.0";
    
    // 请求码
    private static final int REQUEST_EDIT_PROFILE = 100;
    
    // 编辑资料的ActivityResultLauncher
    private ActivityResultLauncher<Intent> editProfileLauncher;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        userController = new UserController(requireContext());
        newsController = new NewsController(requireContext());
        
        // 初始化ActivityResultLauncher
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        Log.d(TAG, "编辑资料成功返回，立即刷新用户信息");
                        loadUserInfo();
                    }
                });
        
        initViews(view);
        loadUserInfo();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 页面每次可见时重新加载用户信息，确保数据最新
        loadUserInfo();
    }
    
    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvUsername = view.findViewById(R.id.tv_username);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvGender = view.findViewById(R.id.tv_gender);
        tvBirthday = view.findViewById(R.id.tv_birthday);
        tvEmail = view.findViewById(R.id.tv_email);
        tvHistoryTitle = view.findViewById(R.id.tv_history_title);
        tvFavoritesTitle = view.findViewById(R.id.tv_favorites_title);
        tvSettings = view.findViewById(R.id.tv_settings);
        tvCheckUpdate = view.findViewById(R.id.tv_check_update);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        
        // 设置点击事件
        btnLogout.setOnClickListener(this);
        btnEditProfile.setOnClickListener(this);
        tvHistoryTitle.setOnClickListener(this);
        tvFavoritesTitle.setOnClickListener(this);
        tvSettings.setOnClickListener(this);
        tvCheckUpdate.setOnClickListener(this);
    }
    
    private void loadUserInfo() {
        String phoneNumber = SharedPreferencesUtil.getString(requireContext(), "phone_number", "");
        Log.d(TAG, "正在加载用户信息，手机号：" + phoneNumber);
        
        if (phoneNumber.isEmpty()) {
            Log.d(TAG, "本地未保存登录信息，尝试从UserController获取当前用户");
            // 尝试从UserController获取当前用户
            User user = userController.getCurrentUser();
            if (user != null) {
                Log.d(TAG, "从UserController获取到当前用户：" + user.getPhoneNumber());
                currentUser = user;
                updateUI();
                return;
            }
            
            // 未登录，跳转到登录页面
            Log.d(TAG, "用户未登录，跳转到登录页面");
            navigateToLogin();
            return;
        }
        
        // 强制从数据库刷新用户信息，而不是使用缓存
        Log.d(TAG, "强制从数据库刷新用户信息");
        userController.refreshCurrentUser(new UserService.ApiCallback<User>() {
            @Override
            public void onSuccess(User result) {
                Log.d(TAG, "刷新用户信息成功：" + result.getPhoneNumber());
                currentUser = result;
                updateUI();
            }
            
            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "刷新用户信息失败：" + errorMsg);
                
                // 如果刷新失败，尝试从数据库直接获取
                Log.d(TAG, "尝试从数据库直接获取用户信息");
                userController.getUserInfo(phoneNumber, new UserService.ApiCallback<User>() {
                    @Override
                    public void onSuccess(User result) {
                        Log.d(TAG, "获取用户信息成功：" + result.getPhoneNumber());
                        currentUser = result;
                        updateUI();
                    }
                    
                    @Override
                    public void onError(String errorMsg) {
                        Log.e(TAG, "获取用户信息失败：" + errorMsg);
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                        
                        // 检查是否是控制器中有缓存的用户信息
                        User cachedUser = userController.getCurrentUser();
                        if (cachedUser != null) {
                            Log.d(TAG, "使用控制器缓存的用户信息");
                            currentUser = cachedUser;
                            updateUI();
                            return;
                        }
                        
                        // 获取用户信息失败，可能是登录状态过期，跳转到登录页面
                        navigateToLogin();
                    }
                });
            }
        });
    }
    
    private void updateUI() {
        if (currentUser != null) {
            // 保存手机号到SharedPreferences确保下次可以直接获取
            SharedPreferencesUtil.putString(requireContext(), "phone_number", currentUser.getPhoneNumber());
            
            // 设置用户名显示，优先使用昵称，如果为空则显示手机号
            String displayName = currentUser.getNickname();
            if (displayName == null || displayName.isEmpty()) {
                displayName = currentUser.getPhoneNumber();
            }
            tvUsername.setText(displayName);
            
            // 设置手机号显示，格式化为部分隐藏
            String phoneNumber = currentUser.getPhoneNumber();
            if (phoneNumber != null && phoneNumber.length() >= 11) {
                String maskedPhone = "手机号: " + phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7);
                tvPhone.setText(maskedPhone);
            } else {
                tvPhone.setText("手机号: " + phoneNumber);
            }
            
            // 设置性别显示
            String gender = currentUser.getGender();
            if (gender == null || gender.isEmpty()) {
                gender = "未设置";
            }
            tvGender.setText("性别: " + gender);
            
            // 设置生日显示
            String birthday = currentUser.getBirthday();
            if (birthday == null || birthday.isEmpty()) {
                birthday = "未设置";
            }
            tvBirthday.setText("生日: " + birthday);
            
            // 设置邮箱显示
            String email = currentUser.getEmail();
            if (email == null || email.isEmpty()) {
                email = "未设置";
            }
            tvEmail.setText("邮箱: " + email);
            
            // 加载头像
            if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
                // 有头像URL，加载网络图片
                Glide.with(requireContext())
                        .load(currentUser.getAvatarUrl())
                        .placeholder(R.drawable.ic_default_avatar)
                        .error(R.drawable.ic_default_avatar)
                        .circleCrop()
                        .into(ivAvatar);
            } else {
                // 没有头像URL，使用默认头像
                ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                // 设置背景
                ivAvatar.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_circle_avatar));
            }
        }
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        startActivity(intent);
        // 不要调用finish()，否则会关闭MainActivity
    }
    
    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        
        if (viewId == R.id.btn_logout) {
            logout();
        } else if (viewId == R.id.btn_edit_profile) {
            editProfile();
        } else if (viewId == R.id.tv_history_title) {
            showBrowsingHistory();
        } else if (viewId == R.id.tv_favorites_title) {
            showFollows();
        } else if (viewId == R.id.tv_settings) {
            showSettings();
        } else if (viewId == R.id.tv_check_update) {
            checkUpdate();
        }
    }
    
    private void editProfile() {
        if (currentUser != null) {
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            editProfileLauncher.launch(intent);
        } else {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        }
    }
    
    private void logout() {
        // 执行退出登录
        userController.logout();
        
        // 清除本地用户信息
        SharedPreferencesUtil.remove(requireContext(), "phone_number");
        SharedPreferencesUtil.remove(requireContext(), "userId");
        SharedPreferencesUtil.remove(requireContext(), "token");
        
        // 跳转到登录页面
        navigateToLogin();
        
        // 提示用户
        Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show();
    }
    
    private void showBrowsingHistory() {
        // 跳转到浏览历史页面
        Intent intent = new Intent(requireContext(), HistoryActivity.class);
        startActivity(intent);
    }
    
    private void showFollows() {
        // 跳转到关注列表页面
        Intent intent = new Intent(requireContext(), FollowsActivity.class);
        startActivity(intent);
    }
    
    private void showSettings() {
        // 跳转到设置页面
        Intent intent = new Intent(requireContext(), SettingsActivity.class);
        startActivity(intent);
    }
    
    private void checkUpdate() {
        // 跳转到版本更新页面
        Intent intent = new Intent(requireContext(), UpdateActivity.class);
        startActivity(intent);
    }
} 