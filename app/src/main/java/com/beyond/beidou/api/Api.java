package com.beyond.beidou.api;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.beyond.beidou.login.LoginActivity;
import com.beyond.beidou.login.StartActivity;
import com.beyond.beidou.util.LoginUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

public class Api {
    private static OkHttpClient client;
    private static String requestUrl;
    private static Map<String, Object> mParams;
    public static Api api = new Api();
    public Api(){}


    public static Api config(String url, HashMap<String, Object> params){
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) //连接超时
                .readTimeout(30, TimeUnit.SECONDS) //读超时
                .writeTimeout(30, TimeUnit.SECONDS) //写超时
                .build();
        requestUrl = ApiConfig.BASE_URL + url;
        mParams = params;
        return api;
    }

    public static Api config(String url){
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) //连接超时
                .readTimeout(30, TimeUnit.SECONDS) //读超时
                .writeTimeout(30, TimeUnit.SECONDS) //写超时
                .build();
        requestUrl = ApiConfig.BASE_URL + url;
        return api;
    }

    public void postRequest(final Context context, final ApiCallback callback){
        JSONObject jsonObject = new JSONObject(mParams);
        String jsonStr = jsonObject.toString();
        RequestBody requestBodyJson =
                RequestBody.create(jsonStr, MediaType.parse("application/json;charset=utf-8"));
        Request request = new Request.Builder()
                .url(requestUrl)
                .post(requestBodyJson)
                .build();
        final Call call = client.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                String responseCode = parseSimpleJson(result,"ResponseCode");
                if (!responseCodeHandling(context,responseCode))
                {
                    LoginUtil.updateSessionExpireTimestamp(context); //每次调用接口都会更新session过期时间
                    callback.onSuccess(result);
                }else{
                    callback.onFailure(new RuntimeException());
                }
            }
        });
    }

    public void postRequestSync(Context context,ApiCallback callback){
        JSONObject jsonObject = new JSONObject(mParams);
        String jsonStr = jsonObject.toString();
        RequestBody requestBodyJson =
                RequestBody.create(jsonStr, MediaType.parse("application/json;charset=utf-8")
                );
        Request request = new Request.Builder()
                .url(requestUrl)
                .post(requestBodyJson)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                String responseText = response.body().string();
                String responseCode = parseSimpleJson(responseText,"ResponseCode");
                if (!responseCodeHandling(context,responseCode))
                {
                    LoginUtil.updateSessionExpireTimestamp(context);
                    callback.onSuccess(responseText);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void postJsonString(final Context context, String jsonString, final ApiCallback callback){
        RequestBody requestBodyJson =
                RequestBody.create(jsonString, MediaType.parse("application/json;charset=utf-8"));
        Request request = new Request.Builder()
                .url(requestUrl)
                .post(requestBodyJson)
                .build();
        final Call call = client.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();

                String responseCode = parseSimpleJson(result,"ResponseCode");
                if (!responseCodeHandling(context,responseCode))
                {
                    LoginUtil.updateSessionExpireTimestamp(context);
                    callback.onSuccess(result);
                }
            }
        });
    }



    public void postRequestFormBody(final Context context,FormBody body, final ApiCallback callback)
    {
        Request request = new Request.Builder().url(requestUrl).post(body).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String result = response.body().string();
                String responseCode = parseSimpleJson(result,"ResponseCode");
                if (context instanceof StartActivity)
                {
                    callback.onSuccess(result);
                }
                else if (!responseCodeHandling(context,responseCode))
                {
                    LoginUtil.updateSessionExpireTimestamp(context);
                    callback.onSuccess(result);
                }

            }
        });
    }


    public void postRequestFormBodySync(Context context,FormBody body,ApiCallback callback)
    {
        Response response;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(requestUrl).post(body).build();
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful())
            {
                String responseText = response.body().string();
                String responseCode = parseSimpleJson(responseText,"ResponseCode");
                //如果返回的结果不正确，不调用Success。维持原状，提示用户重新操作
                if (!responseCodeHandling(context,responseCode))
                {
                    LoginUtil.updateSessionExpireTimestamp(context);
                    callback.onSuccess(responseText);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String parseSimpleJson(String response, String key)
    {
        String returnValue = null;
        try {
            JSONObject object = new JSONObject(response);
            returnValue = object.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    public String parseNestedJson(String response,String outerKey,String innerKey)
    {
        String returnValue = null;
        try {
            JSONObject object = new JSONObject(response);
            JSONObject innerObject = (JSONObject)object.get(outerKey);
            returnValue = innerObject.getString(innerKey);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    public static boolean responseCodeHandling(final Context context,String responseCode)
    {
        boolean isError = false;
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if ("400010".equals(responseCode))          //令牌过期非法，重新获取令牌后再重新操作。
        {
            //1.不做判断，令牌过期再处理。2.后台每隔59分钟更新一次Token
            isError = true;
            builder.setTitle("提示");
            builder.setMessage("操作失败，请稍后再试");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Thread getTokenThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (!LoginUtil.getAccessToken(context)){}
                        }
                    });
                    getTokenThread.start();
                    try {
                        getTokenThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }else if ("400110".equals(responseCode))    //会话过期非法,重新登录
        {
            isError = true;
            builder.setTitle("提示");
            builder.setMessage("长时间未操作，请重新登录");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setClass(context, LoginActivity.class);
                    context.startActivity(intent);
                    ((Activity)context).finish();   //结束当前活动
                }
            });
        }
        if (isError)
        {
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    builder.show();
                }
            });
        }

        return isError;
    }
}