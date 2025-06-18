package com.gxuwz.xinwenapp.view.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.gxuwz.xinwenapp.R;
import com.gxuwz.xinwenapp.controller.UserController;
import com.gxuwz.xinwenapp.model.entity.User;
import com.gxuwz.xinwenapp.model.service.UserService;
import com.gxuwz.xinwenapp.util.SharedPreferencesUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * 编辑个人资料页面
 * 
 * 作用：
 * 1. 允许用户修改个人信息
 * 2. 包括昵称、性别、生日、邮箱等
 */
public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    
    private EditText etNickname;
    private RadioGroup rgGender;
    private RadioButton rbMale;
    private RadioButton rbFemale;
    private Button btnSelectBirthday;
    private EditText etEmail;
    private Button btnSave;
    private ProgressBar progressBar;
    
    private UserController userController;
    private User currentUser;
    private Calendar birthdayCalendar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        
        userController = new UserController(this);
        birthdayCalendar = Calendar.getInstance();
        
        initViews();
        loadUserProfile();
    }
    
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("编辑个人资料");
        }
        
        etNickname = findViewById(R.id.et_nickname);
        rgGender = findViewById(R.id.rg_gender);
        rbMale = findViewById(R.id.rb_male);
        rbFemale = findViewById(R.id.rb_female);
        btnSelectBirthday = findViewById(R.id.btn_select_birthday);
        etEmail = findViewById(R.id.et_email);
        btnSave = findViewById(R.id.btn_save);
        progressBar = findViewById(R.id.progress_bar);
        
        btnSelectBirthday.setOnClickListener(v -> showDatePickerDialog());
        btnSave.setOnClickListener(v -> saveUserProfile());
    }
    
    private void loadUserProfile() {
        // 先检查登录状态
        if (!userController.isLoggedIn()) {
            // 尝试从SharedPreferences获取手机号并恢复登录状态
            String phoneNumber = SharedPreferencesUtil.getString(this, "phone_number", "");
            if (!phoneNumber.isEmpty()) {
                showLoading(true);
                userController.getUserInfo(phoneNumber, new UserService.ApiCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        // 获取到用户信息后，显示用户资料
                        showLoading(false);
                        currentUser = user;
                        displayUserProfile(user);
                    }
                    
                    @Override
                    public void onError(String errorMsg) {
                        showLoading(false);
                        Toast.makeText(EditProfileActivity.this, "获取用户信息失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "获取用户信息失败: " + errorMsg);
                        finish();
                    }
                });
            } else {
                Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                finish();
            }
            return;
        }
        
        // 已登录，直接刷新用户信息
        showLoading(true);
        
        userController.refreshCurrentUser(new UserService.ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                showLoading(false);
                currentUser = user;
                displayUserProfile(user);
            }
            
            @Override
            public void onError(String errorMsg) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, "获取用户信息失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "获取用户信息失败: " + errorMsg);
                finish();
            }
        });
    }
    
    private void displayUserProfile(User user) {
        if (user == null) return;
        
        etNickname.setText(user.getNickname());
        
        if ("男".equals(user.getGender())) {
            rbMale.setChecked(true);
        } else if ("女".equals(user.getGender())) {
            rbFemale.setChecked(true);
        }
        
        if (user.getBirthday() != null && !user.getBirthday().isEmpty()) {
            btnSelectBirthday.setText(user.getBirthday());
            
            try {
                // 解析生日字符串为Calendar对象
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                birthdayCalendar.setTime(sdf.parse(user.getBirthday()));
            } catch (Exception e) {
                Log.e(TAG, "解析生日日期失败", e);
            }
        }
        
        etEmail.setText(user.getEmail());
    }
    
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    birthdayCalendar.set(Calendar.YEAR, year);
                    birthdayCalendar.set(Calendar.MONTH, month);
                    birthdayCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String formattedDate = sdf.format(birthdayCalendar.getTime());
                    btnSelectBirthday.setText(formattedDate);
                },
                birthdayCalendar.get(Calendar.YEAR),
                birthdayCalendar.get(Calendar.MONTH),
                birthdayCalendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // 设置最大日期为今天
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
    
    private void saveUserProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "用户信息不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String nickname = etNickname.getText().toString().trim();
        if (nickname.isEmpty()) {
            Toast.makeText(this, "昵称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 获取性别
        String gender = "";
        int checkedId = rgGender.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_male) {
            gender = "男";
        } else if (checkedId == R.id.rb_female) {
            gender = "女";
        }
        
        // 获取生日
        String birthday = btnSelectBirthday.getText().toString();
        if (birthday.equals("选择生日")) {
            birthday = "";
        }
        
        // 获取邮箱
        String email = etEmail.getText().toString().trim();
        
        // 更新用户信息
        currentUser.setNickname(nickname);
        currentUser.setGender(gender);
        currentUser.setBirthday(birthday);
        currentUser.setEmail(email);
        
        showLoading(true);
        
        userController.updateUserProfile(currentUser, new UserService.ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                showLoading(false);
                if (result) {
                    Toast.makeText(EditProfileActivity.this, "个人资料更新成功", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "个人资料更新失败", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onError(String errorMsg) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, "更新失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "更新用户资料失败: " + errorMsg);
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