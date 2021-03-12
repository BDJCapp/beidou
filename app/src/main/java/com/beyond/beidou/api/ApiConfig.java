package com.beyond.beidou.api;

public class ApiConfig {

    private static String AccessToken = "";
    public static final String BASE_URL = "http://172.18.7.86/dist/API/";
    private static String SessionUUID = "00000000-0000-0000-0000-000000000000";
    public static final String GrantType = "BDJC";
    public static final String AuthorizationLable = "UzHky82L6hOKCAsI5MBQYImw";
    public static final String AuthorizationSecret = "HCarvgfeeCQlFoWfo8lylh7aF61wNNBjv8FriEw";
    public static final String GET_ACCESS_TOKEN = "getAccessToken.php";
    public static final String GET_SESSION_UUID = "doSession.php";
    public static final String LOGIN = "doLogin.php";
    public static final String GET_PROJECTS = "getProjects.php";
    public static final String GET_GRAPHIC_DATA = "getGraphicData.php";
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
}
