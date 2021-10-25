package com.beyond.beidou;

public class TestBean {
    private String StationUUID;
    private String DeviceUUID;
    private String ProjectUUID;
    private String StationName;
    private String StationType;

    public TestBean() {
    }

    public TestBean(String stationUUID, String deviceUUID, String projectUUID, String stationName, String stationType) {
        StationUUID = stationUUID;
        DeviceUUID = deviceUUID;
        ProjectUUID = projectUUID;
        StationName = stationName;
        StationType = stationType;
    }

    public String getStationUUID() {
        return StationUUID;
    }

    public void setStationUUID(String stationUUID) {
        StationUUID = stationUUID;
    }

    public String getDeviceUUID() {
        return DeviceUUID;
    }

    public void setDeviceUUID(String deviceUUID) {
        DeviceUUID = deviceUUID;
    }

    public String getProjectUUID() {
        return ProjectUUID;
    }

    public void setProjectUUID(String projectUUID) {
        ProjectUUID = projectUUID;
    }

    public String getStationName() {
        return StationName;
    }

    public void setStationName(String stationName) {
        StationName = stationName;
    }

    public String getStationType() {
        return StationType;
    }

    public void setStationType(String stationType) {
        StationType = stationType;
    }

    @Override
    public String toString() {
        return "TestBean{" +
                "StationUUID='" + StationUUID + '\'' +
                ", DeviceUUID='" + DeviceUUID + '\'' +
                ", ProjectUUID='" + ProjectUUID + '\'' +
                ", StationName='" + StationName + '\'' +
                ", StationType='" + StationType + '\'' +
                '}';
    }
}
