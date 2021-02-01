package com.beyond.beidou.util;

/**
 * @author: 李垚
 * @date: 2020/12/28
 */
public class APIUtil {
    private static final String baseUrl = "http://39.96.80.62/bdjc/API/";
    private static final String doSessionUrl = baseUrl + "doSession.php";
    private static final String getImageCodeUrl = baseUrl + "getImageCode.php";
    private static final String doLoginUrl = baseUrl + "doLogin.php";
    private static final String getProjectUrl = baseUrl + "getProject.php";
    private static final String getRealNameUserUrl = baseUrl + "getRealNameUser.php";
    private static final String getDeviceUrl = baseUrl + "getDevice.php";



    private static String sessionUUID = "00000000-0000-0000-0000-000000000000";

    public static String getGetRealNameUserUrl() {
        return getRealNameUserUrl;
    }

    public static String getDoSessionUrl() {
        return doSessionUrl;
    }
    public static String getGetImageCodeUrl() {
        return getImageCodeUrl;
    }

    public static String getSessionUUID() {
        return sessionUUID;
    }

    public static void setSessionUUID(String sessionUUID) {
        APIUtil.sessionUUID = sessionUUID;
    }
    public static String getDoLoginUrl() {
        return doLoginUrl;
    }

    public static String getGetProjectUrl() {
        return getProjectUrl;
    }
    public static String getBaseUrl() {
        return baseUrl;
    }

    public static String getGetDeviceUrl() {
        return getDeviceUrl;
    }
}
