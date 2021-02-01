package com.beyond.beidou;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
    }
}
