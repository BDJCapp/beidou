package com.beyond.beidou.my;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class UpdatePwdFragment extends BaseFragment implements View.OnClickListener {

    private ImageView mImageBack;
    private Button mBtnConfirm;
    private EditText mEtOriginalPwd;
    private EditText mEtNewPwd;
    private EditText mEtConfirmPwd;
    private HashMap<String, Object> mParams = new HashMap<String, Object>();
    private Activity mMainActivity = null;

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
        mMainActivity = getActivity();
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
        final MainActivity activity = (MainActivity) getActivity();
        String originalPwd = mEtOriginalPwd.getText().toString();
        String newPwd = mEtNewPwd.getText().toString();
        String confirmPwd = mEtConfirmPwd.getText().toString();
        AlertDialog dialog = null;
        switch (v.getId()) {
            case R.id.img_updatePwd_back:
                getFragmentManager().popBackStack();
                activity.setUpdatePwdFragment(null);
                activity.setNowFragment(activity.getSecurityFragment());
                break;
            case R.id.btn_confirm:
                if (TextUtils.isEmpty(originalPwd) || TextUtils.isEmpty(newPwd) || TextUtils.isEmpty(confirmPwd)) {
                    dialog = new AlertDialog.Builder(getContext()).setTitle("提示")
                            .setMessage("输入不能为空！").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setCancelable(false).create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#0075E3"));
                } else if (!newPwd.equals(confirmPwd)) {
                    dialog = new AlertDialog.Builder(getContext()).setTitle("提示")
                            .setMessage("两次输入的密码不一致！").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setCancelable(false).create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#0075E3"));
                } else {
                    mParams.put("AccessToken", ApiConfig.getAccessToken());
                    mParams.put("SessionUUID", ApiConfig.getSessionUUID());
                    mParams.put("OldPassword", LoginUtil.DES3Encode(originalPwd, ApiConfig.getSessionUUID()));
                    mParams.put("NewPassword", LoginUtil.DES3Encode(newPwd, ApiConfig.getSessionUUID()));
                    Api.config(ApiConfig.SET_PASSWORD, mParams).postRequest(getContext(), new ApiCallback() {
                        @Override
                        public void onSuccess(String res) {
                            Gson gson = new Gson();
                            final LoginResponse response = gson.fromJson(res, LoginResponse.class);
                            if (Integer.parseInt(response.getResponseCode()) == 400412) {
                                mMainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog dialog1= new AlertDialog.Builder(getContext()).setTitle("提示")
                                                .setMessage("原密码错误！").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }).create();
                                        dialog1.show();
                                        dialog1.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#0075E3"));
                                    }
                                });
                            }else if (Integer.parseInt(response.getResponseCode()) == 200) {
                                mMainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog dialog2 = new AlertDialog.Builder(getContext()).setTitle("提示").setCancelable(false)
                                                .setMessage("修改成功，请重新登录！").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        MainActivity activity = (MainActivity) getActivity();
                                                        dialog.dismiss();
                                                        saveStringToSP("lastProjectName", activity.getPresentProject());
                                                        ProjectFragment.sIsReLogin = true;
                                                        logOut(getStringFromSP("userName"), ApiConfig.getAccessToken(), ApiConfig.getSessionUUID());
                                                    }
                                                }).create();
                                        dialog2.show();
                                        dialog2.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#0075E3"));
                                    }
                                });
                            }
                            else if (Integer.parseInt(response.getResponseCode()) == 400) {
                                mMainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog dialog = new AlertDialog.Builder(getContext()).setTitle("提示")
                                                .setMessage("密码太简单，请重新修改！").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }).create();
                                        dialog.show();
                                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#0075E3"));
                                    }
                                });
                            }
                            else{
                                mMainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog dialog = new AlertDialog.Builder(mMainActivity).setTitle("提示")
                                                .setMessage(response.getResponseMsg()).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }).create();
                                        dialog.show();
                                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#0075E3"));
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
        LoginUtil.logout(mMainActivity, userName, SessionUUID, AccessToken, new ApiCallback() {
            @Override
            public void onSuccess(String res) {
                try {

                    JSONObject object = new JSONObject(res);
                    String responseCode = object.getString("ResponseCode");
                    Log.e("log out response ", responseCode);
                    ApiConfig.setSessionUUID("00000000-0000-0000-0000-000000000000");
                    while (!LoginUtil.getAccessToken(mMainActivity)) {
                    }
                    while (!LoginUtil.getSessionId(mMainActivity)) {
                    }
                    Intent intent = new Intent(mMainActivity, LoginActivity.class);
                    startActivity(intent);
                    mMainActivity.finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {

            }

        });
    }
}