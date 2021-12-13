package com.beyond.beidou.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import lecho.lib.hellocharts.view.LineChartView;

public class MyChartView extends LineChartView {

    private float mLastY;

    private long mStartTime,mEndTime;

    private float mSpeed;

    public MyChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastY = event.getY();
                mStartTime = System.currentTimeMillis();
                getParent().requestDisallowInterceptTouchEvent(true);  //不允许上层ScrollView拦截事件
                break;
            case MotionEvent.ACTION_MOVE:
                mEndTime = System.currentTimeMillis();
                mSpeed = Math.abs(event.getY() - mLastY) / (mEndTime - mStartTime);
//                LogUtil.e("ChartFragment 滑动冲突，speed: ",mSpeed+"");
                if (mSpeed < 1.5f) {
//                    LogUtil.e("ChartFragment 滑动冲突 ChartView处理","***");
                    getParent().requestDisallowInterceptTouchEvent(true);
                }else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }
}
