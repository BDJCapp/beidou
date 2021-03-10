package com.beyond.beidou.entites;

public class SessionUUIDResponse {

    /**
     * ResponseCode : 202
     * ResponseMsg : 会话请求成功
     * SessionUUID : 9105d41d-e4ce-3b05-325f-557c037732a4
     */

    private String ResponseCode;
    private String ResponseMsg;
    private String SessionUUID;

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

    public String getSessionUUID() {
        return SessionUUID;
    }

    public void setSessionUUID(String SessionUUID) {
        this.SessionUUID = SessionUUID;
    }
}
