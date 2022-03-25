package com.beyond.beidou.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.beyond.beidou.BaseActivity;
import com.beyond.beidou.BuildConfig;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.api.Api;
import com.beyond.beidou.api.ApiCallback;
import com.beyond.beidou.api.ApiConfig;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AutoUpdater {
    // 下载安装包的网络路径
    private String apkUrl;

    // 保存APK的文件名
    private static final String saveFileName = "bdjc.apk";
    private static File apkFile;

    // 下载线程
    private Thread downLoadThread;
    private int progress;// 当前进度
    // 应用程序Context
    private Context mContext;
    // 是否是最新的应用,默认为false
    private boolean isNew = false;
    private boolean intercept = false;
    // 进度条与通知UI刷新的handler和msg常量
    private ProgressBar mProgress;
    private TextView txtStatus;

    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;
    private static final int SHOW_DOWN = 3;
    private static final int SHOW_NEWEST = 4;
    private static final int SHOW_ERROR = 5;

    public AutoUpdater(Context context) {
        mContext = context;
        apkFile = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), saveFileName);
    }

    public void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("软件版本更新");
        builder.setMessage("有最新的软件包，请下载并安装!");
        builder.setPositiveButton("立即下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showDownloadDialog();
            }
        });
        builder.setCancelable(false);
        if (mContext instanceof MainActivity){
            builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(mContext.getResources().getColor(R.color.main_blue));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(mContext.getResources().getColor(R.color.text_black));
    }

    public void showNewestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("软件版本更新");
        builder.setMessage("当前已是最新版本！");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(mContext.getResources().getColor(R.color.main_blue));
    }

    private void showDownloadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.update_progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.pg_update);
        txtStatus = v.findViewById(R.id.txtStatus);
        builder.setView(v);
        builder.setCancelable(false);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                intercept = true;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(mContext.getResources().getColor(R.color.text_black));
        downloadApk();
    }

    /**
     * 检查是否更新的内容
     */
    public void checkUpdate(final String paramVersion, final ApiCallback callback) {
        FormBody body = new FormBody.Builder()
                .add("AccessToken", ApiConfig.getAccessToken())
                .add("FileFormat", "apk")
                .add("FileVersion", paramVersion)
                .build();
        Api.config(ApiConfig.GET_DOWNLOAD_URL).postRequestFormBody(mContext, body, new ApiCallback() {
            @Override
            public void onSuccess(String res) {
                LogUtil.e("****GET_DOWNLOAD_URL",res);
                Api api = new Api();
                String responseCode = api.parseSimpleJson(res, "ResponseCode");
                if ("200".equals(responseCode)){
                    String downloadURL = api.parseSimpleJson(res, "DownloadURL");
                    String fileVersion = api.parseSimpleJson(res, "FileVersion");
                    if (downloadURL==null){
                        if ("".equals(paramVersion)){
                            //手动检查，返回最新安装包地址出错，打印信息
                            Message message = new Message();
                            message.what = SHOW_ERROR;
                            message.obj = "安装包下载地址为空,请稍后再试";
                            mHandler.sendMessage(message);
                            return;
                        }
                        else {
                            //自动检查时，未下架。所以直接跳过
                            LogUtil.e("*****自动检查更新：未下架","++++++++++");
                            callback.onSuccess(res);
                            //更新时间戳
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
                            ((BaseActivity)mContext).saveStringToSP("checkUpdateExpireTimestamp", String.valueOf(calendar.getTimeInMillis()));
                            LogUtil.e("checkUpdateExpireTimestamp",String.valueOf(calendar.getTimeInMillis()));
                            return;
                        }
                    }
                    String localVersion = "v"+getLocalVersionName(mContext);
                    if (!localVersion.equals(fileVersion)){
                        apkUrl = downloadURL;
                        mHandler.sendEmptyMessage(SHOW_DOWN);
                    }else {
                        if (mContext instanceof MainActivity){
                            mHandler.sendEmptyMessage(SHOW_NEWEST);
                        }
                    }
                }else {
                    //非200，打印错误信息
                    Message message = new Message();
                    message.what = SHOW_ERROR;
                    message.obj = api.parseSimpleJson(res, "ResponseMsg");
                    mHandler.sendMessage(message);
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * 从服务器下载APK安装包
     */
    public void downloadApk() {
        if (LoginUtil.isNetworkUsable(mContext)){
            downLoadThread = new Thread(DownApkWork);
            downLoadThread.start();
        }
    }

    private Runnable DownApkWork = new Runnable() {
        @Override
        public void run() {
            final Request request = new Request.Builder()
                    .url(apkUrl)
                    .get()
                    .build();
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS) //连接超时
                    .readTimeout(30, TimeUnit.SECONDS) //读超时
                    .writeTimeout(30, TimeUnit.SECONDS) //写超时
                    .build();
            final Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    LogUtil.e("检查更新App onResponse",e.toString());
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    InputStream ins = response.body().byteStream();
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    long length =  response.body().contentLength();
                    int count = 0;
                    LogUtil.e("****接口调用下载", String.valueOf(length));
                    byte[] buf = new byte[1024];
                    while (!intercept) {
                        int numread = ins.read(buf);
                        count += numread;
                        progress = (int) (((float) count / length) * 100);
                        // 下载进度
                        mHandler.sendEmptyMessage(DOWN_UPDATE);
                        if (numread <= 0) {
                            // 下载完成通知安装
                            mHandler.sendEmptyMessage(DOWN_OVER);
                            break;
                        }
                        fos.write(buf, 0, numread);
                    }
                    fos.close();
                    ins.close();
                }
            });
        }
    };

    /**
     * 安装APK内容
     */
    public void installAPK() {
        try {
            if (!apkFile.exists()) {
                return;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//安装完成后打开新版本
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 给目标应用一个临时授权
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//判断版本大于等于7.0
                //如果SDK版本>=24，即：Build.VERSION.SDK_INT >= 24，使用FileProvider兼容安装apk
//                String packageName = mContext.getApplicationContext().getPackageName();
                LogUtil.e("*****installAPK mContext  mContext.getPackageName() + \".fileprovider\"  apkFile", String.valueOf(mContext)+","+mContext.getPackageName() + ".fileprovider"+","+apkFile);
                Uri apkUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".fileprovider", apkFile);
                LogUtil.e("AuthUpDate apkUri", String.valueOf(apkUri));
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            }
            LogUtil.e("*****startActivity","startActivity");
            mContext.startActivity(intent);
            android.os.Process.killProcess(android.os.Process.myPid());//安装完之后会提示”完成” “打开”。
        } catch (Exception e) {

        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOW_DOWN:
                    showUpdateDialog();
                    break;
                case SHOW_NEWEST:
                    showNewestDialog();
                    break;
                case DOWN_UPDATE:
                    txtStatus.setText(progress + "%");
                    mProgress.setProgress(progress);
                    break;
                case DOWN_OVER:
                    Toast.makeText(mContext, "下载完毕", Toast.LENGTH_SHORT).show();
                    installAPK();
                    break;
                case SHOW_ERROR:
                    Toast.makeText(mContext, (String) msg.obj,Toast.LENGTH_SHORT).show();
                default:
                    break;
            }
        }

    };

    /**
     * 获取本地软件版本
     */
    public static String getLocalVersionName(Context ctx) {
        String localVersionName = "";
        int localVersionCode = 0;
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersionName = packageInfo.versionName;
            localVersionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
//        return localVersionName + "." + localVersionCode;
        return localVersionName;
    }

}
