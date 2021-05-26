package com.beyond.beidou.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

    private TextView tvLoginByPhone;
    private TextView tvLoginByPwd;
    private TextView tvLoginByEmail;
    private TextView tvAccount;
    private TextView tvCheckCode;
    private EditText etLoginAccount;
    private EditText etLoginCheck;
    private Button btnSendCode;
    private ImageView imgVisible;
    private Button btnLogin;
    private TextView tvForgetPwd;

    private final int QUITAPP = 0;
    private final int LOGIN = 1;
    private final int LOGINDEFAULTFAILED = 2;
    private final int CODE_ERROR = 421;
    private final int ACCOUNT_OR_PWD_ERROR = 400120;
    private final int IIILEGAL_USER_SESSION = 400110;
    private final int SESSION_EXPIRATION_LOGOUT = 204;
    private final int CODE_NULL = 423;
    private final int SUCCESS = 200;
    private static boolean isExit = false;
    ZLoadingDialog dialog = new ZLoadingDialog(LoginActivity.this);

    private Intent intent;
    private boolean isVisible = false;
    private LoginUtil loginUtil;
    public Handler handler = new  Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what)
            {
                case ACCOUNT_OR_PWD_ERROR:
                    Toast.makeText(LoginActivity.this,"用户名或密码错误,请重新输入",Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    break;
                case IIILEGAL_USER_SESSION:

                case SESSION_EXPIRATION_LOGOUT:

                case 400:
                    Toast.makeText(LoginActivity.this, String.valueOf(msg.obj),Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    break;
                case LOGIN:
                    //加载动画之后登录
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            login(etLoginAccount.getText().toString(),etLoginCheck.getText().toString(),ApiConfig.getSessionUUID(), ApiConfig.getAccessToken());
                        }
                    }).start();
                    break;
                case QUITAPP://判断是否连续点击两次
                    isExit = false;
                    break;
                case SUCCESS:
                    intent.setClass(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    dialog.dismiss();
                    finish();
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
        loginUtil = new LoginUtil();
    }

    @Override
    public void initData() {

    }

    @Override
    public void initView() {
        tvLoginByPwd = findViewById(R.id.tv_loginByPwd);
        tvAccount = findViewById(R.id.tv_account);
        etLoginAccount = findViewById(R.id.et_loginAccount);
        etLoginCheck = findViewById(R.id.et_loginCheck);
        imgVisible = findViewById(R.id.img_visiblePwd);
        btnLogin = findViewById(R.id.btn_login);
        tvForgetPwd = findViewById(R.id.tv_forgetPwd);
        imgVisible.setOnClickListener(this);
        btnLogin.setOnClickListener(this);

//        etLoginAccount.setText("qazXSW0");
//        etLoginCheck.setText("qazxswEDCVFR0*");
        etLoginAccount.setText("qwerASD5");
        etLoginCheck.setText("qwertyuiiopASDFG5*");

        etLoginAccount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                {
                    checkAccount();
                }
            }
        });

        etLoginCheck.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                {
                    checkPassword();
                }
            }
        });
    }

    @Override
    public void initEvent() {

    }


    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.img_visiblePwd:
                setPwdVisible();
                break;
            case R.id.btn_login:
                if (!(checkAccount()&&checkPassword()))
                {
                    break;
                }
                Log.e("token为", ApiConfig.getAccessToken());
                Log.e("SessionUUID为",  ApiConfig.getSessionUUID());
                dialog.setLoadingBuilder(Z_TYPE.ROTATE_CIRCLE)//设置类型
                        .setLoadingColor(Color.BLACK)//颜色
                        .setHintText("Loading...")
                        .setCanceledOnTouchOutside(false)
                        .show();
                handler.sendEmptyMessageDelayed(LOGIN,0);
                break;
        }
    }

    private void setPwdVisible() {
        if (!isVisible)
        {
            imgVisible.setImageResource(R.drawable.ic_pwd_visible);
            //设置密码可见
            etLoginCheck.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            //将已输入的密码设置为可见
            etLoginCheck.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

            isVisible = true;
        }else{
            imgVisible.setImageResource(R.drawable.ic_pwd_invisible);
            //设置密码不可见
            etLoginCheck.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            //将已输入的密码设置为不可见
            etLoginCheck.setTransformationMethod(PasswordTransformationMethod.getInstance());
            isVisible = false;
        }
        //设置是否可见之后，光标会移动到首位。此时需将光标移动到最后一位
        etLoginCheck.setSelection(etLoginCheck.getText().toString().length());
    }

    public void login(String username,String password,String SessionUUID,String AccessToken)
    {
        String encodePwd = LoginUtil.DES3Encode(password,SessionUUID);
        loginUtil.loginByPwd(LoginActivity.this,username, encodePwd, SessionUUID, AccessToken, new ApiCallback() {
            @Override
            public void onSuccess(String res) {
                if (!TextUtils.isEmpty(res)) {
                    try {
                        Message message = new Message();
                        JSONObject object = new JSONObject(res);
                        String errCode = object.getString("ResponseCode");
                        String errMsg = object.getString("ResponseMsg");
                        switch (errCode) {
                            case "200": //登录成功
                                handler.sendEmptyMessageDelayed(200,1000);
                                break;
                            case "400"://操作失败/参数非法
                                message.obj = errMsg;
                                message.what = 400;
                                handler.sendMessage(message);
                                break;
                            case "400010"://访问令牌非法
                                message.obj = errMsg;
                                message.what = 40010;
                                handler.sendMessage(message);
                                break;
                            case "400120": //用户名和密码错误
                                message.obj = errMsg;
                                message.what = ACCOUNT_OR_PWD_ERROR;
                                handler.sendMessage(message);
                                break;
                            case "400110"://用户会话非法
                                message.obj = errMsg;
                                message.what = IIILEGAL_USER_SESSION;
                                handler.sendMessage(message);
                                break;
                            default:
                                message.what = LOGINDEFAULTFAILED;
                                handler.sendMessage(message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(LoginActivity.this, "网络请求失败，请检查网络连接，稍后再试", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(this, "Need to grant all permissions!", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                } else {
                    Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
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
            handler.sendEmptyMessageDelayed(QUITAPP, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }

    public boolean checkAccount()
    {
        String account = etLoginAccount.getText().toString();
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
        String password = etLoginCheck.getText().toString();
        boolean isCorrect = true;
        if ("".equals(password))
        {
            isCorrect = false;
            Toast.makeText(getApplicationContext(),"请输入密码",Toast.LENGTH_SHORT).show();
        } else if (!LoginUtil.checkPwd(password))
        {
            isCorrect = false;
            Toast.makeText(getApplicationContext(),"密码输入有误，请重新输入",Toast.LENGTH_SHORT).show();
        }
        return isCorrect;
    }
}
