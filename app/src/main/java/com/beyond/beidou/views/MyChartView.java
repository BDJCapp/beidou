package com.beyond.beidou.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.beyond.beidou.util.LogUtil;
import com.beyond.beidou.util.ScreenUtil;

import lecho.lib.hellocharts.view.LineChartView;

public class MyChartView extends LineChartView {

    private float mLastY,mLastX;

    private long mStartTime,mEndTime;

    private float mSpeed;

    public MyChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent event) {
//        switch (event.getAction()){
//            case MotionEvent.ACTION_DOWN:
//                mLastY = event.getY();
//                mStartTime = System.currentTimeMillis();
//                getParent().requestDisallowInterceptTouchEvent(true);  //不允许上层ScrollView拦截事件
//                LogUtil.e("当前触摸位置 X,Y",event.getX() + "," + event.getY());
//                break;
//            case MotionEvent.ACTION_MOVE:
//                mEndTime = System.currentTimeMillis();
//                mSpeed = Math.abs(event.getY() - mLastY) / (mEndTime - mStartTime);
////                LogUtil.e("ChartFragment 滑动冲突，speed: ",mSpeed+"");
//                if (mSpeed < 1.5f) {
////                    LogUtil.e("ChartFragment 滑动冲突 ChartView处理","***");
//                    getParent().requestDisallowInterceptTouchEvent(true);
//                }else {
//                    getParent().requestDisallowInterceptTouchEvent(false);
//                }
//                break;
//        }
//        return super.dispatchTouchEvent(event);
//    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getX();
                mLastY = event.getY();
                mStartTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mLastX < 170){
//                if (mLastX < ScreenUtil.sp2px(getContext(),84)){
                    //滑动标签范围，滑动整个屏幕
                    LogUtil.e("*****","标签范围");
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                else {
//                    getParent().requestDisallowInterceptTouchEvent(true);  //不允许上层ScrollView拦截事件
                    mEndTime = System.currentTimeMillis();
                    mSpeed = Math.abs(event.getY() - mLastY) / (mEndTime - mStartTime);
                    if (mSpeed < 1.45f) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }else {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }
}
