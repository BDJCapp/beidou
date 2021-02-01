package com.beyond.beidou.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.beyond.beidou.BaseActivity;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.util.APIUtil;
import com.beyond.beidou.util.HttpUtil;
import com.beyond.beidou.util.LoginUtil;
import com.xuexiang.xui.widget.toast.XToast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

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
    private TextView tvRegister;
    private TextView tvForgetPwd;
    private TextView tvPictureCode;
    private EditText etPictureCode;
    private ImageView imgPictureCode;

    private static int loginType = LoginUtil.LOGINBYPWD;   //默认为密码登录
    private final int IMAGECODESUCCESS = 1;            //设置msg.what
    private final int LOGINDEFAULTFAILED = 2;
    private final int CODE_ERROR = 421;
    private final int ACCOUNT_OR_PWD_ERROR = 422;
    private final int CODE_NULL = 423;

    private Intent intent;
    private boolean isVisible = false;
    private LoginUtil loginUtil;
    //这个handler是用来做什么的
    public Handler handler = new  Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what)
            {
                case IMAGECODESUCCESS:
                    byte[] Picture = (byte[]) msg.obj;

                    Bitmap bitmap = BitmapFactory.decodeByteArray(Picture, 0, Picture.length);
                    //将图片流传化为bitmap类型 这样才能用到
                    Matrix matrix = new Matrix();
                    matrix.postScale(2,2);//对图片进行缩放第一个参数是X轴的缩放大小，第二个参数是Y轴的缩放大小
                    Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);

                    imgPictureCode.setImageBitmap(resizeBmp);
                    break;
                case LOGINDEFAULTFAILED:
                    Toast.makeText(LoginActivity.this,"输入信息有误，请重新登录",Toast.LENGTH_LONG).show();
                    etLoginAccount.setText("");
                    etLoginCheck.setText("");
                    etPictureCode.setText("");
                    getImageCode();
                    break;
                case CODE_ERROR:
                    Toast.makeText(LoginActivity.this, String.valueOf(msg.obj),Toast.LENGTH_LONG).show();
                    etPictureCode.setText("");
                    getImageCode();
                    break;
                case ACCOUNT_OR_PWD_ERROR:
                    Toast.makeText(LoginActivity.this, String.valueOf(msg.obj),Toast.LENGTH_LONG).show();
                    etLoginAccount.setText("");
                    etLoginCheck.setText("");
                    etPictureCode.setText("");
                    getImageCode();
                    break;
                case CODE_NULL:
                    Toast.makeText(LoginActivity.this, String.valueOf(msg.obj),Toast.LENGTH_LONG).show();
                    getImageCode();
                    break;
            }

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
//        tvLoginByPhone = findViewById(R.id.tv_loginByPhone);
        tvLoginByPwd = findViewById(R.id.tv_loginByPwd);
//        tvLoginByEmail = findViewById(R.id.tv_loginByEmail);
        tvAccount = findViewById(R.id.tv_account);
//        tvCheckCode = findViewById(R.id.tv_checkCode);
        etLoginAccount = findViewById(R.id.et_loginAccount);
        etLoginCheck = findViewById(R.id.et_loginCheck);
//        btnSendCode = findViewById(R.id.btn_loginSendCode);
        imgVisible = findViewById(R.id.img_visiblePwd);
        btnLogin = findViewById(R.id.btn_login);
//        tvRegister = findViewById(R.id.tv_register);
        tvForgetPwd = findViewById(R.id.tv_forgetPwd);
        tvPictureCode = findViewById(R.id.tv_pictureCode);
        etPictureCode = findViewById(R.id.et_pictureCheck);
        imgPictureCode = findViewById(R.id.img_pictureCode);
//
//        tvLoginByPwd.setOnClickListener(this);
//        tvLoginByPhone.setOnClickListener(this);
//        tvLoginByEmail.setOnClickListener(this);
        imgVisible.setOnClickListener(this);
//        tvRegister.setOnClickListener(this);
//        tvForgetPwd.setOnClickListener(this);
//        btnSendCode.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        imgPictureCode.setOnClickListener(this);
    }

    @Override
    public void initEvent() {
        getImageCode();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
//            case R.id.tv_loginByPhone:
//                loginType = LoginUtil.LOGINBYPHONE;
//                setPhoneUI();
//                break;
            case R.id.tv_loginByPwd:
                loginType = LoginUtil.LOGINBYPWD;
                setPwdUI();
                break;
//            case R.id.tv_loginByEmail:
//                loginType = LoginUtil.LOGINBYEMAIL;
//                setEmailUI();
//                break;
            case R.id.img_visiblePwd:
                setPwdVisible();
                break;
//            case R.id.tv_register:
//                intent.setClass(LoginActivity.this,RegisterActivity.class);
//                startActivity(intent);
//                finish();
//                break;
//            case R.id.btn_loginSendCode:
//                    sendCode();
//                    break;
            case R.id.btn_login:
                login(etLoginAccount.getText().toString(),etLoginCheck.getText().toString(),etPictureCode.getText().toString());
                //loginUtil.test(etPictureCode.getText().toString());
                break;
            case R.id.img_pictureCode:
                getImageCode();
                break;
//            case R.id.tv_forgetPwd:
//                intent.setClass(LoginActivity.this,ForgetPwdActivity.class);
//                startActivity(intent);
//                break;
        }
    }


    /**
     * 1.检查手机号或者邮箱是否有效
     * 2.发送验证码
     */
    private void sendCode() {
        if (loginType == LoginUtil.LOGINBYPHONE)
        {
            String phone = etLoginAccount.getText().toString();
            boolean isPhone = loginUtil.checkAccount(phone, LoginUtil.LOGINBYPHONE);
            if (!isPhone)
            {
                //如果手机号格式不正确
                etLoginAccount.setText("");
                XToast.warning(this,"手机号格式错误，请重新输入").show();
            }
        }
        else if (loginType == LoginUtil.LOGINBYEMAIL)
        {
            String email = etLoginAccount.getText().toString();
            boolean isEmail = loginUtil.checkAccount(email, LoginUtil.LOGINBYEMAIL);
            if (!isEmail)
            {
                //如果手机号格式不正确
                etLoginAccount.setText("");
                XToast.warning(this,"邮箱格式错误，请重新输入").show();
            }
        }
    }

    /**
     * @param
     * @return
     * @author 李垚
     * @time 2020/12/15 15:45
     */
    private void setPhoneUI() {
        tvLoginByEmail.setTextColor(getResources().getColor(R.color.text_black));
        tvLoginByPhone.setTextColor(getResources().getColor(R.color.main_blue));
        tvLoginByPwd.setTextColor(getResources().getColor(R.color.text_black));
        tvAccount.setText("+86 :   ");
        etLoginAccount.setHint("请输入手机号码");
        btnSendCode.setVisibility(View.VISIBLE);
        tvCheckCode.setText("验证码");
        etLoginCheck.setHint("请输入手机验证码验证码");
        etLoginAccount.setText("");
        etLoginCheck.setText("");
        imgVisible.setVisibility(View.INVISIBLE);
        tvForgetPwd.setVisibility(View.INVISIBLE);
    }

    private void setEmailUI() {
        tvLoginByEmail.setTextColor(getResources().getColor(R.color.main_blue));
        tvLoginByPhone.setTextColor(getResources().getColor(R.color.text_black));
        tvLoginByPwd.setTextColor(getResources().getColor(R.color.text_black));
        tvAccount.setText("邮箱    ");
        etLoginAccount.setHint("请输入邮箱");
        btnSendCode.setVisibility(View.VISIBLE);
        tvCheckCode.setText("验证码");
        etLoginCheck.setHint("请输入验证码");
        etLoginAccount.setText("");
        etLoginCheck.setText("");
        imgVisible.setVisibility(View.INVISIBLE);
        tvForgetPwd.setVisibility(View.INVISIBLE);
    }

    private void setPwdUI() {
        tvLoginByEmail.setTextColor(getResources().getColor(R.color.text_black));
        tvLoginByPhone.setTextColor(getResources().getColor(R.color.text_black));
        tvLoginByPwd.setTextColor(getResources().getColor(R.color.main_blue));
        etLoginAccount.setHint("请输入手机号码/账号/邮箱");
        btnSendCode.setVisibility(View.INVISIBLE);
        tvAccount.setText("账号 :   ");
        tvCheckCode.setText("密码：");
        etLoginCheck.setHint("请输入密码");
        etLoginAccount.setText("");
        etLoginCheck.setText("");
        etLoginCheck.setTransformationMethod(PasswordTransformationMethod.getInstance());
        imgVisible.setVisibility(View.VISIBLE);
        tvForgetPwd.setVisibility(View.VISIBLE);
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

    /**
     * 获取图片验证码
     */
    //获取图形验证码和获取普通图片有什么区别吗,怎么确定是什么请求
    public void getImageCode()
    {
        Log.e("设置之后的SessionUUID", APIUtil.getSessionUUID());
        FormBody body1 = new FormBody.Builder()//
                .add("sessionUUID", APIUtil.getSessionUUID())//传输sessionuid
                .build();
        Log.e("请求成功，SessionUUID为1",APIUtil.getSessionUUID());
        HttpUtil.sendOkHttpRequestFormBody(APIUtil.getGetImageCodeUrl(), body1, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("获取图片验证码失败", "错误信息是" + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.e("请求成功，SessionUUID为2",APIUtil.getSessionUUID());
                byte[] bytes = response.body().bytes();//获取字节数组//获取响应体
                Message message = new Message();
                message.obj = bytes;
                message.what = IMAGECODESUCCESS;
                handler.sendMessage(message);
            }
        });
    }

    public void login(String userName,String password,String imageCode)
    {
        if (loginType == LoginUtil.LOGINBYPWD) {
            loginUtil.loginByPwd("qwerASD5", "qwertyuiiopASDFG5*", imageCode, new Callback() {

                /*loginUtil.loginByPwd(userName, password, imageCode, new Callback() {*/

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    //网络请求失败
                    Toast.makeText(LoginActivity.this, "网络请求失败，请检查网络连接，稍后再试", Toast.LENGTH_SHORT).show();
                    etLoginAccount.setText("");
                    etLoginCheck.setText("");
                    etPictureCode.setText("");
                    getImageCode();
                }
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseText = response.body().string();
                    if (!TextUtils.isEmpty(responseText))
                    {
                        try {
                            Message message = new Message();
                            JSONObject object = new JSONObject(responseText);
                            String errCode = object.getString("errCode");
                            String errMsg = object.getString("errMsg");
                            Log.e("登录的response",responseText);
                            //获取json中的code。json是包含很多数据，这里只是单拿出其中的code吗
                            switch (errCode){
                                case "200": //登录成功
                                    intent.setClass(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                    break;
                                case "421": //验证码错误
                                    message.obj = errMsg;
                                    message.what = CODE_ERROR;
                                    handler.sendMessage(message);
                                    break;
                                case "422": //用户名和密码错误
                                    message.obj = errMsg;
                                    message.what = ACCOUNT_OR_PWD_ERROR;
                                    handler.sendMessage(message);
                                    break;
                                case "423": //验证码为空
                                    message.obj = errMsg;
                                    message.what = CODE_NULL;
                                    handler.sendMessage(message);
                                    break;
                                default:
                                    message.what= LOGINDEFAULTFAILED;
                                    handler.sendMessage(message);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }
}
