package com.beyond.beidou;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.beyond.beidou.util.LogUtil;
import com.beyond.beidou.util.LoginUtil;
import com.xuexiang.xui.XUI;
import com.xuexiang.xui.utils.StatusBarUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author: 李垚
 * @date: 2021/1/29
 */
public abstract class BaseActivity extends AppCompatActivity {
    private Context mContext;

    public abstract void init();

    public abstract void initData();

    public abstract void initView();

    public abstract void initEvent();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }

    public void showToast(String msg){
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    public void showToastSync(String msg){
        Looper.prepare();
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        Looper.loop();
    }

    public void navigateTo(Class cls){
        Intent intent = new Intent(mContext, cls);
        startActivity(intent);
    }

    public void navigateToWithFlag(Class cls, int flags){
        Intent intent = new Intent(mContext, cls);
        intent.setFlags(flags);
        startActivity(intent);
    }

    protected void saveStringToSP(String key, String value){
        SharedPreferences sp = getSharedPreferences("sp_user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }
}
