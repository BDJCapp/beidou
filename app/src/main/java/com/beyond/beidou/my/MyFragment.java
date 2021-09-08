package com.beyond.beidou.my;

import android.os.Bundle;
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

import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;


public class MyFragment extends BaseFragment implements View.OnClickListener {
    private TextView mTvUserName;
    private ImageView mIvUserInfo;
    public static String userName = "";
    private RelativeLayout mRlHelp;
    private RelativeLayout mRlAbout;
    private CardView mCardViewSettings;
    private boolean isGetName = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(initLayout(), container, false);
        initView(view);
        userName = getStringFromSP("userName");
        isGetName = true;
        mTvUserName.setText(userName);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public int initLayout() {
        return R.layout.fragment_my;
    }

    public void initView(View view) {
        mTvUserName = view.findViewById(R.id.tv_user_name);
        mIvUserInfo = view.findViewById(R.id.img_user);
        mRlHelp = view.findViewById(R.id.img_help);
        mRlAbout = view.findViewById(R.id.img_about);
        mCardViewSettings = view.findViewById(R.id.cv_settings);
        mIvUserInfo.setOnClickListener(this);
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
                ft.add(R.id.layout_home, settingsFragment).hide(this);
                ft.addToBackStack("MyFragment");   //加入到返回栈中
                ft.commit();
                break;
            case R.id.img_user:
                if(!isGetName){
                    showToast("未获取到用户名，请稍后再试");
                    break;
                }
                Fragment userInfoFragment = new UserInfoFragment();
                activity.setUserInfoFragment(userInfoFragment);
                activity.setNowFragment(userInfoFragment);
                ft.add(R.id.layout_home, userInfoFragment).hide(this);
                ft.addToBackStack("UserInfoFragment");   //加入到返回栈中
                ft.commit();
                break;
        }
    }
}