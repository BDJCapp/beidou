package com.beyond.beidou.my;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.api.Api;
import com.beyond.beidou.api.ApiCallback;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.entites.LoginResponse;
import com.beyond.beidou.login.LoginActivity;
import com.beyond.beidou.project.ProjectFragment;
import com.beyond.beidou.util.LoginUtil;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdatePwdFragment extends BaseFragment implements View.OnClickListener {

    private ImageView mImageBack;
    private Button mBtnConfirm;
    private EditText mEtOriginalPwd;
    private EditText mEtNewPwd;
    private EditText mEtConfirmPwd;
    private HashMap<String, Object> mParams = new HashMap<String, Object>();

    public UpdatePwdFragment() {
    }

    public static UpdatePwdFragment newInstance() {
        UpdatePwdFragment fragment = new UpdatePwdFragment();
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
        return R.layout.fragment_updatepwd;
    }


    public void initView(View view) {
        mImageBack = view.findViewById(R.id.img_updatePwd_back);
        mBtnConfirm = view.findViewById(R.id.btn_confirm);
        mEtOriginalPwd = view.findViewById(R.id.et_originalPwd);
        mEtNewPwd = view.findViewById(R.id.et_newPwd);
        mEtConfirmPwd = view.findViewById(R.id.et_confirmPwd);

        mImageBack.setOnClickListener(this);
        mBtnConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        MainActivity activity = (MainActivity) getActivity();
        String originalPwd = mEtOriginalPwd.getText().toString();
        String newPwd = mEtNewPwd.getText().toString();
        String confirmPwd = mEtConfirmPwd.getText().toString();
        AlertDialog.Builder builder;
        switch (v.getId()) {
            case R.id.img_updatePwd_back:
                getFragmentManager().popBackStack();
                activity.setUpdatePwdFragment(null);
                activity.setNowFragment(activity.getSecurityFragment());
                break;
            case R.id.btn_confirm:
                if (TextUtils.isEmpty(originalPwd) || TextUtils.isEmpty(newPwd) || TextUtils.isEmpty(confirmPwd)) {
                    builder = new AlertDialog.Builder(getContext()).setTitle("提示")
                            .setMessage("输入不能为空！").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.create().show();
                } else if (!LoginUtil.checkPwd(newPwd)) {
                    builder = new AlertDialog.Builder(getContext()).setTitle("提示")
                            .setMessage("密码格式不正确！").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.create().show();
                } else if (!newPwd.equals(confirmPwd)) {
                    builder = new AlertDialog.Builder(getContext()).setTitle("提示")
                            .setMessage("两次输入的密码不一致！").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.create().show();
                } else {
                    mParams.put("AccessToken", ApiConfig.getAccessToken());
                    mParams.put("SessionUUID", ApiConfig.getSessionUUID());
                    mParams.put("OldPassword", LoginUtil.DES3Encode(originalPwd,ApiConfig.getSessionUUID()));
                    mParams.put("NewPassword", LoginUtil.DES3Encode(newPwd,ApiConfig.getSessionUUID()));
                    Api.config(ApiConfig.SET_PASSWORD, mParams).postRequest(getContext(), new ApiCallback() {
                        @Override
                        public void onSuccess(String res) {
                            Gson gson = new Gson();
                            LoginResponse response = gson.fromJson(res, LoginResponse.class);
                            if (Integer.parseInt(response.getResponseCode()) == 400412) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog.Builder builder;
                                        builder = new AlertDialog.Builder(getContext()).setTitle("提示")
                                                .setMessage("原密码错误！").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                        builder.create().show();
                                    }
                                });
                            } else if (Integer.parseInt(response.getResponseCode()) == 200) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog.Builder builder;
                                        builder = new AlertDialog.Builder(getContext()).setTitle("提示")
                                                .setMessage("修改成功，请重新登录！").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        MainActivity activity = (MainActivity) getActivity();
                                                        dialog.dismiss();
                                                        saveStringToSP("lastProjectName", activity.getPresentProject());
                                                        ProjectFragment.isReLogin = true;
                                                        Log.e("logout  ", "position 1");
                                                        logOut(getStringFromSP("userName"), ApiConfig.getAccessToken(), ApiConfig.getSessionUUID());
                                                    }
                                                });
                                        builder.create().show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {

                        }
                    });
                }
                break;
        }
    }

    private void logOut(String userName, String AccessToken, String SessionUUID) {
        Log.e("logout  ", "position 2");
        LoginUtil.logout(getActivity(),userName, SessionUUID, AccessToken, new ApiCallback() {
            @Override
            public void onSuccess(String res) {
                try {
                    JSONObject object = new JSONObject(res);
                    String responseCode = object.getString("ResponseCode");
                    Log.e("log out response ", "logggggggg outtttt   " + responseCode);
                    switch (responseCode) {
                        case "205":
                            ApiConfig.setSessionUUID("00000000-0000-0000-0000-000000000000");
                            while (!LoginUtil.getAccessToken(getActivity())) {}
                            while (!LoginUtil.getSessionId(getActivity())){}
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {

            }

/*            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    String responseText = response.body().string();
                    JSONObject object = new JSONObject(responseText);
                    String responseCode = object.getString("ResponseCode");
                    switch (responseCode) {
                        case "205":
                            ApiConfig.setSessionUUID("00000000-0000-0000-0000-000000000000");
                            while (!LoginUtil.getAccessToken(getActivity())) {}
                            while (!LoginUtil.getSessionId(getActivity())){}
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }*/
        });
    }
}