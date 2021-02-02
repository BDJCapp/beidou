package com.beyond.beidou;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.xuexiang.xui.XUI;
import com.xuexiang.xui.utils.StatusBarUtils;

/**
 * @author: 李垚
 * @date: 2021/1/29
 */
public abstract class BaseActivity extends AppCompatActivity {

    public abstract void init();

    public abstract void initData();

    public abstract void initView();

    public abstract void initEvent();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XUI.initTheme(this);//初始化XUI
        //设置沉浸式状态栏
        //StatusBarUtils.initStatusBarStyle(this,false, ActivityCompat.getColor(this,R.color.main_blue));
        StatusBarUtils.translucent(this, ActivityCompat.getColor(this,R.color.main_blue));
    }
}
