package com.beyond.beidou;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;

import androidx.fragment.app.Fragment;

/**
 * @author: 李垚
 * @date: 2021/2/1
 */
public abstract class BaseFragment extends Fragment {



    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void initAfterSetContentView(Activity activity, View titleViewGroup) {
        if (activity == null)
            return;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
//            Window window = activity.getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (titleViewGroup == null)
                return;
            // 设置头部控件ViewGroup的PaddingTop,防止界面与状态栏重叠
            int statusBarHeight = getStatusBarHeight(activity);
            titleViewGroup.setPadding(0, statusBarHeight, 0, 0);
        }
    }
    /**
     * 获取状态栏高度
     * @param context
     * @return
     */
    private static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
