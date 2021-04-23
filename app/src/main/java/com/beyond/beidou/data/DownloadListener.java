package com.beyond.beidou.data;

public interface DownloadListener {
    void onProgress(int progress);  //用于通知当前下载进度
    void onSuccess();
    void onFailed(String reason);
    void onPaused();
    void onCanceled();
}
