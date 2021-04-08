package com.beyond.beidou.my;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.project.ProjectFragment;
import com.beyond.beidou.project.ProjectInfo;
import com.beyond.beidou.R;
import com.beyond.beidou.api.Api;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.login.LoginActivity;
import com.beyond.beidou.util.LoginUtil;
import com.beyond.beidou.util.LogUtil;
import com.beyond.beidou.util.SPDatautils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;


public class MyFragment extends BaseFragment implements View.OnClickListener{
    private TextView textView;
    private View view;
    private Button btnQuit;
    private Intent intent;
    private LoginUtil loginUtil;
    private ImageView imgUserMore;
    private final int whatUSERNAME = 1;
    private String userName;
    private RelativeLayout RlHelp;
    private RelativeLayout RlAbout;



    public Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case whatUSERNAME:
                    textView.setText(String.valueOf(msg.obj));
//                    ProjectInfo prj = SPDatautils.getProjectInfo(getContext());
//                    textView.setText(prj.getProjectName());
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
        textView = view.findViewById(R.id.tv_username);
        imgUserMore = view.findViewById(R.id.img_UserMore);
        RlHelp = view.findViewById(R.id.img_help);
        RlAbout = view.findViewById(R.id.img_about);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = new Intent();
        loginUtil = new LoginUtil();
        setUserName();
    }

    @Override
    public void onStart() {
        super.onStart();
        btnQuit.setOnClickListener(this);
        imgUserMore.setOnClickListener(this);
        RlHelp.setOnClickListener(this);
        RlAbout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        MainActivity activity = (MainActivity) getActivity();
        switch (view.getId())
        {
            case R.id.btn_quit:
                saveStringToSP("lastProjectName", activity.getPresentProject());
                ProjectFragment.isReLogin = true;
                logout("qazXSW0",ApiConfig.getSessionUUID(), ApiConfig.getAccessToken());
                break;

            case R.id.img_help:
                Fragment helpFragment = new HelpFragment();
                activity.setHelpFragment(helpFragment);
                activity.setNowFragment(helpFragment);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.add(R.id.layout_home, helpFragment).hide(this);
                ft.addToBackStack(null);   //加入到返回栈中
                ft.commit();
//                Intent intent = new Intent(getActivity(),HelpActivity.class);
//                startActivity(intent);
                break;

            case R.id.img_about:
                Fragment aboutFragment = new AboutFragment();
                MainActivity activity2 = (MainActivity) getActivity();
                activity2.setAboutFragment(aboutFragment);
                activity2.setNowFragment(aboutFragment);
                FragmentManager fragmentManager2 = getFragmentManager();
                FragmentTransaction ft2 = fragmentManager2.beginTransaction();
                ft2.add(R.id.layout_home, aboutFragment).hide(this);
                ft2.addToBackStack(null);   //加入到返回栈中
                ft2.commit();
                break;
//            case R.id.img_UserMore:
//                intent.setClass(getActivity(), ShowUserInfoActivity.class);
//                intent.putExtra("userName",userName);
//                startActivity(intent);
//                break;
        }
    }

    public void logout(String Username, final String SessionUUID, String AccessToken)
    {

        loginUtil.logout("qazXSW0",  SessionUUID, AccessToken, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //网络请求失败

            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.e("退出的response", String.valueOf(response));
                String responseText = response.body().string();
                if (!TextUtils.isEmpty(responseText))
                {
                    try {
                        Message message = new Message();
                        JSONObject object = new JSONObject(responseText);
                        String errCode = object.getString("ResponseCode");
                        String errMsg = object.getString("ResponseMsg");
                        Log.e("退出的response",responseText);
                        //获取json中的code。json是包含很多数据，这里只是单拿出其中的code吗
                        switch (errCode){
                            case "205": //退出成功
                                ApiConfig.setSessionUUID("00000000-0000-0000-0000-000000000000");
                                Intent intent= new Intent(getActivity(),LoginActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    public void setUserName()
    {
        FormBody body = new FormBody.Builder()
                .add("SessionUUID", ApiConfig.getSessionUUID())
                .add("AccessToken", ApiConfig.getAccessToken())
                .build();

        Api.config(ApiConfig.GET_SESSION_UUID).postRequestFormBody( body, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                LogUtil.e("设置用户名请求失败", "错误原因"+e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();
                LogUtil.e("SessionUUID再次请求成功,内容为",responseText);
                if (!TextUtils.isEmpty(responseText)){
                    try {
                        JSONObject object = new JSONObject(responseText);
                        userName = object.getString("UserName");
                        LogUtil.e("setUserName",userName);
                        Message message = new Message();
                        message.obj = userName;
                        message.what = whatUSERNAME;
                        handler.sendMessage(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    LogUtil.e("MyFragment setUserName()","设置用户名的response为空");
                }
            }
        });
    }
}