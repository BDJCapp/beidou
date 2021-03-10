package com.beyond.beidou.entites;

/**
 * @author: 李垚
 * @date: 2021/3/10
 */
public class AccessTokenResponse {

    /**
     * ResponseCode : 200
     * ResponseMsg : 操作成功
     * AccessToken : 64eb860b-2af4-451b-07a4-1976cc98f555
     * ExpireTimestamp : 1615359247
     */

    private String ResponseCode;
    private String ResponseMsg;
    private String AccessToken;
    private int ExpireTimestamp;

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

    public String getAccessToken() {
        return AccessToken;
    }

    public void setAccessToken(String AccessToken) {
        this.AccessToken = AccessToken;
    }

    public int getExpireTimestamp() {
        return ExpireTimestamp;
    }

    public void setExpireTimestamp(int ExpireTimestamp) {
        this.ExpireTimestamp = ExpireTimestamp;
    }
}
