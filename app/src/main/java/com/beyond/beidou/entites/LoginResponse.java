package com.beyond.beidou.entites;

public class LoginResponse {

    /**
     * ResponseCode : 200
     * ResponseMsg : 操作成功
     */

    private String ResponseCode;
    private String ResponseMsg;

    public String getResponseCode() {
        return ResponseCode;
    }

    public void setResponseCode(String ResponseCode) {
        this.ResponseCode = ResponseCode;
    }

    public String getResponseMsg() {
        return ResponseMsg;
    }

    public void setResponseMsg(String ResponseMsg) {
        this.ResponseMsg = ResponseMsg;
    }
}
