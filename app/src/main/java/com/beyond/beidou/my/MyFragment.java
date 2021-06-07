package com.beyond.beidou.my;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.baidu.mapapi.SDKInitializer;
import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.api.ApiCallback;
import com.beyond.beidou.R;
import com.beyond.beidou.api.Api;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.util.LogUtil;
import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.FormBody;


public class MyFragment extends BaseFragment implements View.OnClickListener {
    private TextView mTvUserName;
    private ImageView mIvMoreInfo;
    public static String userName = "";
    private RelativeLayout mRlHelp;
    private RelativeLayout mRlAbout;
    private CardView mCardViewSettings;
    private boolean isGetName = false;

    private final int SET_USER_NAME = 1;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case SET_USER_NAME:
                    mTvUserName.setText(String.valueOf(msg.obj));
                    saveStringToSP("userName", String.valueOf(msg.obj));
                    break;
            }

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(initLayout(), container, false);
        initView(view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUserName();
    }

    public int initLayout() {
        return R.layout.fragment_my;
    }

    public void initView(View view) {
        mTvUserName = view.findViewById(R.id.tv_username);
        mIvMoreInfo = view.findViewById(R.id.img_UserMore);
        mRlHelp = view.findViewById(R.id.img_help);
        mRlAbout = view.findViewById(R.id.img_about);
        mCardViewSettings = view.findViewById(R.id.cv_settings);
        mIvMoreInfo.setOnClickListener(this);
        mRlHelp.setOnClickListener(this);
        mRlAbout.setOnClickListener(this);
        mCardViewSettings.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        MainActivity activity = (MainActivity) getActivity();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        switch (view.getId()) {
            case R.id.cv_settings:
                if(!isGetName){
                    showToast("未获取到用户名，请稍后再试");
                    break;
                }
                Fragment settingsFragment = new SettingsFragment();
                activity.setSettingsFragment(settingsFragment);
                activity.setNowFragment(settingsFragment);
                LogUtil.e("nowFragment", activity.getNowFragment().toString());
                ft.add(R.id.layout_home, settingsFragment).hide(this);
                ft.addToBackStack(null);   //加入到返回栈中
                ft.commit();
                break;
        }
    }
    
    public boolean setUserName() {
        FormBody body = new FormBody.Builder()
                .add("SessionUUID", ApiConfig.getSessionUUID())
                .add("AccessToken", ApiConfig.getAccessToken())
                .build();

        Api.config(ApiConfig.GET_SESSION_UUID).postRequestFormBody(getActivity(), body, new ApiCallback() {
            @Override
            public void onSuccess(String res) {
                if (!TextUtils.isEmpty(res)) {
                    try {
                        JSONObject object = new JSONObject(res);
                        userName = object.getString("UserName");
                        LogUtil.e("setUserName", userName);
                        Message message = new Message();
                        message.obj = userName;
                        message.what = SET_USER_NAME;
                        handler.sendMessage(message);
                        isGetName = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    LogUtil.e("MyFragment setUserName()", "设置用户名的response为空");
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
        return isGetName;
    }
}