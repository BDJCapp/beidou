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
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.util.LogUtil;
import com.beyond.beidou.util.LoginUtil;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


/**
 * @author: 李垚
 * @date: 2020/12/22
 */

public class StartActivity extends BaseActivity {

    private LoginUtil loginUtil;
    private Intent intent;
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
        Thread getTokenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!LoginUtil.getAccessToken(StartActivity.this)){
                    LogUtil.e("StartActivity initData()","循环请求Token");
                }
                LoginUtil.upDateToken(getApplicationContext());
                LogUtil.e("StartActivity 成功获取Token", ApiConfig.getAccessToken());
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
                while (!LoginUtil.getSessionId(StartActivity.this)){
                    LogUtil.e("StartActivity initData()","循环请求Session");
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
        handler.sendEmptyMessageDelayed(1001, 500);
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
        setContentView(R.layout.activity_start);
        init();
        initData();
    }
}