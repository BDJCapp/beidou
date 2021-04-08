package com.beyond.beidou.my;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.baidu.mapapi.SDKInitializer;
import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;

public class SecurityFragment extends BaseFragment implements View.OnClickListener{


    private CardView mCvUpdatePwd;
    private ImageView mImageBack;

    public SecurityFragment() {
    }

    public static SecurityFragment newInstance() {
        SecurityFragment fragment = new SecurityFragment();
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
        return R.layout.fragment_security;
    }



    public void initView(View view){
        mCvUpdatePwd = view.findViewById(R.id.cv_updatePwd);
        mImageBack = view.findViewById(R.id.img_security_back);

        mCvUpdatePwd.setOnClickListener(this);
        mImageBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        MainActivity activity = (MainActivity) getActivity();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        switch (v.getId()){
            case R.id.cv_updatePwd:
                Fragment updatePwdFragment = new UpdatePwdFragment();
                activity.setUpdatePwdFragment(updatePwdFragment);
                activity.setNowFragment(updatePwdFragment);
                ft.add(R.id.layout_home, updatePwdFragment).hide(this);
                ft.addToBackStack(null);   //加入到返回栈中
                ft.commit();
                break;
            case R.id.img_security_back:
                getFragmentManager().popBackStack();
                activity.setSecurityFragment(null);
                activity.setNowFragment(activity.getMyFragment());
                break;
        }
    }
}