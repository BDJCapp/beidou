package com.beyond.beidou.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.beyond.beidou.BaseActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.util.LogUtil;
import com.beyond.beidou.util.LoginUtil;


/**
 * @author: 李垚
 * @date: 2020/12/22
 */
public class StartActivity extends BaseActivity {

    private LoginUtil loginUtil;
    private Intent intent;
    boolean isFinish;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
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
        isFinish = LoginUtil.getAccessToken();
        while (!isFinish)
        {
            isFinish = LoginUtil.getAccessToken();
        }
        //获取到Token和SessionUUID之后结束启动页
        handler.sendEmptyMessageDelayed(1001, 200);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        init();
        Thread httpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                initEvent();
            }
        });
        httpThread.start();
    }
}