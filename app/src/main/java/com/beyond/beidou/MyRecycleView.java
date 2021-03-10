package com.beyond.beidou;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class MyRecycleView extends RecyclerView {
    private float lastPosY,moveY;   //lastPosY:手指最后滑动到的y轴位置；moveY：手指总共滑动的垂直距离
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
                lastPosY=e.getY();
                moveY=0;
                //有待商榷
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                //如果手指往下滑动
                if(lastPosY<e.getY()){
                    if(onScroll!=null){
                        moveY+=(e.getY()-lastPosY)/2;
                        moveY=onScroll.scrollPullDown((int)moveY);
                        lastPosY=e.getY();
                        if(moveY>0)return true;
                    }
                }
                lastPosY=e.getY();
                if(moveY>0)return true;
                break;
            case MotionEvent.ACTION_UP:
                if(onScroll!=null)
                    onScroll.eventUp((int)moveY);
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