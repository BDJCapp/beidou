package com.beyond.beidou.entites;

import java.util.List;

public class GetUserInfoResponse {

    /**
     * ResponseCode : 200
     * ResponseMsg : 操作成功
     * PageInfo : {"PageFlag":"UserCreateTime","PageNumber":1,"PageSize":10,"TotalNumber":2}
     * UserList : [{"UserUUID":"263109a6-f4f2-d07e-e985-e7d223d13d8a","CompanyUserUUID":"cc775619-7beb-61c3-e192-7018f776f554","UserName":"Admin123456","Password":"qwertyuiiopASDFG5*","UserMobile":"13800138000","UserEmail":"","UserNickName":"","UserRealName":"管理员","UserCardType":"0","UserIdCard":"","UserStatus":"1","UserCreateTime":"2020-01-01 00:00:00","UserModifyTime":"2021-04-27 09:50:21","UserLastTime":"2021-06-22 14:07:04","UserPwdExpireTime":"2021-07-26 09:50:21"},{"UserUUID":"bd898b38-7d2d-5335-5fb2-30a74bb814f1","CompanyUserUUID":"6031ece9-7b50-df5a-7645-3fcdddaa4063","UserName":"qwerASD5","Password":"qwertyuiiopASDFG5*","UserMobile":"13800000000","UserEmail":"","UserNickName":"","UserRealName":"用户","UserCardType":"0","UserIdCard":"","UserStatus":"1","UserCreateTime":"2020-01-01 00:00:00","UserModifyTime":"2021-05-31 18:44:49","UserLastTime":"2021-07-07 14:32:01","UserPwdExpireTime":"2022-05-31 18:44:49"}]
     */

    private String ResponseCode;
    private String ResponseMsg;
    private PageInfoBean PageInfo;
    private List<UserListBean> UserList;

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

    public PageInfoBean getPageInfo() {
        return PageInfo;
    }

    public void setPageInfo(PageInfoBean PageInfo) {
        this.PageInfo = PageInfo;
    }

    public List<UserListBean> getUserList() {
        return UserList;
    }

    public void setUserList(List<UserListBean> UserList) {
        this.UserList = UserList;
    }

    public static class PageInfoBean {
        /**
         * PageFlag : UserCreateTime
         * PageNumber : 1
         * PageSize : 10
         * TotalNumber : 2
         */

        private String PageFlag;
        private int PageNumber;
        private int PageSize;
        private int TotalNumber;

        public String getPageFlag() {
            return PageFlag;
        }

        public void setPageFlag(String PageFlag) {
            this.PageFlag = PageFlag;
        }

        public int getPageNumber() {
            return PageNumber;
        }

        public void setPageNumber(int PageNumber) {
            this.PageNumber = PageNumber;
        }

        public int getPageSize() {
            return PageSize;
        }

        public void setPageSize(int PageSize) {
            this.PageSize = PageSize;
        }

        public int getTotalNumber() {
            return TotalNumber;
        }

        public void setTotalNumber(int TotalNumber) {
            this.TotalNumber = TotalNumber;
        }
    }

    public static class UserListBean {
        /**
         * UserUUID : 263109a6-f4f2-d07e-e985-e7d223d13d8a
         * CompanyUserUUID : cc775619-7beb-61c3-e192-7018f776f554
         * UserName : Admin123456
         * Password : qwertyuiiopASDFG5*
         * UserMobile : 13800138000
         * UserEmail :
         * UserNickName :
         * UserRealName : 管理员
         * UserCardType : 0
         * UserIdCard :
         * UserStatus : 1
         * UserCreateTime : 2020-01-01 00:00:00
         * UserModifyTime : 2021-04-27 09:50:21
         * UserLastTime : 2021-06-22 14:07:04
         * UserPwdExpireTime : 2021-07-26 09:50:21
         */

        private String UserUUID;
        private String CompanyUserUUID;
        private String UserName;
        private String Password;
        private String UserMobile;
        private String UserEmail;
        private String UserNickName;
        private String UserRealName;
        private String UserCardType;
        private String UserIdCard;
        private String UserStatus;
        private String UserCreateTime;
        private String UserModifyTime;
        private String UserLastTime;
        private String UserPwdExpireTime;

        public String getUserUUID() {
            return UserUUID;
        }

        public void setUserUUID(String UserUUID) {
            this.UserUUID = UserUUID;
        }

        public String getCompanyUserUUID() {
            return CompanyUserUUID;
        }

        public void setCompanyUserUUID(String CompanyUserUUID) {
            this.CompanyUserUUID = CompanyUserUUID;
        }

        public String getUserName() {
            return UserName;
        }

        public void setUserName(String UserName) {
            this.UserName = UserName;
        }

        public String getPassword() {
            return Password;
        }

        public void setPassword(String Password) {
            this.Password = Password;
        }

        public String getUserMobile() {
            return UserMobile;
        }

        public void setUserMobile(String UserMobile) {
            this.UserMobile = UserMobile;
        }

        public String getUserEmail() {
            return UserEmail;
        }

        public void setUserEmail(String UserEmail) {
            this.UserEmail = UserEmail;
        }

        public String getUserNickName() {
            return UserNickName;
        }

        public void setUserNickName(String UserNickName) {
            this.UserNickName = UserNickName;
        }

        public String getUserRealName() {
            return UserRealName;
        }

        public void setUserRealName(String UserRealName) {
            this.UserRealName = UserRealName;
        }

        public String getUserCardType() {
            return UserCardType;
        }

        public void setUserCardType(String UserCardType) {
            this.UserCardType = UserCardType;
        }

        public String getUserIdCard() {
            return UserIdCard;
        }

        public void setUserIdCard(String UserIdCard) {
            this.UserIdCard = UserIdCard;
        }

        public String getUserStatus() {
            return UserStatus;
        }

        public void setUserStatus(String UserStatus) {
            this.UserStatus = UserStatus;
        }

        public String getUserCreateTime() {
            return UserCreateTime;
        }

        public void setUserCreateTime(String UserCreateTime) {
            this.UserCreateTime = UserCreateTime;
        }

        public String getUserModifyTime() {
            return UserModifyTime;
        }

        public void setUserModifyTime(String UserModifyTime) {
            this.UserModifyTime = UserModifyTime;
        }

        public String getUserLastTime() {
            return UserLastTime;
        }

        public void setUserLastTime(String UserLastTime) {
            this.UserLastTime = UserLastTime;
        }

        public String getUserPwdExpireTime() {
            return UserPwdExpireTime;
        }

        public void setUserPwdExpireTime(String UserPwdExpireTime) {
            this.UserPwdExpireTime = UserPwdExpireTime;
        }
    }
}
