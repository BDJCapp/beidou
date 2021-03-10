package com.beyond.beidou.api;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Api {
    private static OkHttpClient client;
    private static String requestUrl;
    private static Map<String, Object> mParams;

    public static Api api = new Api();

    public Api(){

    }

    public static Api config(String url, HashMap<String, Object> params){
        client = new OkHttpClient.Builder()
                .build();
        requestUrl = ApiConfig.BASE_URL + url;
        mParams = params;
        return api;
    }

    public static Api config(String url){
        client = new OkHttpClient.Builder()
                .build();
        requestUrl = ApiConfig.BASE_URL + url;
        return api;
    }

    public void postRequest(Context context, final ApiCallback callback){
        JSONObject jsonObject = new JSONObject(mParams);
        String jsonStr = jsonObject.toString();
        RequestBody requestBodyJson =
                RequestBody.create(jsonStr, MediaType.parse("application/json;charset=utf-8")
                        );
        Request request = new Request.Builder()
                .url(requestUrl)
                .post(requestBodyJson)
                .build();
        final Call call = client.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.getMessage());
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                callback.onSuccess(result);
            }
        });
    }

    public String postRequestSync(Context context){
        JSONObject jsonObject = new JSONObject(mParams);
        String jsonStr = jsonObject.toString();
        RequestBody requestBodyJson =
                RequestBody.create(jsonStr, MediaType.parse("application/json;charset=utf-8")
                );
        Request request = new Request.Builder()
                .url(requestUrl)
                .post(requestBodyJson)
                .build();
        final Call call = client.newCall(request);
        try {
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public  void postRequestFormBody(FormBody body, okhttp3.Callback callback)
    {
        Request request = new Request.Builder().url(requestUrl).post(body).build();
        client.newCall(request).enqueue(callback);
    }

    public  void postRequestFormBodySync(FormBody body, ApiCallback callback)
    {
        Response response;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(requestUrl).post(body).build();
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful())
            {
                callback.onSuccess(response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String parseJSONObject(String response,String key)
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
