package com.gxuwz.xinwenapp.view.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.gxuwz.xinwenapp.R;
import com.gxuwz.xinwenapp.controller.UserController;
import com.gxuwz.xinwenapp.model.service.UserService;

/**
 * 修改密码页面
 * 
 * 作用：
 * 1. 允许用户修改登录密码
 * 2. 验证旧密码，确认新密码
 */
public class ChangePasswordActivity extends AppCompatActivity {
    private static final String TAG = "ChangePasswordActivity";
    
    private EditText etOldPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnSave;
    private ProgressBar progressBar;
    
    private UserController userController;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        
        userController = new UserController(this);
        
        initViews();
    }
    
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("修改密码");
        }
        
        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSave = findViewById(R.id.btn_save);
        progressBar = findViewById(R.id.progress_bar);
        
        btnSave.setOnClickListener(v -> validateAndChangePassword());
    }
    
    private void validateAndChangePassword() {
        // 获取输入内容
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        // 验证输入
        if (TextUtils.isEmpty(oldPassword)) {
            Toast.makeText(this, "请输入原密码", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "请输入新密码", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "请确认新密码", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (newPassword.length() < 6) {
            Toast.makeText(this, "新密码长度不能少于6位", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (oldPassword.equals(newPassword)) {
            Toast.makeText(this, "新密码不能与原密码相同", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示加载进度
        showLoading(true);
        
        // 调用控制器修改密码
        userController.changePassword(oldPassword, newPassword, new UserService.ApiCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                showLoading(false);
                
                switch (result) {
                    case 0:
                        // 修改成功
                        Toast.makeText(ChangePasswordActivity.this, "密码修改成功，请重新登录", Toast.LENGTH_SHORT).show();
                        // 退出登录
                        userController.logout();
                        // 返回登录页面
                        setResult(RESULT_OK);
                        finish();
                        break;
                    case 1:
                        // 用户不存在
                        Toast.makeText(ChangePasswordActivity.this, "用户不存在", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        // 旧密码错误
                        Toast.makeText(ChangePasswordActivity.this, "原密码错误", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        // 其他错误
                        Toast.makeText(ChangePasswordActivity.this, "修改密码失败", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            
            @Override
            public void onError(String errorMsg) {
                showLoading(false);
                Toast.makeText(ChangePasswordActivity.this, "修改密码失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "修改密码失败: " + errorMsg);
            }
        });
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 