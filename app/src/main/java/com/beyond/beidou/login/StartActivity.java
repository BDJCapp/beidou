package com.beyond.beidou.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.beyond.beidou.BaseActivity;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.api.ApiCallback;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.util.AutoUpdater;
import com.beyond.beidou.util.LogUtil;
import com.beyond.beidou.util.LoginUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class StartActivity extends BaseActivity {

    private Intent mIntent;
    private final int LOGIN = 1001;
    private final int MAIN = 1002;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case LOGIN:
                    mIntent.setClass(StartActivity.this, LoginActivity.class);
                    startActivity(mIntent);
                    finish();
                    break;
                case MAIN:
                    mIntent.setClass(StartActivity.this, MainActivity.class);
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
    public void initData() { }

    @Override
    public void initView() { }

    @Override
    public void initEvent() {
        checkForceUpdate();
    }

    public void autoLogin(){
        //如果不是首次登录且session到期时间未过则直接进入首页
        if (!"".equals(getStringFromSP("SessionExpireTimestamp"))){
            long sessionExpireTimestamp = Long.parseLong(getStringFromSP("SessionExpireTimestamp"));
            if (sessionExpireTimestamp >= Calendar.getInstance().getTimeInMillis()){
                //登录
                LogUtil.e("loginActivity initEvent()","自动登录中...");
                ApiConfig.setAccessToken(getStringFromSP("accessToken"));
                ApiConfig.setSessionUUID(getStringFromSP("sessionUUID"));
                LoginUtil.loginByPwd(StartActivity.this, getStringFromSP("userName"), getStringFromSP("password"), getStringFromSP("sessionUUID"), getStringFromSP("accessToken"), new ApiCallback() {
                    @Override
                    public void onSuccess(String res) {
                        try {
                            JSONObject object = new JSONObject(res);
                            String errCode = object.getString("ResponseCode");
                            if ("200".equals(errCode)){
                                mHandler.sendEmptyMessage(MAIN);
                            }else {
                                LogUtil.e("loginActivity initEvent()","****自动登录失败*****");
                                mHandler.sendEmptyMessage(LOGIN);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(Exception e) {
                        mHandler.sendEmptyMessage(LOGIN);
                    }
                });
            }else {
                LogUtil.e("loginActivity initEvent()","Session过期");
                mHandler.sendEmptyMessageDelayed(LOGIN, 500);
            }
        }else {
            //首次登录或者上次是退出登录，则进入登录页
            LogUtil.e("loginActivity initEvent()","跳转登录页");
            mHandler.sendEmptyMessageDelayed(LOGIN, 500);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //解决华为退出到后台，重启app问题
        if (!this.isTaskRoot()) {
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                finish();
                return;
            }
        }
        setContentView(R.layout.activity_start);
        init();

    }

    @Override
    protected void onResume() {
        super.onResume();
        initEvent();
    }

    public void checkForceUpdate(){

        if (!"".equals(getStringFromSP("checkUpdateExpireTimestamp"))){
            long checkUpdateExpireTimestamp = Long.parseLong(getStringFromSP("checkUpdateExpireTimestamp"));
            if (checkUpdateExpireTimestamp > Calendar.getInstance().getTimeInMillis()){
                autoLogin();
                return;
            }
        }

        Thread getTokenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtil.e("--------getAccessToken()",ApiConfig.getAccessToken());
                while (!LoginUtil.getAccessToken(StartActivity.this)) {
                }
            }
        });
        getTokenThread.start();
        try {
            getTokenThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final AutoUpdater autoUpdater = new AutoUpdater(StartActivity.this);
        autoUpdater.checkUpdate("v"+AutoUpdater.getLocalVersionName(getApplication()), new ApiCallback() {
            @Override
            public void onSuccess(String res) {
                autoLogin();
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }
}