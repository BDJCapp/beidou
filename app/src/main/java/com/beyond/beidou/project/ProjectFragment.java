package com.beyond.beidou.project;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MyRecycleView;
import com.beyond.beidou.R;
import com.beyond.beidou.adapter.MonitoringPointsAdapter;
import com.beyond.beidou.entites.MonitoringPoint;
import com.beyond.beidou.util.ScreenUtil;
import com.yinglan.scrolllayout.ScrollLayout;

import java.util.ArrayList;
import java.util.List;

public class ProjectFragment extends BaseFragment {

    private LocationClient mLocationClient;
    private boolean isFirstLocate = true;
    private BaiduMap mBaiduMap = null;
    private MapView mMapView;
    private ScrollLayout mScrollLayout;
    private RelativeLayout mRelativeLayout;
    private Spinner mSpinner;
    private List<MonitoringPoint> mPointList = new ArrayList<>();
    private MyRecycleView mRecyclerView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(initLayout(), container, false);
        initView(view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public ProjectFragment() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
    }

    public static ProjectFragment newInstance() {
        ProjectFragment fragment = new ProjectFragment();
        return fragment;
    }


    protected int initLayout() {
        SDKInitializer.initialize(getContext().getApplicationContext());
        return R.layout.fragment_project;
    }


    protected void initView(View view) {
        mMapView = view.findViewById(R.id.bdmapView);
        mScrollLayout = view.findViewById(R.id.scrollLayout);
        mRelativeLayout = view.findViewById(R.id.relativeLayout);
        mSpinner = view.findViewById(R.id.spinner);
        mRecyclerView = view.findViewById(R.id.recycle_view);

//        设置 setting
        mScrollLayout.setMinOffset(200);
        mScrollLayout.setMaxOffset(800);
        mScrollLayout.setExitOffset(400);
        mScrollLayout.setIsSupportExit(true);
        mScrollLayout.setAllowHorizontalScroll(true);
        mScrollLayout.setToOpen();

        mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScrollLayout.scrollToExit();
            }
        });
    }

    protected void initData() {
        final int width = ScreenUtil.getScreenXRatio(getActivity());
        final int height = ScreenUtil.getScreenXRatio(getActivity());

        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMapView.setZoomControlsPosition(new Point((int) (width * 0.88 + 0.5f), (int) (height * 0.73 + 0.5f)));
            }
        });
        mBaiduMap.setMyLocationEnabled(true);

        mLocationClient = new LocationClient(getContext().getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        requestLocation();    //请求百度地图位置

        final String[] arr = {"实验室测试工程", "北京和平里工程"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), R.layout.item_select, arr);
        arrayAdapter.setDropDownViewResource(R.layout.item_drop);
        mSpinner.setAdapter(arrayAdapter);

        initPointList();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        MonitoringPointsAdapter pointsAdapter = new MonitoringPointsAdapter(mPointList);
        mRecyclerView.setAdapter(pointsAdapter);

    }

    private void initPointList() {
        MonitoringPoint point1 = new MonitoringPoint("监测点1", "基准站", "2020-04-01");
        MonitoringPoint point2 = new MonitoringPoint("监测点2", "移动站", "2020-04-01");
        MonitoringPoint point3 = new MonitoringPoint("监测点3", "移动站", "2020-04-01");
        MonitoringPoint point4 = new MonitoringPoint("监测点4", "移动站", "2020-04-01");
        mPointList.add(point1);
        mPointList.add(point2);
        mPointList.add(point3);
        mPointList.add(point4);
        mPointList.add(point4);
        mPointList.add(point4);
        mPointList.add(point4);
        mPointList.add(point4);
        mPointList.add(point4);
        mPointList.add(point4);
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void navigateTo(BDLocation location) {
        if (isFirstLocate) {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(18f);
            mBaiduMap.animateMapStatus(update);
            isFirstLocate = false;
        } else {
            MyLocationData.Builder builder = new MyLocationData.Builder();
            builder.latitude(location.getLatitude()).longitude(location.getLongitude());
            MyLocationData data = builder.build();
            mBaiduMap.setMyLocationData(data);
        }
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();

        //设置定位模式，默认高精度
        //LocationMode.Hight_Accuracy：高精度
        //LocationMode.Battery_Saving：低功耗
        //LocationMode.Device_Sensors：仅使用设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        //可选，设置返回经纬度坐标类型，默认 GCJ02
        //GCJ02：国测局坐标
        //BD09LL：百度经纬度坐标
        //BD09：百度墨卡托坐标
        //海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标
        option.setCoorType("BD09LL");

        //可选，设置发起定位请求的问题，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置为非0，需设置1000ms以上才有效
        option.setScanSpan(1000);

        //可选，设置是否使用gps，默认false
        //使用高精度和仅使用设备两种定位模式的，参数必须设置为true
        option.setOpenGps(true);

        //可选，设置是否当GPS有效时按照1s/1次频率输出GPS结果，默认false
        option.setLocationNotify(true);

        //可选，定位SDK内部是一个service，并放到了独立进程
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnorekillProcess(true)
        option.setIgnoreKillProcess(false);

        //可选，设置是否收集Crash信息，默认收集，即参数为false
        option.SetIgnoreCacheException(false);

        //可选，V7.2版本新增功能
        //如果设置了该接口，首次启动定位时，会先判断当前Wi-Fi是否超出有效期，若超出有效期，会先重新扫描Wi-Fi，然后定位
        option.setWifiCacheTimeOut(5 * 60 * 1000);

        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
        option.setEnableSimulateGps(false);

        option.setIsNeedAddress(true);

        mLocationClient.setLocOption(option);
    }

    private class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            navigateTo(bdLocation);

            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("纬度：").append(bdLocation.getLatitude()).append("\n");
            currentPosition.append("经度：").append(bdLocation.getLongitude()).append("\n");
            currentPosition.append("国家：").append(bdLocation.getCountry()).append("\n");
            currentPosition.append("省：").append(bdLocation.getProvince()).append("\n");
            currentPosition.append("市：").append(bdLocation.getCity()).append("\n");
            currentPosition.append("区：").append(bdLocation.getDistrict()).append("\n");
            currentPosition.append("村镇：").append(bdLocation.getTown()).append("\n");
            currentPosition.append("街道：").append(bdLocation.getStreet()).append("\n");
            currentPosition.append("地址：").append(bdLocation.getAddrStr()).append("\n");
            currentPosition.append("定位方式：");
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                currentPosition.append("GPS").append("\n");
            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                currentPosition.append("网络").append("\n");
            }
            currentPosition.append("getLocType: ").append(bdLocation.getLocType());
            Log.i("onReceiveLocation", currentPosition.toString());
        }
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }
}
