package com.beyond.beidou.my;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.api.Api;
import com.beyond.beidou.api.ApiCallback;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.entites.GetUserInfoResponse;
import com.google.gson.Gson;


import java.util.HashMap;

import okhttp3.FormBody;

public class UserInfoFragment extends BaseFragment {

    private TextView mTvUserName, mTvUserPhone, mTvUserEmail, mTvUserRealName, mTvUserIDType, mTvUserID;
    private ImageView mIvBack;
    private MainActivity mMainActivity = null;
    private HashMap<String, String> cardType = new HashMap<>();

    public UserInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainActivity = (MainActivity) getActivity();
        View view = inflater.inflate(initLayout(), container, false);
        initView(view);
        getUserInfo();
        return view;
    }

    public int initLayout() {
        return R.layout.fragment_user_info;
    }

    public void initView(View view) {
        mIvBack = view.findViewById(R.id.iv_userInfo_back);
        mTvUserName = view.findViewById(R.id.tv_user_name);
        mTvUserPhone = view.findViewById(R.id.tv_user_phone);
        mTvUserEmail = view.findViewById(R.id.tv_user_email);
        mTvUserRealName = view.findViewById(R.id.tv_user_real_name);
        mTvUserIDType = view.findViewById(R.id.tv_user_IDType);
        mTvUserID = view.findViewById(R.id.tv_user_ID);

        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
                mMainActivity.setUserInfoFragment(null);
                mMainActivity.setNowFragment(mMainActivity.getMyFragment());
            }
        });

        cardType.put("0", "未知");
        cardType.put("10","居民身份证");
        cardType.put("11","居民户口簿");
        cardType.put("13","军官证");
        cardType.put("20", "港澳台通行证");
        cardType.put("30", "外国护照");
    }

    private void getUserInfo() {
        final String userName = getStringFromSP("userName");
        FormBody body = new FormBody.Builder()
                .add("SessionUUID", ApiConfig.getSessionUUID())
                .add("AccessToken", ApiConfig.getAccessToken())
                .build();
        Api.config(ApiConfig.GET_USER_INFORMATION).postRequestFormBody(getActivity(), body, new ApiCallback() {
            @Override
            public void onSuccess(String res) {
                Gson gson = new Gson();
                GetUserInfoResponse response = gson.fromJson(res, GetUserInfoResponse.class);
                if (Integer.parseInt(response.getResponseCode()) == 200) {
                    for(final GetUserInfoResponse.UserListBean user : response.getUserList()){
                        if(userName.equals(user.getUserName())){
                            mMainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTvUserName.setText(user.getUserName());
                                    mTvUserPhone.setText(user.getUserMobile());
                                    mTvUserEmail.setText(user.getUserEmail());
                                    mTvUserRealName.setText(user.getUserRealName());
                                    mTvUserIDType.setText(cardType.get(user.getUserCardType()));
                                    mTvUserID.setText(transferID(user.getUserIdCard()));
                                }
                            });
                        }
                    }
                }else{
                    mMainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("未获取到用户信息，请重新操作");
                            mIvBack.callOnClick();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

    private String transferID(String id){
        if(id.length() == 0){
            return id;
        }
        if(id.length() <= 6){
            return "XXX";
        }
        StringBuilder sb = new StringBuilder(id);
        return sb.replace(2, sb.length() - 1, "XXX").toString();
    }

}