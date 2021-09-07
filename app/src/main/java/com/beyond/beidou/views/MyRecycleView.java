package com.beyond.beidou.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class MyRecycleView extends RecyclerView {
    private float mLastPosY, mMoveY;   //mLastPosY:手指最后滑动到的y轴位置；mMoveY：手指总共滑动的垂直距离
    public MyRecycleView(@NonNull Context context) {
        super(context);
    }

    public MyRecycleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRecycleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }



    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastPosY =e.getY();
                mMoveY = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                mLastPosY =e.getY();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        return super.onTouchEvent(e);
    }

    public interface OnScroll{
        //向下滑动时回调
        float scrollPullDown(int dy);
        //手指离开屏幕时回调
        void eventUp(int dy);
    }
    private OnScroll onScroll;
    public void setOnScroll(OnScroll onScroll){
        this.onScroll=onScroll;
    }
}