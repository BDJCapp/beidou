package com.beyond.beidou.my;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.api.ApiCallback;
import com.beyond.beidou.util.AutoUpdater;


public class MyFragment extends BaseFragment implements View.OnClickListener {
    private TextView mTvUserName;
    private TextView mTvVersion;
    private ImageView mIvUserInfo;
    public static String userName = "";
    private CardView mCardViewSettings;
    private CardView mCardViewFile;
    private CardView mCardViewUpdateApp;
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

    public int initLayout() {
        return R.layout.fragment_my;
    }

    public void initView(View view) {
        mTvUserName = view.findViewById(R.id.tv_user_name);
        mTvVersion = view.findViewById(R.id.tv_version);
        mIvUserInfo = view.findViewById(R.id.img_user);
        mCardViewSettings = view.findViewById(R.id.cv_settings);
        mCardViewFile = view.findViewById(R.id.cv_file);
        mCardViewUpdateApp = view.findViewById(R.id.cv_updateApp);
        mIvUserInfo.setOnClickListener(this);
        mCardViewSettings.setOnClickListener(this);
        mCardViewFile.setOnClickListener(this);
        mCardViewUpdateApp.setOnClickListener(this);

        mTvVersion.setText("Ver "+AutoUpdater.getLocalVersionName(getActivity()));
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
                activity.setExit(false);
                ft.add(R.id.layout_home, settingsFragment).hide(this);
//                ft.addToBackStack(null);   //加入到返回栈中
                ft.commit();
                break;
            case R.id.cv_file:
                if(!isGetName){
                    showToast("未获取到用户名，请稍后再试");
                    break;
                }
                Fragment fileManageFragment = new FileManageFragment();
                activity.setFileManageFragment(fileManageFragment);
                activity.setNowFragment(fileManageFragment);
                activity.setExit(false);
                ft.add(R.id.layout_home, fileManageFragment).hide(this);
//                ft.addToBackStack(null);   //加入到返回栈中
                ft.commit();
                break;
            case R.id.cv_updateApp:
                AutoUpdater autoUpdater = new AutoUpdater(getContext());
                autoUpdater.checkUpdate("", new ApiCallback() {
                    @Override
                    public void onSuccess(String res) {

                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });
                break;
            case R.id.img_user:
                if(!isGetName){
                    showToast("未获取到用户名，请稍后再试");
                    break;
                }
                Fragment userInfoFragment = new UserInfoFragment();
                activity.setUserInfoFragment(userInfoFragment);
                activity.setNowFragment(userInfoFragment);
                activity.setExit(false);
                ft.add(R.id.layout_home, userInfoFragment).hide(this);
//                ft.addToBackStack(null);   //加入到返回栈中
                ft.commit();
                break;
        }
    }

}