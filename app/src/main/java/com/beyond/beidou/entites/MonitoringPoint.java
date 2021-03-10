package com.beyond.beidou.entites;

public class MonitoringPoint {
    private String name;
    private String type;
    private String activeTime;

    public MonitoringPoint(String name, String type, String activeTime) {
        this.name = name;
        this.type = type;
        this.activeTime = activeTime;
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
