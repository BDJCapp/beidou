package com.beyond.beidou.api;

public interface ApiCallback {

    void onSuccess(String res);

    void onFailure(Exception e);

}
