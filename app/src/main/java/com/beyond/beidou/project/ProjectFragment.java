package com.beyond.beidou.project;

import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import com.baidu.mapapi.utils.CoordinateConverter;
import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.MyRecycleView;
import com.beyond.beidou.R;
import com.beyond.beidou.adapter.MonitoringPointsAdapter;
import com.beyond.beidou.api.Api;
import com.beyond.beidou.api.ApiCallback;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.data.ChartFragment;
import com.beyond.beidou.entites.MonitoringPoint;
import com.beyond.beidou.entites.ProjectResponse;
import com.beyond.beidou.util.ScreenUtil;
import com.google.gson.Gson;
import com.yinglan.scrolllayout.ScrollLayout;

import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProjectFragment extends BaseFragment implements View.OnClickListener {

    private LocationClient mLocationClient;
    private boolean isFirstLocate = true;
    private BaiduMap mBaiduMap;
    private MapView mMapView;
    private ScrollLayout mScrollLayout;
    private RelativeLayout mRelativeLayout;
    private Spinner mSpinner;
    private List<MonitoringPoint> mPointList = new ArrayList<>();
    private MyRecycleView mRecyclerView;
    private HashMap<String, Object> mParams = new HashMap<String, Object>();
    private TextView mTvAmount;
    private Button mBtnAmount;
    private Button mBtnOnline;
    private Button mBtnWarning;
    private Button mBtnError;
    private Button mBtnOffline;
    private TextView mTvTime;

    private List<ProjectResponse.ProjectListBean> projectList = new ArrayList<>();
    private ProjectResponse.ProjectListBean.ProjectStationStatusBean projectStationStatus;
    private ArrayList<String> projectNameList = new ArrayList<>();
    private String netTime;
    private List<ProjectResponse.ProjectListBean.StationListBean> projectStationList = new ArrayList<>();
    private MonitoringPointsAdapter mPointsAdapter;
    private LinearLayoutManager layoutManager;
    private ArrayList<String> stationNameList = new ArrayList<>();

    private static boolean isFirstLogin = true;
    private static String presentProject;
    public static boolean isReLogin = false;

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
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        //显示才刷新
        if (!hidden) {
            MainActivity mMainActivity = (MainActivity) getActivity();
            if (!presentProject.equals(mMainActivity.getPresentProject())) {
                isFirstLocate = true;
            }
            presentProject = mMainActivity.getPresentProject();
            Log.e("refreshData", "onHiddenChanged");
            refreshData();
        }
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
        mTvAmount = view.findViewById(R.id.tv_amount);
        mBtnAmount = view.findViewById(R.id.btn_amount);
        mBtnOnline = view.findViewById(R.id.btn_online);
        mBtnWarning = view.findViewById(R.id.btn_warning);
        mBtnError = view.findViewById(R.id.btn_error);
        mBtnOffline = view.findViewById(R.id.btn_offline);
        mTvTime = view.findViewById(R.id.tv_time);

//        设置 setting
        mScrollLayout.setMinOffset(200);
        mScrollLayout.setMaxOffset(800);
        mScrollLayout.setExitOffset(400);
        mScrollLayout.setIsSupportExit(true);
        mScrollLayout.setAllowHorizontalScroll(true);
        mScrollLayout.setToOpen();

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                presentProject = (String) mSpinner.getItemAtPosition(position);
                isFirstLocate = true;
                MainActivity mMainActivity = (MainActivity) getActivity();
                mMainActivity.setPresentProject(presentProject);
                Log.e("refreshData", "mSpinner");
                refreshData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                showToast("没有选中任何工程");
            }
        });
        mRelativeLayout.setOnClickListener(this);
        mBtnAmount.setOnClickListener(this);
        mBtnOnline.setOnClickListener(this);
        mBtnWarning.setOnClickListener(this);
        mBtnError.setOnClickListener(this);
        mBtnOffline.setOnClickListener(this);

    }

    protected void initData() {
        final int width = ScreenUtil.getScreenXRatio(getActivity());
        final int height = ScreenUtil.getScreenXRatio(getActivity());
        if (isFirstLogin) {
            String spVal = getStringFromSP("lastProjectName");
            presentProject = spVal;
            isFirstLogin = false;
        } else {
            MainActivity mMainActivity = (MainActivity) getActivity();
            presentProject = mMainActivity.getPresentProject();
            if(presentProject == null){
                presentProject = "";
            }
            Log.e("presentProject", presentProject);
        }
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                //待改动调整
                mMapView.setZoomControlsPosition(new Point((int) (width * 0.88 + 0.5f), (int) (height * 0.73 + 0.5f)));
            }
        });
        mBaiduMap.setMyLocationEnabled(true);
        mParams.put("AccessToken", ApiConfig.getAccessToken());
        mParams.put("SessionUUID", ApiConfig.getSessionUUID());
        Api.config(ApiConfig.GET_PROJECTS, mParams).postRequest(getContext(), new ApiCallback() {
            @Override
            public void onSuccess(final String res) {
                Gson gson = new Gson();
                ProjectResponse response = gson.fromJson(res, ProjectResponse.class);
                if (Integer.parseInt(response.getResponseCode()) == 200) {
                    projectList = response.getProjectList();
                    for (ProjectResponse.ProjectListBean project : projectList) {
                        projectNameList.add(project.getProjectName());
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), R.layout.item_select, projectNameList);
                            arrayAdapter.setDropDownViewResource(R.layout.item_drop);
                            mSpinner.setAdapter(arrayAdapter);
                            if (presentProject.equals("")) {
                                presentProject = mSpinner.getSelectedItem().toString();
                                MainActivity mMainActivity = (MainActivity) getActivity();
                                mMainActivity.setPresentProject(presentProject);
                                for (ProjectResponse.ProjectListBean project : projectList) {
                                    if (project.getProjectName().equals(presentProject)) {
                                        projectStationStatus = project.getProjectStationStatus();
                                        projectStationList = project.getStationList();
                                        for (ProjectResponse.ProjectListBean.StationListBean station : project.getStationList()) {
                                            stationNameList.add(station.getStationName());
                                        }
                                    }
                                }
                            } else {
                                for (ProjectResponse.ProjectListBean project : projectList) {
                                    if (project.getProjectName().equals(presentProject)) {
                                        projectStationStatus = project.getProjectStationStatus();
                                        projectStationList = project.getStationList();
                                        mSpinner.setSelection(projectList.indexOf(project));
                                        for (ProjectResponse.ProjectListBean.StationListBean station : project.getStationList()) {
                                            stationNameList.add(station.getStationName());
                                        }
                                    }
                                }
                            }
                            Log.e("project", "present project is " + presentProject);
                            mTvAmount.setText(String.valueOf(projectStationStatus.getTotal()));
                            mBtnAmount.setText("总数\n" + projectStationStatus.getTotal());
                            mBtnOnline.setText("在线\n" + projectStationStatus.getOnline());
                            mBtnWarning.setText("警告\n" + projectStationStatus.getWarning());
                            mBtnError.setText("故障\n" + projectStationStatus.getError());
                            mBtnOffline.setText("离线\n" + projectStationStatus.getOffline());

                            mLocationClient = new LocationClient(getContext().getApplicationContext());
                            mLocationClient.registerLocationListener(new MyLocationListener());
                            requestLocation();    //请求百度地图位置
                            initStationList();    //初始化监测点数据
                            layoutManager = new LinearLayoutManager(getContext());
                            mRecyclerView.setLayoutManager(layoutManager);
                            mPointsAdapter = new MonitoringPointsAdapter(mPointList);
                            mRecyclerView.setAdapter(mPointsAdapter);
                            mPointsAdapter.setOnItemClickListener(new MonitoringPointsAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    switchFragment(presentProject, stationNameList, position);
                                    MainActivity activity = (MainActivity) getActivity();
                                    activity.getNavigationView().setSelectedItemId(activity.getNavigationView().getMenu().getItem(1).getItemId());
                                    Log.e("project", "you click " + position);
                                }
                            });

                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault());
                            Date date = new Date(System.currentTimeMillis());
                            mTvTime.setText(simpleDateFormat.format(date));
                            //设置地图marker覆盖物
                            mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker) {
                                    View window = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow_showinfo, null, false);
                                    TextView tv_name, tv_type;
                                    LinearLayout window_layout;
                                    window_layout = window.findViewById(R.id.window_layout);
                                    tv_name = window.findViewById(R.id.tv_name);
                                    tv_type = window.findViewById(R.id.tv_type);
                                    tv_name.setText("监测点名称：" + marker.getExtraInfo().get("stationName"));
                                    tv_type.setText("监测点类型：" + marker.getExtraInfo().get("stationType"));
                                    InfoWindow mInfoWindow = new InfoWindow(window, marker.getPosition(), -100);

                                    mBaiduMap.showInfoWindow(mInfoWindow);
                                    window_layout.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mBaiduMap.hideInfoWindow();
                                        }
                                    });
                                    return false;
                                }
                            });
                        }
                    });
                }
                Log.e("project", response.getResponseMsg());
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    private void refreshData() {
        if(isReLogin){
            isFirstLogin = true;
        }
        Log.e("refreshData", "refreshData");
        Log.e("project", "projectList size: " + projectList.size());
        Log.e("project", "projectName: " + presentProject);
        for (ProjectResponse.ProjectListBean project : projectList) {
            if (project.getProjectName().equals(presentProject)) {
                projectStationStatus = project.getProjectStationStatus();
                projectStationList = project.getStationList();
                mSpinner.setSelection(projectList.indexOf(project));
            }
        }
        mTvAmount.setText(String.valueOf(projectStationStatus.getTotal()));
        mBtnAmount.setText("总数\n" + projectStationStatus.getTotal());
        mBtnOnline.setText("在线\n" + projectStationStatus.getOnline());
        mBtnWarning.setText("警告\n" + projectStationStatus.getWarning());
        mBtnError.setText("故障\n" + projectStationStatus.getError());
        mBtnOffline.setText("离线\n" + projectStationStatus.getOffline());

        mLocationClient = new LocationClient(getContext().getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        requestLocation();    //请求百度地图位置
        initStationList();    //初始化监测点数据
        layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mPointsAdapter = new MonitoringPointsAdapter(mPointList);
        mRecyclerView.setAdapter(mPointsAdapter);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        mTvTime.setText(simpleDateFormat.format(date));
        //设置地图marker覆盖物
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                View window = LayoutInflater.from(getActivity()).inflate(R.layout.popupwindow_showinfo, null, false);
                TextView tv_name, tv_type;
                LinearLayout window_layout;
                window_layout = window.findViewById(R.id.window_layout);
                tv_name = window.findViewById(R.id.tv_name);
                tv_type = window.findViewById(R.id.tv_type);
                tv_name.setText("监测点名称：" + marker.getExtraInfo().get("stationName"));
                tv_type.setText("监测点类型：" + marker.getExtraInfo().get("stationType"));
                InfoWindow mInfoWindow = new InfoWindow(window, marker.getPosition(), -100);

                mBaiduMap.showInfoWindow(mInfoWindow);
                window_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBaiduMap.hideInfoWindow();
                    }
                });
                return false;
            }
        });
    }

    //获取网络时间，待定
    private void getNetTime() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //百度时间
                    //url = new URL("http://www.baidu.com");
//                    中国科学院国家授时中心
                    URL url = new URL("http://www.ntsc.ac.cn");
                    URLConnection uc = url.openConnection();//生成连接对象
                    uc.connect(); //发出连接
                    long ld = uc.getDate(); //取得网站日期时间
                    Log.e("project", "time ld: " + uc.getDate());
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(ld);
                    netTime = formatter.format(calendar.getTime());
                    Log.e("project", "time ld " + netTime);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvTime.setText(netTime);
                        }
                    });
                } catch (Exception e) {
                    Log.e("project", "net time error :" + e.toString());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initStationList() {
        mBaiduMap.clear();
        mPointList.clear();
        Log.e("project", "stationList size: " + projectStationList.size());
        LatLng sourcePoint = null;
        for (ProjectResponse.ProjectListBean.StationListBean projectStation :
                projectStationList) {
            mPointList.add(new MonitoringPoint(projectStation.getStationName(), projectStation.getStationType(), projectStation.getStationLastTime(), projectStation.getStationStatus()));
            if (!TextUtils.isEmpty(projectStation.getStationLatitude()) && !TextUtils.isEmpty(projectStation.getStationLongitude())) {
                sourcePoint = new LatLng(Double.parseDouble(projectStation.getStationLatitude()), Double.parseDouble(projectStation.getStationLongitude()));
            } else {
                mBaiduMap.clear();
                return;
            }
            CoordinateConverter converter = new CoordinateConverter().from(CoordinateConverter.CoordType.GPS).coord(sourcePoint);
            LatLng targetPoint = converter.convert();
            Log.e("project", "point: " + targetPoint.toString());
            BitmapDescriptor markerBitmap = null;
            int statusCode = Integer.parseInt(projectStation.getStationStatus());
            if (statusCode >= 10 && statusCode <= 19) {
                markerBitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_mk_online);
            } else if (statusCode >= 20 && statusCode <= 29) {
                markerBitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_mk_offline);
            } else if (statusCode >= 30 && statusCode <= 39) {
                markerBitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_mk_warning);
            } else if (statusCode >= 40 && statusCode <= 49) {
                markerBitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_mk_error);
            }
            Bundle mBundle = new Bundle();
            mBundle.putString("stationName", projectStation.getStationName());
            switch (projectStation.getStationType()) {
                case "0":
                    mBundle.putString("stationType", "未知");
                    break;
                case "1":
                    mBundle.putString("stationType", "基准站");
                    break;
                case "2":
                    mBundle.putString("stationType", "移动站");
                    break;
            }
            OverlayOptions option = new MarkerOptions()
                    .position(targetPoint)
                    .icon(markerBitmap)
                    .extraInfo(mBundle);
            mBaiduMap.addOverlay(option);
        }
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void navigateTo(BDLocation location) {
        if (isFirstLocate) {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            if (projectStationStatus.getTotal() != 0) {
                ProjectResponse.ProjectListBean.StationListBean projectStation = projectStationList.get(0);
                LatLng sourcePoint = new LatLng(Double.parseDouble(projectStation.getStationLatitude()), Double.parseDouble(projectStation.getStationLongitude()));
                CoordinateConverter converter = new CoordinateConverter().from(CoordinateConverter.CoordType.GPS).coord(sourcePoint);
                ll = converter.convert();
            }
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(14f);
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
        //可选，设置发起定位请求的问题
        // ，int类型，单位ms
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
        option.setIgnoreKillProcess(true);
        //可选，设置是否收集Crash信息，默认收集，即参数为false
        option.SetIgnoreCacheException(false);
        //可选，V7.2版本新增功能
        //如果设置了该接口，首次启动定位时，会先判断当前Wi-Fi是否超出有效期，若超出有效期，会先重新扫描Wi-Fi，然后定位
        option.setWifiCacheTimeOut(5 * 60 * 1000);
        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
        option.setEnableSimulateGps(false);
        //设置是否需要详细地址信息
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relativeLayout:
                mScrollLayout.scrollToExit();
                break;
            case R.id.btn_amount:
                mPointList.clear();
                for (ProjectResponse.ProjectListBean.StationListBean projectStation :
                        projectStationList) {
                    mPointList.add(new MonitoringPoint(projectStation.getStationName(), projectStation.getStationType(), projectStation.getStationLastTime(), projectStation.getStationStatus()));
                }
                mPointsAdapter.setData(mPointList);
                mPointsAdapter.notifyDataSetChanged();
                break;
            case R.id.btn_online:
                mPointList.clear();
                for (ProjectResponse.ProjectListBean.StationListBean projectStation :
                        projectStationList) {
                    int statusCode = Integer.parseInt(projectStation.getStationStatus());
                    if (statusCode >= 10 && statusCode <= 19) {
                        mPointList.add(new MonitoringPoint(projectStation.getStationName(), projectStation.getStationType(), projectStation.getStationLastTime(), projectStation.getStationStatus()));
                    }
                }
                mPointsAdapter.setData(mPointList);
                mPointsAdapter.notifyDataSetChanged();
                break;
            case R.id.btn_warning:
                mPointList.clear();
                for (ProjectResponse.ProjectListBean.StationListBean projectStation :
                        projectStationList) {
                    int statusCode = Integer.parseInt(projectStation.getStationStatus());
                    if (statusCode >= 30 && statusCode <= 39) {
                        mPointList.add(new MonitoringPoint(projectStation.getStationName(), projectStation.getStationType(), projectStation.getStationLastTime(), projectStation.getStationStatus()));
                    }
                }
                mPointsAdapter.setData(mPointList);
                mPointsAdapter.notifyDataSetChanged();
                break;
            case R.id.btn_error:
                mPointList.clear();
                for (ProjectResponse.ProjectListBean.StationListBean projectStation :
                        projectStationList) {
                    int statusCode = Integer.parseInt(projectStation.getStationStatus());
                    if (statusCode >= 40 && statusCode <= 49) {
                        mPointList.add(new MonitoringPoint(projectStation.getStationName(), projectStation.getStationType(), projectStation.getStationLastTime(), projectStation.getStationStatus()));
                    }
                }
                mPointsAdapter.setData(mPointList);
                mPointsAdapter.notifyDataSetChanged();
                break;
            case R.id.btn_offline:
                mPointList.clear();
                for (ProjectResponse.ProjectListBean.StationListBean projectStation :
                        projectStationList) {
                    int statusCode = Integer.parseInt(projectStation.getStationStatus());
                    if (statusCode >= 20 && statusCode <= 29) {
                        mPointList.add(new MonitoringPoint(projectStation.getStationName(), projectStation.getStationType(), projectStation.getStationLastTime(), projectStation.getStationStatus()));
                    }
                }
                mPointsAdapter.setData(mPointList);
                mPointsAdapter.notifyDataSetChanged();
                break;
        }
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

    public void switchFragment(String projectName, ArrayList<String> stationNameList, int position) {
        //Fragment chartFragment = new ChartFragment();
        ChartFragment chartFragment = ChartFragment.newInstance(projectName, stationNameList, position);
        MainActivity activity = (MainActivity) getActivity();
        activity.setChartFragment(chartFragment);
        activity.setNowFragment(chartFragment);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(R.id.layout_home, chartFragment).hide(this);
        ft.addToBackStack("projectFragment");   //加入到返回栈中
        ft.commit();
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