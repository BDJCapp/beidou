package com.beyond.beidou.api;

/**
 * 可以增加多种情况
 */
public interface ApiCallback {

    void onSuccess(String res);

    void onFailure(Exception e);

}
