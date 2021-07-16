package com.beyond.beidou.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.beyond.beidou.BaseActivity;
import com.beyond.beidou.api.Api;
import com.beyond.beidou.api.ApiCallback;
import com.beyond.beidou.api.ApiConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import okhttp3.FormBody;



public class LoginUtil {

    public static final int LOGINBYPHONE = 1;
    public static final int LOGINBYPWD = 2;
    public static final int LOGINBYEMAIL = 3;
    public static final int CHANGEPWD = 0;
    public static final int RESETPWD = 1;


    public LoginUtil() {
    }
    /**
     * 判断手机号是否有效
     * @param account 用户输入的手机号或者邮箱
     * @param loginType 登录方式
     * @return  手机号有效返回为true，手机号无效返回false
     */
    public static boolean checkAccount(String account, int loginType)
    {
        Pattern p = null;
        if (loginType == LOGINBYPWD)
        {
            //账号以大写或小写字母开头，+（4-31位大小写字符或数字）
            p = Pattern.compile("^[a-zA-Z][a-zA-Z\\d]{4,31}$");
        }
        else if (loginType == LOGINBYPHONE)
        {
            //手机号第一位为1，第二位为3/4/5/7/8，+（5-9位数字）
            p = Pattern.compile("^1[3|4|5|7|8][0-9]\\d{4,8}$");
//            p = Pattern.compile("^1(3([0-35-9]\\d|4[1-8])|4[14-9]\\d|5([0-35689]\\d|7[1-79])|66\\d|7[2-35-8]\\d|8\\d{2}|9[13589]\\d)\\d{7}$");
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
    public static boolean checkPwd(String pwd)
    {
        //密码12-64位且包含数字，大小写字母以及特殊符号
        Pattern p = Pattern.compile("(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[\\W_]).{12,64}");
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
    public static boolean getSessionId(final Context context)
    {
        final String[] responseCode = new String[1];
        String sessionUUID = ((BaseActivity)context).getStringFromSP("SessionUUID");
        if ("".equals(sessionUUID))//缓存中无数据时，获取默认的一串0
        {
            sessionUUID = ApiConfig.getSessionUUID();
        }
        LogUtil.e("缓存中的SessionUUID",((BaseActivity)context).getStringFromSP("SessionUUID"));
        LogUtil.e("登录使用的SessionUUID",sessionUUID);

        FormBody body = new FormBody.Builder()
                    .add("AccessToken", ApiConfig.getAccessToken())
                    .add("SessionUUID", sessionUUID)
                    .build();

        Api.config(ApiConfig.GET_SESSION_UUID).postRequestFormBodySync(context,body,new ApiCallback() {
                @Override
                public void onSuccess(String res) {
                    if (!TextUtils.isEmpty(res)) {
                        try {
                            JSONObject object = new JSONObject(res);
                            responseCode[0] = object.getString("ResponseCode");
                            if ("201".equals(responseCode[0]))
                            {
                                //当缓存中SessionUUID已登录时，更新内存中SessionUUID
                                ApiConfig.setSessionUUID(((BaseActivity)context).getStringFromSP("SessionUUID"));
                                return;
                            }
                            String sessionUUID = object.getString("SessionUUID");
                            ApiConfig.setSessionUUID(sessionUUID);
                            ((BaseActivity)context).saveStringToSP("SessionUUID",sessionUUID);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    Log.e("请求session失败","错误原因" + e.getMessage());
                }
            });
        if ("202".equals(responseCode[0]) || "201".equals(responseCode[0]))
        {
            //会话请求成功（202）或者会话已登录（201）
            return true;
        }
        else if ("204".equals(responseCode[0]))
        {
            //会话已注销
            while (!getAccessToken(context)){
                LogUtil.e("LoginUtil getSession","循环获取Token");
            }
            ((BaseActivity)context).saveStringToSP("SessionUUID","");
            ApiConfig.setSessionUUID("00000000-0000-0000-0000-000000000000");
            return false;
        }else {
                return false;
        }
    }

    public static boolean getAccessToken(Context context) {
        final String[] responseCode = new String[1];
        FormBody body = new FormBody.Builder()
                .add("GrantType", ApiConfig.GrantType)
                .add("AppID", ApiConfig.AppID)
                .add("AppSecret", ApiConfig.AppSecret )
                .build();

        Api.config(ApiConfig.GET_ACCESS_TOKEN).postRequestFormBodySync(context,body,new ApiCallback() {
            @Override
            public void onSuccess(String res) {
                if (!TextUtils.isEmpty(res)) {
                    try {
                        LogUtil.e("Token返回值",res);
                        JSONObject object = new JSONObject(res);
                        ApiConfig.setAccessToken(object.getString("AccessToken"));
                        responseCode[0] = object.getString("ResponseCode");
                        LogUtil.e("getToken  执行结束",res);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("请求AccessToken失败", "错误原因" + e.getMessage());
            }
        });
        if ("200".equals(responseCode[0]))
        {
            return true;
        }
        else {
            return false;
        }
    }


    public static void loginByPwd(Context context,String Username, String Password, String SessionUUID,String AccessToken, ApiCallback callback)
    {
        FormBody body = new FormBody.Builder()
                .add("Username",Username)
                .add("Password",Password)
                .add("SessionUUID",SessionUUID)
                .add("AccessToken",AccessToken)
                .build();
        Api.config(ApiConfig.LOGIN).postRequestFormBody(context,body,callback);
    }

    public static void logout(Context context,String Username, String SessionUUID,String AccessToken, ApiCallback callback)
    {
        FormBody body = new FormBody.Builder()
                .add("Username",Username)
                .add("SessionUUID",SessionUUID)
                .add("AccessToken",AccessToken)
                .build();
        Api.config(ApiConfig.LOGOUT).postRequestFormBody(context,body,callback);
    }

    /**
     * 每隔59分钟更新一次Token
     * @param context 上下文
     */
    public static void upDateToken(final Context context)
    {
        Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
//                LogUtil.e("定时任务","1111");
                while (!LoginUtil.getAccessToken(context)){
                    LogUtil.e("LoginUtil upDateToken","循环获取Token");
                }
            }
        };
        timer.schedule(timerTask,59*60*1000,59*60*1000);     //每隔59分钟执行一次
    }


    /**
     * 使用3DES加密
     * @param Plaintext 明文
     * @param secretKey 密钥
     * @throws Exception
     */
    public static String DES3Encode(String Plaintext, String secretKey) {

        String keyIv = MD5Encode(secretKey).toLowerCase();
        String iv = keyIv.substring(keyIv.length() - 8);
        String encoding = "utf-8";
        DESedeKeySpec spec;
        SecretKeyFactory keyfactory;
        Key deskey = null;
        Cipher cipher;
        IvParameterSpec ips;
        byte[] encryptData = new byte[0];

//        LogUtil.e("MD5加密后",keyIv);
//        LogUtil.e("截取的后8位",iv);
//        LogUtil.e("加密前,Plaintext，secretKey",Plaintext + "  " + keyIv);

        try {
            spec = new DESedeKeySpec(keyIv.getBytes());

            keyfactory = SecretKeyFactory.getInstance("DESede");

            deskey = keyfactory.generateSecret(spec);

            cipher = Cipher.getInstance("desede/CBC/PKCS7Padding");

            ips = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, deskey, ips);

            encryptData = cipher.doFinal(Plaintext.getBytes(encoding));
        }catch (Exception e)
        {
            e.printStackTrace();
        }
//        LogUtil.e("加密后",Base64.encodeToString(encryptData,Base64.DEFAULT));
        return Base64.encodeToString(encryptData,Base64.DEFAULT);
    }

    /**
     * 计算字符串MD5的值
     * @param string 传入要加密的值
     * @return 计算后的MD5值
     */
    public static String MD5Encode(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static boolean isNetworkUsable(final Context context)
    {
        boolean isUsable = true;
        if (!isNetworkConnected(context))
        {
            isUsable = false;
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context,"网络不可用，请检查网络设置！",Toast.LENGTH_LONG).show();
                }
            });
        }
        return isUsable;
    }
}
