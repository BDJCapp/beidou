package com.beyond.beidou.data;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.util.FileUtil;
import com.beyond.beidou.util.LogUtil;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;


public class DownloadService extends Service {
    private DownloadTask downloadTask;
    private String downloadUrl;
    private DownLoadExcelSuccess downLoadExcelSuccess;
    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1,getNotification("正在导出，请稍后",progress));
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("导出成功",-1));
            downLoadExcelSuccess.showSnackBar();
//            Toast.makeText(DownloadService.this,"导出成功",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed(String reason) {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification(reason,-1));
//            Toast.makeText(DownloadService.this,reason,Toast.LENGTH_SHORT).show();
        }


        @Override
        public void onPaused() {
            downloadTask = null;
            Toast.makeText(DownloadService.this,"导出已暂停",Toast.LENGTH_SHORT).show();
        }


        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this,"导出已取消",Toast.LENGTH_SHORT).show();
        }
    };

    private DownloadBinder mBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    public class DownloadBinder extends Binder{

        public DownloadService getService()
        {
            return DownloadService.this;
        }

        public void startDownload(String url){
            if (downloadTask == null){
                downloadUrl = url;
                downloadTask = new DownloadTask(listener);
                String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                filePath = directory + fileName;
                //此时是在Service中执行该任务
                downloadTask.execute(downloadUrl);
                startForeground(1,getNotification("导出中,请稍后...",0));
            }
        }

        public void pauseDownload(){
            if (downloadTask != null){
                downloadTask.pauseDownload();
            }
        }

        public void cancleDownload()
        {
            if (downloadTask != null){
                downloadTask.cancelDownload();
            }else {
                if (downloadUrl != null){
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory + fileName);
                    if (file.exists())
                    {
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this,"Canceled",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private NotificationManager getNotificationManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title,int progress){

        //8.0以上新增,设置通知渠道否则无法展示通知。
        String CHANNEL_ONE_ID = "com.beyond.beidou";
        String CHANNEL_ONE_NAME = "Channel One";
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }


        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setChannelId(CHANNEL_ONE_ID);//新增,设置通知渠道
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if (progress > 0){
            //大于0显示进度
            builder.setContentText(progress+"%");
            builder.setProgress(100,progress,false);
        }
        return builder.build();
    }

    public interface DownLoadExcelSuccess
    {
        void showSnackBar();
    }

    public void setDownLoadExcelSuccess(DownLoadExcelSuccess downLoadExcelSuccess) {
        this.downLoadExcelSuccess = downLoadExcelSuccess;
    }
}
