package com.beyond.beidou.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.beyond.beidou.project.ProjectInfo;

/**
 * 使用sharepreference实现数据存储的工具类
 * */

public class SPDatautils {
    private static final String mFilename = "mydata";

    /**
     * 保存最后一次的项目名
     * @param context
     * @param projectName
     * @return
     */
    public static boolean saveprojectName (Context context,String projectName){
        boolean flag = false;
        SharedPreferences sp = context.getSharedPreferences(mFilename,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("projectName",projectName);
        editor.commit();
        flag = true;
        return flag;

    }

    /**
     *获取项目名方法
     */
    public static ProjectInfo getProjectInfo(Context context){
        ProjectInfo prj = null;
        SharedPreferences sp = context.getSharedPreferences(mFilename,Context.MODE_PRIVATE);
        String projectName = sp.getString("projectName","Null");

        prj = new ProjectInfo();
        prj.setProjectName(projectName);
        return prj;
    }
}
