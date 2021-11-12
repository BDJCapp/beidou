package com.beyond.beidou.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.beyond.beidou.BaseActivity;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.api.ApiCallback;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.util.LogUtil;
import com.beyond.beidou.util.LoginUtil;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends BaseActivity implements View.OnClickListener{

    private EditText mLoginAccountEt;
    private EditText mLoginCheckEt;
    private ImageView mPwdVisibleImg;
    private Button mLoginBtn;
    private Spinner mPlatformSp;
    private RelativeLayout mRootRl;

    private final int QUIT_APP = 0;
    private final int LOGIN = 1;
    private final int GET_TOKEN_SESSION= 3;
    private final int LOGIN_FAILED = 400;
    private final int LOGIN_DEFAULT_FAILED = 400000;
    private final int ACCOUNT_OR_PWD_ERROR = 400120;
    private final int ILLEGAL_USER_SESSION = 400110;
    private final int ILLEGAL_USER_TOKEN = 400010;
    private final int SUCCESS = 200;
    private static boolean isExit = false;
    private ZLoadingDialog mLoadingDlg = new ZLoadingDialog(LoginActivity.this);
    private Intent intent;
    private boolean isVisible = false;
    public Handler handler = new  Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what)
            {
                case ACCOUNT_OR_PWD_ERROR:
                    Toast.makeText(LoginActivity.this,"用户名或密码错误,请重新输入",Toast.LENGTH_LONG).show();
                    mLoadingDlg.dismiss();
                    break;
                case ILLEGAL_USER_SESSION:
                case ILLEGAL_USER_TOKEN:
                case LOGIN_DEFAULT_FAILED:
                case LOGIN_FAILED:
                    Toast.makeText(LoginActivity.this, String.valueOf(msg.obj),Toast.LENGTH_LONG).show();
                    mLoadingDlg.dismiss();
                    break;
                case LOGIN:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            login(mLoginAccountEt.getText().toString(), mLoginCheckEt.getText().toString(),ApiConfig.getSessionUUID(), ApiConfig.getAccessToken());
                        }
                    }).start();
                    break;
                case QUIT_APP://判断是否连续点击两次
                    isExit = false;
                    break;
                case SUCCESS:
                    intent.setClass(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    mLoadingDlg.dismiss();
                    finish();
                    break;
                case GET_TOKEN_SESSION:
                    InitTokenAndSession();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        requestPermissions();
        init();
        initView();
        initEvent();
    }

    @Override
    public void init() {
        intent = new Intent();
    }

    @Override
    public void initData() {}

    @Override
    public void initView() {
        mLoginAccountEt = findViewById(R.id.et_loginAccount);
        mLoginCheckEt = findViewById(R.id.et_loginCheck);
        mPwdVisibleImg = findViewById(R.id.img_visiblePwd);
        mLoginBtn = findViewById(R.id.btn_login);
        mPlatformSp = findViewById(R.id.spinner_platform);
        mRootRl = findViewById(R.id.layout_loginRoot);
        mPwdVisibleImg.setOnClickListener(this);
        mLoginBtn.setOnClickListener(this);
        mLoginAccountEt.setText("qwerASD5");
        mLoginCheckEt.setText("qwertyuiiopASDFG5*");

//        mLoginAccountEt.setText("AdminBDS");
//        mLoginCheckEt.setText("Beijing712*BDJC");

        mLoginAccountEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                {
                    checkAccount();
                }
            }
        });

        mLoginCheckEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                {
                    checkPassword();
                }
            }
        });

        mPlatformSp.setSelection(1);    //默认为第二平台

        mPlatformSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        ApiConfig.setBaseUrl(ApiConfig.FIRST_BASE_URL);
                        saveStringToSP("presentPlatform", "1");
                        break;
                    case 1:
                        ApiConfig.setBaseUrl(ApiConfig.SECOND_BASE_URL);
                        saveStringToSP("presentPlatform", "2");
                        break;
                    case 2:
                        ApiConfig.setBaseUrl(ApiConfig.THIRD_BASE_URL);
                        saveStringToSP("presentPlatform", "3");
                        break;
                    case 3:
                        ApiConfig.setBaseUrl(ApiConfig.FOURTH_BASE_URL);
                        saveStringToSP("presentPlatform", "4");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    @Override
    public void initEvent() {}

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.img_visiblePwd:
                setPwdVisible();
                break;
            case R.id.btn_login:
                //收起软键盘
                InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                if (!(checkAccount()&&checkPassword()))
                {
                    break;
                }
                mLoadingDlg.setLoadingBuilder(Z_TYPE.ROTATE_CIRCLE)//设置类型
                        .setLoadingColor(Color.BLACK)//颜色
                        .setHintText("加载中")
                        .setCanceledOnTouchOutside(false)
                        .show();
                handler.sendEmptyMessageDelayed(GET_TOKEN_SESSION,0);
                break;
        }
    }

    private void setPwdVisible() {
        if (!isVisible)
        {
            mPwdVisibleImg.setImageResource(R.drawable.ic_pwd_visible);
            //设置密码可见
            mLoginCheckEt.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            //将已输入的密码设置为可见
            mLoginCheckEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            isVisible = true;
        }else{
            mPwdVisibleImg.setImageResource(R.drawable.ic_pwd_invisible);
            //设置密码不可见
            mLoginCheckEt.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            //将已输入的密码设置为不可见
            mLoginCheckEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            isVisible = false;
        }
        //设置是否可见之后，光标会移动到首位。此时需将光标移动到最后一位
        mLoginCheckEt.setSelection(mLoginCheckEt.getText().toString().length());
    }

    public void login(final String username, final String password, final String SessionUUID, final String AccessToken)
    {
        final String encodePwd = LoginUtil.DES3Encode(password,SessionUUID);
        LoginUtil.loginByPwd(LoginActivity.this,username, encodePwd, SessionUUID, AccessToken, new ApiCallback() {
            @Override
            public void onSuccess(String res) {
                //保存信息到SP中，若session未过期，用于自动登录
                saveStringToSP("userName",username);
                saveStringToSP("password",encodePwd);
                saveStringToSP("sessionUUID",SessionUUID);
                saveStringToSP("accessToken",AccessToken);
                if (!TextUtils.isEmpty(res)) {
                    try {
                        Message message = new Message();
                        JSONObject object = new JSONObject(res);
                        String errCode = object.getString("ResponseCode");
                        String errMsg = object.getString("ResponseMsg");
                        switch (errCode) {
                            case "200": //登录成功
                                handler.sendEmptyMessageDelayed(SUCCESS,1000);
                                break;
                            case "400"://操作失败/参数非法
                                message.obj = errMsg;
                                message.what = LOGIN_FAILED;
                                handler.sendMessage(message);
                                break;
                            case "400010"://访问令牌非法
                                message.obj = errMsg;
                                message.what = ILLEGAL_USER_TOKEN;
                                handler.sendMessage(message);
                                break;
                            case "400120": //用户名和密码错误
                                message.obj = errMsg;
                                message.what = ACCOUNT_OR_PWD_ERROR;
                                handler.sendMessage(message);
                                break;
                            case "400110"://用户会话非法
                                message.obj = errMsg;
                                message.what = ILLEGAL_USER_SESSION;
                                handler.sendMessage(message);
                                break;
                            default:
                                message.obj = errMsg;
                                message.what = LOGIN_DEFAULT_FAILED;
                                handler.sendMessage(message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(LoginActivity.this, "请求失败，请检查网络连接，稍后再试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void requestPermissions(){
        List<String> permissionList = new ArrayList<String>();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0){
                    for(int result : grantResults){
                        if(result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this, "需要获取所需权限！", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                } else {
                    Toast.makeText(this, "需要获取所需权限!", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            handler.sendEmptyMessageDelayed(QUIT_APP, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }

    public boolean checkAccount()
    {
        String account = mLoginAccountEt.getText().toString();
        boolean isCorrect = true;
        if ("".equals(account))
        {
            Toast.makeText(getApplicationContext(),"请输入账号",Toast.LENGTH_SHORT).show();
        }else if (Character.isLetter(account.charAt(0)))
        {
            //用户名
            if (!LoginUtil.checkAccount(account,LoginUtil.LOGINBYPWD))
            {
                isCorrect = false;
                LogUtil.e("LoginActivity Account","用户名格式错误");
            }
        }else if (account.contains("@"))
        {
            //邮箱
            if (!LoginUtil.checkAccount(account,LoginUtil.LOGINBYEMAIL))
            {
                isCorrect = false;
                LogUtil.e("LoginActivity Account","邮箱格式错误");
            }
        }else {
            //手机号
            if (!LoginUtil.checkAccount(account,LoginUtil.LOGINBYPHONE))
            {
                isCorrect = false;
                LogUtil.e("LoginActivity Account","手机号格式错误");
            }
        }
        if (!isCorrect)
        {
            Toast.makeText(getApplicationContext(),"请输入正确的用户名密码",Toast.LENGTH_SHORT).show();
        }
        return isCorrect;
    }

    public boolean checkPassword()
    {
        String password = mLoginCheckEt.getText().toString();
        boolean isCorrect = true;
        if ("".equals(password))
        {
            isCorrect = false;
            Toast.makeText(getApplicationContext(),"请输入密码",Toast.LENGTH_SHORT).show();
        }

        /*
        else if (!LoginUtil.checkPwd(password))  检查密码正则
        {
            isCorrect = false;
            Toast.makeText(getApplicationContext(),"密码输入有误，请重新输入",Toast.LENGTH_SHORT).show();
        }
        */
        return isCorrect;
    }

    public void InitTokenAndSession()
    {
        if (LoginUtil.isNetworkUsable(this))
        {
            new Thread(new Runnable()
            {
                @Override
                public void run() {
                    Thread getTokenThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (!LoginUtil.getAccessToken(LoginActivity.this)){ }
                            LoginUtil.upDateToken(getApplicationContext());
                            LogUtil.e("LoginActivity 成功获取Token", ApiConfig.getAccessToken());
                        }
                    });
                    getTokenThread.start();
                    try {
                        getTokenThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Thread getSessionThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (!LoginUtil.getSessionId(LoginActivity.this)){ }
                            LogUtil.e("LoginActivity 成功获取Session", ApiConfig.getSessionUUID());
                        }
                    });
                    getSessionThread.start();
                    try {
                        getSessionThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessageDelayed(LOGIN,0);
                }
            }).start();
        }else {
            mLoadingDlg.dismiss();
        }
    }

}
