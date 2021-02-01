package com.beyond.beidou.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.beyond.beidou.BaseActivity;
import com.beyond.beidou.R;

import java.util.ArrayList;
import java.util.List;

public class MapTestActivity extends BaseActivity {

    private MapView mapView;
    private LocationClient mLocationClient;
    private BaiduMap baiduMap;

    @Override
    public void init() {

    }

    @Override
    public void initData() {

    }

    @Override
    public void initView() {

    }

    @Override
    public void initEvent() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map_test);
        mapView = findViewById(R.id.map_bmapview);
        baiduMap = mapView.getMap();
//由于这四个为危险权限，所以必须要进行运行时动态申请
        List<String> permissionList = new ArrayList<>();  //将未授权的权限添加到List中，统一申请
        if (ContextCompat.checkSelfPermission(MapTestActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MapTestActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
        {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MapTestActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty())
        {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MapTestActivity.this,permissions,1 );
        }
        else {
            requestLocation();
            initEvent();
        }
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0){
                    for (int result: grantResults){
                        if (result != PackageManager.PERMISSION_GRANTED)
                        {
                            Toast.makeText(this,"必须同意所有授权才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }
                else {
                    Toast.makeText(this,"发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    private void initLocation()
    {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);   //设置扫描间隔为5秒，即间隔5秒更新一次位置信息。
        //option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);  //设置定位模式为仅GPS
        option.setIsNeedAddress(true);  //设置获取详细定位信息
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}