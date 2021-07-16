package com.beyond.beidou.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.beyond.beidou.BaseActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.util.LogUtil;
import com.beyond.beidou.util.LoginUtil;

/**
 * @author: 李垚
 * @date: 2020/12/22
 */
public class StartActivity extends BaseActivity {

    private Intent mIntent;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1001:
                    mIntent.setClass(StartActivity.this, LoginActivity.class);
                    startActivity(mIntent);
                    finish();
                    break;
            }
        }
    };

    @Override
    public void init() {
        mIntent = new Intent();
    }

    @Override
    public void initData() {
        Thread getTokenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!LoginUtil.getAccessToken(StartActivity.this)) {
                    LogUtil.e("StartActivity initData()", "循环请求Token");
                }
                LoginUtil.upDateToken(getApplicationContext());
                LogUtil.e("StartActivity 成功获取Token", ApiConfig.getAccessToken());
                LogUtil.e("StartActivity getToken Thread", Thread.currentThread().toString());
            }
        });
        getTokenThread.start();

        try {
            getTokenThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Thread getSessionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!LoginUtil.getSessionId(StartActivity.this)) {
                    LogUtil.e("StartActivity initData()", "循环请求Session");
                }
            }
        });
        getSessionThread.start();

        try {
            getSessionThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //获取到Token和SessionUUID之后等待500毫秒结束启动页
        mHandler.sendEmptyMessageDelayed(1001, 500);
    }

    @Override
    public void initView() {

    }

    @Override
    public void initEvent() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!this.isTaskRoot()) {   //解决华为退出到后台，重启app问题
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                finish();
                return;
            }
        }

        setContentView(R.layout.activity_start);
        init();
        if (LoginUtil.isNetworkUsable(this)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    initData();
                }
            }).start();
        }
    }
}