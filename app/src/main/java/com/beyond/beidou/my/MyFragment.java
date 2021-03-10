package com.beyond.beidou.my;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.beyond.beidou.R;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.login.LoginActivity;
import com.beyond.beidou.util.LoginUtil;
import com.beyond.beidou.util.LogUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

/**
 * @author: 李垚
 * @date: 2020/12/21
 */
public class MyFragment extends Fragment implements View.OnClickListener{
    private TextView textView;
    private View view;
    private Button btnQuit;
    private Intent intent;
    private LoginUtil loginUtil;
    private ImageView imgUserMore;
    private final int whatUSERNAME = 1;
    private String userName;

    public Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case whatUSERNAME:
                    textView.setText(String.valueOf(msg.obj));
                    break;
            }

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        /*这个false不可省略，否则当添加fragment时会出现
        java.lang.IllegalStateException: The specified child already has a parent. You must call removeView() on the child’s parent first*/
        view = inflater.inflate(R.layout.fragment_my,container,false);
        btnQuit = view.findViewById(R.id.btn_quit);
        textView = view.findViewById(R.id.tv_account);
        imgUserMore = view.findViewById(R.id.img_UserMore);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = new Intent();
        loginUtil = new LoginUtil();
    }

    @Override
    public void onStart() {
        super.onStart();
        btnQuit.setOnClickListener(this);
        imgUserMore.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btn_quit:
                break;
//            case R.id.img_UserMore:
//                intent.setClass(getActivity(), ShowUserInfoActivity.class);
//                intent.putExtra("userName",userName);
//                startActivity(intent);
//                break;
        }
    }
}

