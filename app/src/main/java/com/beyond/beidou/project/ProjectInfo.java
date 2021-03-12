package com.beyond.beidou.project;

import android.content.SharedPreferences;

public class ProjectInfo {
    private String projectName;

    public ProjectInfo() {
    }

    public ProjectInfo(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
