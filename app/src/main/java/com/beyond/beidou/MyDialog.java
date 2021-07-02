package com.beyond.beidou;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author: 李垚
 * @date: 2021/5/18
 */
public class MyDialog extends Dialog {


    //显示的标题
    private TextView mTitleTv;

    private TextView mStartTimeTv, mEndTimeTv;

    //确认和取消按钮
    private Button mNegativeBtn, mPositiveBtn;


    public MyDialog(Context context) {
        super(context, R.style.CustomDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog);
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);
        //初始化界面控件
        initView();
        //初始化界面控件的事件
        initEvent();
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        mPositiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( onClickBottomListener!= null) {
                    onClickBottomListener.onPositiveClick();
                }
            }
        });
        //设置取消按钮被点击后，向外界提供监听
        mNegativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( onClickBottomListener!= null) {
                    onClickBottomListener.onNegativeClick();
                }
            }
        });

        mStartTimeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickTextViewListener != null)
                {
                    onClickTextViewListener.onStartTimeClick(v);
                }
            }
        });

        mEndTimeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickTextViewListener != null)
                {
                    onClickTextViewListener.onEndTimeClick(v);
                }
            }
        });
    }


    @Override
    public void show() {
        super.show();
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        mNegativeBtn = findViewById(R.id.negtive);
        mPositiveBtn = findViewById(R.id.positive);
        mTitleTv = findViewById(R.id.title);
        mStartTimeTv = findViewById(R.id.tv_startTime);
        mEndTimeTv = findViewById(R.id.tv_endTime);
    }

    /**
     * 设置确定取消按钮的回调
     */
    public OnClickBottomListener onClickBottomListener;
    public MyDialog setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
        this.onClickBottomListener = onClickBottomListener;
        return this;
    }
    public interface OnClickBottomListener{
        /**
         * 点击确定按钮事件
         */
        void onPositiveClick();
        /**
         * 点击取消按钮事件
         */
        void onNegativeClick();
    }

    /**
     * 设置开始时间和结束时间的回调
     */
    public OnClickTextViewListener onClickTextViewListener;
    public MyDialog setOnClickTextViewListener(OnClickTextViewListener onClickTextViewListener) {
        this.onClickTextViewListener = onClickTextViewListener;
        return this;
    }
    public interface OnClickTextViewListener{
        /**
         * 点击开始时间事件
         */
        void onStartTimeClick(View v);
        /**
         * 点击结束时间事件
         */
        void onEndTimeClick(View v);
    }

    public String getStartTime(){
        return (String) mStartTimeTv.getText();
    }

    public String getEndTime(){
        return (String) mEndTimeTv.getText();
    }

}
