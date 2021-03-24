package com.beyond.beidou.util;

import android.content.Intent;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.beyond.beidou.api.Api;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.test.BeanTest;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;


public class LoginUtil {

    public static final int LOGINBYPHONE = 1;
    public static final int LOGINBYPWD = 2;
    public static final int LOGINBYEMAIL = 3;


    public LoginUtil() {
    }
    /**
     * 判断手机号是否有效
     * @param account 用户输入的手机号或者邮箱
     * @param loginType 登录方式
     * @return  手机号有效返回为true，手机号无效返回false
     */
    public boolean checkAccount(String account, int loginType)
    {
        Pattern p = null;
        if (loginType == LOGINBYPHONE)
        {
            //设置手机号正则表达式
            p = Pattern.compile("^1(3([0-35-9]\\d|4[1-8])|4[14-9]\\d|5([0-35689]\\d|7[1-79])|66\\d|7[2-35-8]\\d|8\\d{2}|9[13589]\\d)\\d{7}$");
        }else if (loginType == LOGINBYEMAIL)
        {
            //设置邮箱正则表达式
            p = Pattern.compile("^[a-z\\d]+(\\.[a-z\\d]+)*@([\\da-z](-[\\da-z])?)+(\\.{1,2}[a-z]+)+$");
        }

        Matcher m = p.matcher(account);
        if (m.matches())
        {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * 判断密码的有效性
     * @param pwd 用户输入的密码
     * @return 有效返回true,无效返回false
     */
    public boolean checkPwd(String pwd)
    {
        //密码8-16位且包含数字，大小写字母以及特殊符号
        Pattern p = Pattern.compile("(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%\\^&\\*()\\_]).{8,16}");
        Matcher m = p.matcher(pwd);
        if (m.matches())
        {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * 获取SessionUUID
     * @return SessionUUID
     */
    public String getSessionId()
    {
            Log.e("SessionUUI中的SessionUUID", ApiConfig.getSessionUUID());
            Log.e("SessionUUID中的Token", ApiConfig.getAccessToken());
            FormBody body = new FormBody.Builder()
                    .add("AccessToken", ApiConfig.getAccessToken())
                    .add("SessionUUID", ApiConfig.getSessionUUID())
                    .build();

            Api.config(ApiConfig.GET_SESSION_UUID).postRequestFormBody(body, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.e("请求session失败","错误原因" + e.getMessage());
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    /*
                        注意okHttp3中.string()方法只能调用一次，
                        否则会出现EAndroidRuntime FATAL EXCEPTION OkHttp Dispatcher错误
                     */
                    String responseText = response.body().string();
                    Log.e("SessionUUIDresponse的内容",responseText);
                    if (!TextUtils.isEmpty(responseText)) {
                        try {
                            JSONObject object = new JSONObject(responseText);
                            ApiConfig.setSessionUUID(object.getString("SessionUUID"));
                            //监听SessionUUID值改变
                            Log.e("请求SessionUUID", "Session:" + object.getString("SessionUUID"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("JsonException","SessionUUID错误信息为" + e.getMessage());
                        }
                    }
                    Log.e("请求成功，SessionUUID为", ApiConfig.getSessionUUID());
                }
            });

        return ApiConfig.getSessionUUID();
    }

    public String getAccessToken() {
        FormBody body = new FormBody.Builder()
                .add("GrantType", ApiConfig.GrantType)
                .add("AppID", ApiConfig.AppID)
                .add("AppSecret", ApiConfig.AppSecret )
                .build();

        Api.config(ApiConfig.GET_ACCESS_TOKEN).postRequestFormBody(body, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("请求AccessToken失败", "错误原因" + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();
                Log.e("AccessTokenresponse的内容",  responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject object = new JSONObject(responseText);
                        //Log.e("解析的AccessTokenID",object.getString("AccessToken"));
                        ApiConfig.setAccessToken(object.getString("AccessToken"));
                        Log.e("AccessToken", "access:" + object.getString("AccessToken"));
                        getSessionId();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("JsonException","AccessToken错误信息为" + e.getMessage());
                    }
                }
                //Log.e("请求成功,AccessTokenID为", APIUtil.getAccessTokenID());
            }
        });
        return ApiConfig.getAccessToken();
    }


    public void loginByPwd(String Username, String Password, String SessionUUID,String AccessToken, Callback callback)
    {
        FormBody body = new FormBody.Builder()
                .add("Username",Username)
                .add("Password",Password)
                .add("SessionUUID",SessionUUID)
                .add("AccessToken",AccessToken)
                .build();
        Api.config(ApiConfig.LOGIN).postRequestFormBody(body,callback);
    }

    public void logout(String Username, String SessionUUID,String AccessToken, Callback callback)
    {
        FormBody body = new FormBody.Builder()
                .add("Username",Username)
                .add("SessionUUID",SessionUUID)
                .add("AccessToken",AccessToken)
                .build();
        Api.config(ApiConfig.LOGOUT).postRequestFormBody(body,callback);
    }
}
