package com.gxuwz.xinwenapp.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gxuwz.xinwenapp.R;
import com.gxuwz.xinwenapp.controller.UserController;
import com.gxuwz.xinwenapp.model.entity.User;
import com.gxuwz.xinwenapp.model.service.UserService.ApiCallback;

/**
 * 登录页Activity
 * 
 * 作用：
 * 1. 用户登录
 * 2. 跳转到注册页
 * 3. 跳转到找回密码页
 * 
 * 调用者：
 * 1. SplashActivity：启动页跳转
 * 2. RegisterActivity：注册成功后返回
 * 3. ForgotPasswordActivity：找回密码后返回
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etPhone;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private TextView tvForgotPassword;
    
    private UserController userController;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        userController = new UserController(this);
        
        initViews();
    }
    
    private void initViews() {
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        
        btnLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        tvForgotPassword.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.btnLogin) {
            login();
        } else if (id == R.id.tvRegister) {
            startActivity(new Intent(this, RegisterActivity.class));
        } else if (id == R.id.tvForgotPassword) {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        }
    }
    
    private void login() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // 输入验证
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 手机号格式验证
        if (!isValidPhoneNumber(phone)) {
            Toast.makeText(this, "请输入有效的手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 禁用登录按钮，避免重复点击
        btnLogin.setEnabled(false);
        
        // 调用控制器登录（异步）
        userController.login(phone, password, new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                // 恢复登录按钮
                btnLogin.setEnabled(true);
                
                if (user != null) {
                    // 登录成功
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    
                    // 跳转到主页
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    // 登录失败
                    Toast.makeText(LoginActivity.this, "手机号或密码错误", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onError(String errorMsg) {
                // 恢复登录按钮
                btnLogin.setEnabled(true);
                
                // 显示错误信息
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * 验证手机号格式
     * @param phone 手机号
     * @return 是否有效
     */
    private boolean isValidPhoneNumber(String phone) {
        // 简单的手机号验证：11位数字，以1开头
        return phone.length() == 11 && phone.startsWith("1");
    }
    
    @Override
    public void onBackPressed() {
        // 调用父类方法
        super.onBackPressed();
        
        // 退出应用
        finish();
    }
} 