package com.beyond.beidou.entites;

public class MonitoringPoint {
    private String name;
    private String type;
    private String activeTime;
    private String status;

    public MonitoringPoint(String name, String type, String activeTime, String status) {
        this.name = name;
        this.type = type;
        this.activeTime = activeTime;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(String activeTime) {
        this.activeTime = activeTime;
    }
}
