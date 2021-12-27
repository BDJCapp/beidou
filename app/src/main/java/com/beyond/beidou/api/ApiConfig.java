package com.beyond.beidou.api;

public class ApiConfig {

    private static String AccessToken = "";

    public static String getAccessToken() {
        return AccessToken;
    }

    public static void setAccessToken(String accessToken) {
        AccessToken = accessToken;
    }

    public static String getSessionUUID() {
        return SessionUUID;
    }

    public static void setSessionUUID(String sessionUUID) {
        SessionUUID = sessionUUID;
    }

    public static final String FIRST_BASE_URL = "http://39.107.99.169/bdjc/API/";  //第一平台，南京
    public static final String SECOND_BASE_URL = "http://39.96.80.62/bdjc/API/";   //第二平台，北京和平里，实验室
    public static final String THIRD_BASE_URL = "http://172.18.7.86/dist/API/";    //第三平台，测试
    public static final String FOURTH_BASE_URL = "http://140.210.9.229/bdjc/API/";    //第四平台，海淀
//    public static final String FOURTH_BASE_URL = "http://140.210.9.229/test/API/";    //第四测试平台，海淀
    public static String BASE_URL = SECOND_BASE_URL;

    public static String SessionUUID = "00000000-0000-0000-0000-000000000000";
    public static final String GrantType = "BDJC";
    public static final String AppID = "UzHky82L6hOKCAsI5MBQYImw";
    public static final String AppSecret = "HCarvgfeeCQlFoWfo8lylh7aF61wNNBjv8FriEw";

    public static final String GET_ACCESS_TOKEN = "getAccessToken.php";
    public static final String GET_SESSION_UUID = "doSession.php";
    public static final String GET_PROJECTS = "getProjects.php";
    public static final String GET_STATIONS = "getStations.php";
    public static final String GET_GRAPHIC_DATA = "getGraphicData.php";
    public static final String LOGIN = "doLogin.php";
    public static final String LOGOUT = "doLogout.php";
    public static final String SET_PASSWORD = "setPassword.php";
    public static final String GET_STATION_REPORT = "getStationReport.php";
    public static final String GET_PROJECT_REPORT = "getProjectReport.php";
    public static final String GET_USER_INFORMATION = "getUserInformation.php";

    public static void setBaseUrl(String baseUrl) {
        BASE_URL = baseUrl;
    }
}
