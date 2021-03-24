package com.beyond.beidou.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.beyond.beidou.BaseActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.util.LoginUtil;


/**
 * @author: 李垚
 * @date: 2020/12/22
 */
public class StartActivity extends BaseActivity {

    private LoginUtil loginUtil;
    private Intent intent;
    private Handler handler = new Handler() {
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

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        init();
//        loginUtil.getSessionId();  //获取SessionUUID
//        Log.e("启动页获取的SessionUUID为", APIUtil.getSessionUUID());
//        loginUtil.getAccessToken();  //获取SessionUUID
//        Log.e("请求成功,启动页AccessTokenID为", APIUtil.getAccessTokenID());
        handler.sendEmptyMessageDelayed(1001, 1000);
    }

    private void hideStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }


}