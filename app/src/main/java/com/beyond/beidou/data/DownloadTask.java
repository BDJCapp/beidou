package com.beyond.beidou.data;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.beyond.beidou.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask extends AsyncTask<String,Integer,Integer> {

    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_PAUSED = 1;
    public static final int TYPE_CANCELED = 2;
    public static final int TYPE_FAILED = 3;
    public static final int TYPE_FAILED_NO_DATA = 4;

    private DownloadListener listener;

    private boolean isCanceled = false;
    private boolean isPaused = false;
    private int lastProgress;

    public DownloadTask(DownloadListener listener)
    {
        this.listener = listener;
    }

    @Override
    protected Integer doInBackground(String... params) {
        Log.e("进入后台下载",".....");
        InputStream is = null;
        RandomAccessFile saveFile = null;
        File file = null;
            try {
            long downloadLength = 0;
            String downloadurl = params[0];
            String filename = downloadurl.substring(downloadurl.lastIndexOf("/"));
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            file = new File(directory + filename);
            LogUtil.e("文件存放地址",directory + filename);
            if (file.exists()){
                downloadLength = file.length();
            }
            LogUtil.e("资源URL",downloadurl);
            long contentLength = getContentLength(downloadurl);
            LogUtil.e("contentLength",contentLength + " ");
            if (contentLength == 0){
                return TYPE_FAILED_NO_DATA;
            }else if (contentLength == downloadLength){
                return TYPE_SUCCESS;
            }

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("RANGE","byte="+downloadLength+"-")
                    .url(downloadurl)
                    .build();

            Response response = client.newCall(request).execute();
            if (response != null){
                is = response.body().byteStream();
                saveFile = new RandomAccessFile(file,"rw");
                saveFile.seek(downloadLength);//跳过已下载的部分

                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b)) != -1)    //执行下载功能
                {
                    if (isCanceled){
                        return TYPE_CANCELED;
                    }
                    else if (isPaused){
                        return TYPE_PAUSED;
                    }
                    else {
                        total += len;
                        saveFile.write(b,0,len);
                        //计算百分比
                        int progress = (int)((total + downloadLength)*100/contentLength);
                        publishProgress(progress);
                        LogUtil.e("正在下载，已完成:",progress + " ");
                    }
                }
                response.body().close();
                LogUtil.e("下载成功","....");
                return TYPE_SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("异常错误",e.toString());
        }finally {
                try {
                    if (is != null){
                    is.close();
                   }
                    if (saveFile != null){
                        saveFile.close();
                    }
                    if (isCanceled && file != null){
                        file.delete();
                    }
            } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return TYPE_FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress){
            listener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer status) {
        switch (status){
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            case TYPE_FAILED:
                listener.onFailed("导出失败,请稍后再试");
                break;
            case TYPE_PAUSED:
                listener.onPaused();
                break;
            case TYPE_CANCELED:
                listener.onCanceled();
            case TYPE_FAILED_NO_DATA:
                listener.onFailed("当前时间段暂无报表,导出失败");
            default:
                break;
        }
    }

    public void pauseDownload(){
        isPaused = true;
    }

    public void cancelDownload(){
        isCanceled = true;
    }

    private long getContentLength(String downloadurl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadurl)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()){
            long contentLength = response.body().contentLength(); //返回下载文件的长度
            response.close();
            return contentLength;
        }
        return 0;
    }
}
