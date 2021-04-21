package com.beyond.beidou.util;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.beyond.beidou.BaseActivity;
import com.beyond.beidou.api.Api;
import com.beyond.beidou.api.ApiCallback;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.test.BeanTest;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;
import android.util.Base64;


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
    public static boolean checkPwd(String pwd)
    {
        //密码12-64位且包含数字，大小写字母以及特殊符号
        Pattern p = Pattern.compile("(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%\\^&\\*()\\_]).{12,64}");
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
    public static boolean getSessionId(Context context)
    {
            FormBody body = new FormBody.Builder()
                    .add("AccessToken", ApiConfig.getAccessToken())
                    .add("SessionUUID", ApiConfig.getSessionUUID())
                    .build();

            Api.config(ApiConfig.GET_SESSION_UUID).postRequestFormBodySync(context,body,new ApiCallback() {
                @Override
                public void onSuccess(String res) {
                    if (!TextUtils.isEmpty(res)) {
                        try {
                            JSONObject object = new JSONObject(res);
                            ApiConfig.setSessionUUID(object.getString("SessionUUID"));
                            LogUtil.e("getSession  ",res);
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
        if ("00000000-0000-0000-0000-000000000000".equals(ApiConfig.getSessionUUID()))
        {
            return false;
        }
        else {
            return true;
        }
    }

    public static boolean getAccessToken(Context context) {
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
                        JSONObject object = new JSONObject(res);
                        ApiConfig.setAccessToken(object.getString("AccessToken"));

                        LogUtil.e("getToken  ",res);
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
        if ("".equals(ApiConfig.getAccessToken()))
        {
            return false;
        }
        else {
            return true;
        }
    }

    public void loginByPwd(Context context,String Username, String Password, String SessionUUID,String AccessToken, ApiCallback callback)
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

    //每隔59分钟获取一次Token
    public static void upDateToken(final Context context)
    {
        Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
//                LogUtil.e("定时任务","1111");
                while (!LoginUtil.getAccessToken(context)){}
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
    public static void DES3Encode(String Plaintext, String secretKey) throws Exception {
        // 密钥 长度不得小于24
//        String secretKey = "123456789012345678901234" ;
        // 向量 可有可无 终端后台也要约定
//        String iv = "01234567";
        // 加解密统一使用的编码方式
        String encoding = "utf-8";

        LogUtil.e("加密前,Plaintext，secretKey",Plaintext + "  " + secretKey);
        Key deskey = null;

        DESedeKeySpec spec = new DESedeKeySpec(secretKey.getBytes());

        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DESede");

        deskey = keyfactory.generateSecret(spec);

        Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");

//        IvParameterSpec ips = new IvParameterSpec( iv.getBytes());
//        cipher.init(Cipher. ENCRYPT_MODE, deskey, ips);

        cipher.init(Cipher.ENCRYPT_MODE, deskey);
        byte[] encryptData = cipher.doFinal(Plaintext.getBytes(encoding))
                ;
        LogUtil.e("加密后",Base64.encodeToString(encryptData,Base64.DEFAULT));
    }
}
