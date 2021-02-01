package com.beyond.beidou.util;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @author: 李垚
 * @date: 2020/12/22
 */
public class HttpUtil {
    public static void sendOkHttpRequestFormBody(String url, FormBody body, okhttp3.Callback callback)
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).post(body).build();
        client.newCall(request).enqueue(callback);//回调对象？
    }

    public static void sendOkHttpRequestRequestBody(String url, RequestBody body, okhttp3.Callback callback)
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).post(body).build();
        client.newCall(request).enqueue(callback);
    }

    public static String parseJSONObject(String response,String key)
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
}
