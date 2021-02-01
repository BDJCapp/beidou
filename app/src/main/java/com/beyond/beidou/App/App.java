package com.beyond.beidou.App;

import android.app.Application;

import com.xuexiang.xui.XUI;

/**
 * 用于保存一些全局变量和方法供整个Activity使用，其实需要在AndroidManifest.xml的Application标签中，指明android:name
 * 使用getApplication获取该类实例
 */
public class App extends Application {



    @Override
    public void onCreate() {
        super.onCreate();
        XUI.init(this);
    }


}
