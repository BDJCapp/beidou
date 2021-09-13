package com.beyond.beidou.my;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.api.ApiCallback;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.login.LoginActivity;
import com.beyond.beidou.project.ProjectFragment;
import com.beyond.beidou.util.LoginUtil;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsFragment extends BaseFragment implements View.OnClickListener{

    private Button mBtnQuit;
    private ImageView mImageBack;
    private CardView mCardViewSettings;
    private ZLoadingDialog dialog;

    public SettingsFragment() {
        // Required empty public constructor
    }


    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(initLayout(), container, false);
        initView(view);
        return view;
    }

    public int initLayout() {
        return R.layout.fragment_settings;
    }

    public void initView(View view){
        mBtnQuit = view.findViewById(R.id.btn_quit);
        mImageBack = view.findViewById(R.id.img_settings_back);
        mCardViewSettings = view.findViewById(R.id.cv_security);

        mBtnQuit.setOnClickListener(this);
        mImageBack.setOnClickListener(this);
        mCardViewSettings.setOnClickListener(this);
        dialog = new ZLoadingDialog(getActivity());
    }

    @Override
    public void onClick(View v) {
        MainActivity activity = (MainActivity) getActivity();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        switch (v.getId()){
            case R.id.img_settings_back:
                getFragmentManager().popBackStack();
                activity.setSettingsFragment(null);
                activity.setNowFragment(activity.getMyFragment());
                break;
            case R.id.cv_security:
                Fragment securityFragment = new SecurityFragment();
                activity.setSecurityFragment(securityFragment);
                activity.setNowFragment(securityFragment);
                ft.add(R.id.layout_home, securityFragment).hide(this);
                ft.addToBackStack(null);   //加入到返回栈中
                ft.commit();
                break;
            case R.id.btn_quit:
                saveStringToSP("lastProjectName", activity.getPresentProject());
                ProjectFragment.sIsReLogin = true;
                if("".equals(MyFragment.userName)){
                    showToast("用户名未加载出来");
                    return;
                }
                dialog.setLoadingBuilder(Z_TYPE.ROTATE_CIRCLE)//设置类型
                        .setLoadingColor(Color.BLACK)//颜色
                        .setHintText("Loading...")
                        .setCancelable(false)
                        .show();
                logout(MyFragment.userName, ApiConfig.getSessionUUID(), ApiConfig.getAccessToken());
                break;
        }
    }

    public void logout(String Username, final String SessionUUID, String AccessToken)
    {
        LoginUtil.logout(getActivity(),Username,  SessionUUID, AccessToken, new ApiCallback() {

            @Override
            public void onSuccess(String res) {
                if (!TextUtils.isEmpty(res))
                {
                    try {
                        saveStringToSP("SessionExpireTimestamp","");
                        JSONObject object = new JSONObject(res);
                        String responseCode = object.getString("ResponseCode");
                        switch (responseCode){
                            case "205": //退出成功
                                Intent intent= new Intent(getActivity(), LoginActivity.class);
                                dialog.dismiss();
                                startActivity(intent);
                                getActivity().finish();
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {

            }

        });
    }
}