package com.beyond.beidou.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import com.beyond.beidou.BaseActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.util.APIUtil;
import com.beyond.beidou.util.LoginUtil;

public class StartActivity extends BaseActivity {

    private LoginUtil loginUtil;
    private Intent intent;
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what)
            {
                case 1001:
                    intent.setClass(StartActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };

    @Override
    public void init() {
        intent = new Intent();
        loginUtil = new LoginUtil();
    }

    @Override
    public void initData() {

    }

    @Override
    public void initView() {

    }

    @Override
    public void initEvent() {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        init();
        loginUtil.getSessionId();  //获取SessionUUID
        Log.e("启动页获取的SessionUUID为", APIUtil.getSessionUUID());
        handler.sendEmptyMessageDelayed(1001, 3000);
    }
    private void hideStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }
}