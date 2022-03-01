package com.beyond.beidou.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class ScreenUtil {

    /**
     * 获取屏幕宽
     * @param activity
     * @return
     */
    public static int getScreenXRatio(Activity activity){
        DisplayMetrics dm = new DisplayMetrics();
        dm = activity.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 获取屏幕
     * @param activity
     * @return
     */
    public static int getScreenYRatio(Activity activity){
        DisplayMetrics dm = new DisplayMetrics();
        dm = activity.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    /**
     * 获取屏幕内容高度
     *
     * @param activity
     * @return
     */
    public static int getScreenHeight(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int result = 0;
        int resourceId = activity.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        int screenHeight = dm.heightPixels - result;
        return screenHeight;
    }

    /**
     * dp转px
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);

    }

    /**
     * px转dp
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static float cm2px(Context context, float cmValue) {
        final float scale = context.getResources().getDisplayMetrics().densityDpi;
        return (float) (cmValue * 25.4 / scale);
    }

    /**
     * sp转换成px
     */
    public static int sp2px(Context context,float spValue){
        float fontScale=context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue*fontScale+0.5f);
    }
}
