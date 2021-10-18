package com.beyond.beidou.entites;

import java.util.UUID;

public class MonitoringPoint {
    private String name;
    private String type;
    private String activeTime;
    private String status;
    private String uuid;

    public MonitoringPoint(String name, String type, String activeTime, String status, String uuid) {
        this.name = name;
        this.type = type;
        this.activeTime = activeTime;
        this.status = status;
        this.uuid = uuid;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
