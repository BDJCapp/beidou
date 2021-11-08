package com.beyond.beidou.project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

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
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.util.*;
import com.beyond.beidou.views.MyRecycleView;
import com.beyond.beidou.R;
import com.beyond.beidou.adapter.MonitoringPointsAdapter;
import com.beyond.beidou.api.Api;
import com.beyond.beidou.api.ApiCallback;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.data.ChartFragment;
import com.beyond.beidou.entites.MonitoringPoint;
import com.beyond.beidou.entites.ProjectResponse;
import com.beyond.beidou.login.LoginActivity;
import com.google.gson.Gson;
import com.yinglan.scrolllayout.ScrollLayout;
import com.yinglan.scrolllayout.content.ContentScrollView;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;

public class ProjectFragment extends BaseFragment implements View.OnClickListener {

    private LocationClient mLocationClient;
    private BaiduMap mBaiduMap;
    private MapView mMapView;
    private ScrollLayout mScrollLayout;
    private Spinner mSpinner;
    private List<MonitoringPoint> mPointList = new ArrayList<>();
    private MyRecycleView mRecyclerView;
    private HashMap<String, Object> mParams = new HashMap<>();
    private TextView mTvAmount;
    private Button mBtnAmount;
    private Button mBtnOnline;
    private Button mBtnWarning;
    private Button mBtnError;
    private Button mBtnOffline;
    private TextView mTvTime;
    private ImageView mIvRefresh;
    private ToggleButton mToggleButton;
    private ImageView mIvSign;
    private ContentScrollView mScrollView;

    private List<ProjectResponse.ProjectListBean> mProjectList = new ArrayList<>();
    private ProjectResponse.ProjectListBean.ProjectStationStatusBean mProjectStationStatus;
    private ArrayList<String> mProjectNameList = new ArrayList<>();
    private List<ProjectResponse.ProjectListBean.StationListBean> mProjectStationList = new ArrayList<>();
    private MonitoringPointsAdapter mPointsAdapter;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<String> mStationNameList = new ArrayList<>();
    private ArrayList<String> mStationUUIDList = new ArrayList<>();

    private static boolean sIsFirstLogin = true;
    private static String mPresentProject;
    public static boolean sIsReLogin = false;
    private static boolean sIsFirstBindListener = true;
    private static final int LOADING = 1;
    private static final int LOADING_FINISH = 200;
    private ZLoadingDialog mDialog;
    public Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case LOADING:
                    getData();
                    mBtnAmount.setBackgroundResource(R.drawable.btn_amount_selected_bg);
                    mBtnAmount.animate().scaleX(1.25f).scaleY(1.25f).setDuration(100).start();
                    clearAnimation(R.id.btn_online);
                    clearAnimation(R.id.btn_warning);
                    clearAnimation(R.id.btn_error);
                    clearAnimation(R.id.btn_offline);
                    break;
                case LOADING_FINISH:
                    mDialog.dismiss();
                    break;
            }
        }
    };

    private MainActivity mMainActivity = null;
    private boolean isMatch = false;
    private ProjectResponse projectResponse;

    private void doLoadingDialog() {
        mHandler.sendEmptyMessageDelayed(LOADING, 50);
        mDialog.setLoadingBuilder(Z_TYPE.ROTATE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK)//颜色
                .setHintText("加载中")
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mMainActivity = (MainActivity) getActivity();
        View view = inflater.inflate(initLayout(), container, false);
        initView(view);
        mLocationClient = new LocationClient(mMainActivity.getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        mLayoutManager = new LinearLayoutManager(mMainActivity);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mPointsAdapter = new MonitoringPointsAdapter();
        requestLocation(); //请求百度地图位置
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
            if (!mPresentProject.equals(mMainActivity.getPresentProject())) {
                doLoadingDialog();
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        doLoadingDialog();
    }

    public int initLayout() {
        SDKInitializer.initialize(mMainActivity.getApplicationContext());
        return R.layout.fragment_project;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initView(final View view) {

        mScrollView = view.findViewById(R.id.scrollView);
        mMapView = view.findViewById(R.id.bdmapView);
        mScrollLayout = view.findViewById(R.id.scrollLayout);
        mSpinner = view.findViewById(R.id.spinner);
        mRecyclerView = view.findViewById(R.id.recycle_view);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mLayoutManager.findFirstCompletelyVisibleItemPosition() != 0) {
                            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    }
                });
                return false;
            }
        });


        mTvAmount = view.findViewById(R.id.tv_amount);
        mBtnAmount = view.findViewById(R.id.btn_amount);
        mBtnOnline = view.findViewById(R.id.btn_online);
        mBtnWarning = view.findViewById(R.id.btn_warning);
        mBtnError = view.findViewById(R.id.btn_error);
        mBtnOffline = view.findViewById(R.id.btn_offline);
        mTvTime = view.findViewById(R.id.tv_time);
        mIvRefresh = view.findViewById(R.id.iv_refresh);
        mIvSign = view.findViewById(R.id.iv_minus);

        //滑动列表设置
        mScrollLayout.setMinOffset(300);
        mScrollLayout.setMaxOffset(800);
        mScrollLayout.setExitOffset(400);
        mScrollLayout.setIsSupportExit(true);
        mScrollLayout.setToOpen();

        mScrollLayout.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (-300 >= scrollY && scrollY > 800 - ScreenUtil.getScreenHeight(mMainActivity)) {
                    mIvSign.setImageResource(R.drawable.ic_less);
                } else if (400 - ScreenUtil.getScreenHeight(mMainActivity) <= scrollY && scrollY < 800 - ScreenUtil.getScreenHeight(mMainActivity)) {
                    mIvSign.setImageResource(R.drawable.ic_more);
                } else if (scrollY == 800 - ScreenUtil.getScreenHeight(mMainActivity)) {
                    mIvSign.setImageResource(R.drawable.ic_minus);
                }
            }
        });

        mSpinner.setDropDownVerticalOffset(ScreenUtil.dip2px(mMainActivity, 30f));
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresentProject = (String) mSpinner.getItemAtPosition(position);
                mMainActivity.setPresentProject(mPresentProject);
                if (sIsFirstBindListener) {
                    sIsFirstBindListener = false;
                    return;
                }
                doLoadingDialog();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                showToast("没有选中任何工程");
            }
        });
        mBtnAmount.setOnClickListener(this);
        mBtnOnline.setOnClickListener(this);
        mBtnWarning.setOnClickListener(this);
        mBtnError.setOnClickListener(this);
        mBtnOffline.setOnClickListener(this);
        mIvRefresh.setOnClickListener(this);

        final int width = ScreenUtil.getScreenXRatio(mMainActivity);
        final int height = ScreenUtil.getScreenXRatio(mMainActivity);

        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMapView.setZoomControlsPosition(new Point((int) (width * 0.88 + 0.5f), (int) (height * 0.73 + 0.5f)));
            }
        });
        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mScrollLayout.scrollToExit();
                    mIvSign.setImageResource(R.drawable.ic_more);
                    mBaiduMap.hideInfoWindow();
                }
            }
        });

        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMaxAndMinZoomLevel(4f, 21f);
        mDialog = new ZLoadingDialog(mMainActivity);
        mToggleButton = view.findViewById(R.id.toggleButton);
        mToggleButton.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                } else {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                }
            }
        });
    }

    private void getData() {
        if (FileUtil.fileExist(mMainActivity.getCacheDir() + "/" + "projectCache-" + getStringFromSP("presentPlatform"))) {
            Gson gson = new Gson();
            projectResponse = gson.fromJson(FileUtil.getProjectCache(mMainActivity), ProjectResponse.class);
            extractData();
            //run a thread
            LogUtil.e("projectFragment", "get data...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mParams.put("AccessToken", ApiConfig.getAccessToken());
                    mParams.put("SessionUUID", ApiConfig.getSessionUUID());
                    Api.config(ApiConfig.GET_PROJECTS, mParams).postRequest(mMainActivity, new ApiCallback() {
                        @Override
                        public void onSuccess(final String res) {
                            FileUtil.saveProjectCache(mMainActivity, res);
                            Gson gson = new Gson();
                            projectResponse = gson.fromJson(res, ProjectResponse.class);

                            extractData();
                            LogUtil.e("projectFragment", "extract complete");
                        }
                        @Override
                        public void onFailure(Exception e) {
                            mHandler.sendEmptyMessageDelayed(LOADING_FINISH, 0);
                        }
                    });
                }
            }).start();
        } else {
            mParams.put("AccessToken", ApiConfig.getAccessToken());
            mParams.put("SessionUUID", ApiConfig.getSessionUUID());
            Api.config(ApiConfig.GET_PROJECTS, mParams).postRequest(mMainActivity, new ApiCallback() {
                @Override
                public void onSuccess(final String res) {
                    FileUtil.saveProjectCache(mMainActivity, res);
                    Gson gson = new Gson();
                    projectResponse = gson.fromJson(res, ProjectResponse.class);
                    extractData();
                }
                @Override
                public void onFailure(Exception e) {
                    mHandler.sendEmptyMessageDelayed(LOADING_FINISH, 0);
                }
            });
        }
    }

    private void extractData(){
        mProjectNameList.clear();
        mStationNameList.clear();
        mStationUUIDList.clear();
        if (sIsReLogin) {
            sIsFirstLogin = true;
            sIsReLogin = false;
        }
        if (sIsFirstLogin) {
            mPresentProject = getStringFromSP("lastProjectName");
            sIsFirstLogin = false;
        } else {
            mPresentProject = mMainActivity.getPresentProject();
            if (mPresentProject == null) {
                mPresentProject = "";
            }
        }
        mParams.clear();
        if (mProjectList != null) {
            mProjectList.clear();
        }
        if (Integer.parseInt(projectResponse.getResponseCode()) == 200) {
            mProjectList = projectResponse.getProjectList();
            if (mProjectList == null) {
                back2Login();
            }
            for (ProjectResponse.ProjectListBean project : mProjectList) {
                //项目名为空则直接跳过
                if (project.getProjectName().equals("")) {
                    continue;
                }
                mProjectNameList.add(project.getProjectName());
            }
            mMainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mMainActivity, R.layout.item_select, mProjectNameList);
                    arrayAdapter.setDropDownViewResource(R.layout.item_drop);
                    mSpinner.setAdapter(arrayAdapter);
                    ProjectFragment.sIsFirstBindListener = true;

                    if (mPresentProject.equals("")) {
                        mPresentProject = mSpinner.getSelectedItem().toString();
                        mMainActivity.setPresentProject(mPresentProject);
                        for (ProjectResponse.ProjectListBean project : mProjectList) {
                            if (project.getProjectName().equals(mPresentProject)) {
                                mProjectStationStatus = project.getProjectStationStatus();
                                mProjectStationList = project.getStationList();
                                for (ProjectResponse.ProjectListBean.StationListBean station : project.getStationList()) {
                                    mStationNameList.add(station.getStationName());
                                    mStationUUIDList.add(station.getStationUUID());
                                }
                            }
                        }
                    } else {
                        for (ProjectResponse.ProjectListBean project : mProjectList) {
                            if (project.getProjectName().equals(mPresentProject)) {
                                mProjectStationStatus = project.getProjectStationStatus();
                                mProjectStationList = project.getStationList();
                                mSpinner.setSelection(mProjectList.indexOf(project), true);
                                for (ProjectResponse.ProjectListBean.StationListBean station : project.getStationList()) {
                                    mStationNameList.add(station.getStationName());
                                    mStationUUIDList.add(station.getStationUUID());
                                }
                                isMatch = true;
                            }
                        }
                        //isMatch解决切换平台时首页数据不匹配问题，以下为切换了平台后的逻辑
                        if (!isMatch) {
                            mPresentProject = mSpinner.getSelectedItem().toString();
                            mMainActivity.setPresentProject(mPresentProject);
                            for (ProjectResponse.ProjectListBean project : mProjectList) {
                                if (project.getProjectName().equals(mPresentProject)) {
                                    mProjectStationStatus = project.getProjectStationStatus();
                                    mProjectStationList = project.getStationList();
                                    for (ProjectResponse.ProjectListBean.StationListBean station : project.getStationList()) {
                                        mStationNameList.add(station.getStationName());
                                        mStationUUIDList.add(station.getStationUUID());
                                    }
                                }
                            }
                            isMatch = false;
                        }
                    }
                    if (mProjectStationStatus == null) {
                        back2Login();
                        return;
                    } else {
//                                Log.e("mProjectStationStatus", mProjectStationStatus.toString());
                        mTvAmount.setText(String.valueOf(mProjectStationStatus.getTotal()));
                        mBtnAmount.setText("总数\n" + mProjectStationStatus.getTotal());
                        mBtnOnline.setText("在线\n" + mProjectStationStatus.getOnline());
                        mBtnWarning.setText("警告\n" + mProjectStationStatus.getWarning());
                        mBtnError.setText("故障\n" + mProjectStationStatus.getError());
                        mBtnOffline.setText("离线\n" + mProjectStationStatus.getOffline());
                    }
                    //初始化监测点数据
                    initStationList();
//                            mPointsAdapter = new MonitoringPointsAdapter(mPointList);
                    ListUtil.sort(mPointList, true, "name", "uuid");
                    mPointsAdapter.setData(mPointList);
                    mRecyclerView.setAdapter(mPointsAdapter);
                    mPointsAdapter.addFooterView(LayoutInflater.from(getActivity()).inflate(R.layout.item_footer_layout, null));
                    mPointsAdapter.setOnItemClickListener(new MonitoringPointsAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            if (!LoginUtil.isNetworkUsable(mMainActivity)) {
                                return;
                            }
                            switchFragment(mPresentProject, mStationNameList, position, mStationUUIDList);
                            mMainActivity.getNavigationView().setSelectedItemId(mMainActivity.getNavigationView().getMenu().getItem(1).getItemId());
                        }
                    });

                    navigate();

                    mPointsAdapter.setOnAreaClickListener(new MonitoringPointsAdapter.OnAreaClickListener() {
                        @Override
                        public void onAreaClick(View view, int position) {
                            for (ProjectResponse.ProjectListBean.StationListBean projectStation :
                                    mProjectStationList) {
                                if (mStationUUIDList.get(position).equals(projectStation.getStationUUID())) {
                                    LatLng ll;
                                    double latitude, longitude;
                                    if (!"".equals(projectStation.getStationLatitude()) && !"".equals(projectStation.getStationLongitude())) {
                                        latitude = Double.parseDouble(projectStation.getStationLatitude());
                                        longitude = Double.parseDouble(projectStation.getStationLongitude());
                                        LatLng sourcePoint = new LatLng(latitude, longitude);
                                        CoordinateConverter converter = new CoordinateConverter().from(CoordinateConverter.CoordType.GPS).coord(sourcePoint);
                                        ll = converter.convert();
                                    } else {
                                        break;
                                    }
                                    MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                                    mBaiduMap.animateMapStatus(update);
                                    update = MapStatusUpdateFactory.zoomTo(18f);
                                    mBaiduMap.animateMapStatus(update);
                                    return;
                                }
                            }
                            mMainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast("监测点暂无位置数据");
                                }
                            });
                        }
                    });

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault());
                    Date date = new Date(System.currentTimeMillis());
                    mTvTime.setText(simpleDateFormat.format(date));
                    //设置地图marker覆盖物
                    mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            View window = LayoutInflater.from(mMainActivity).inflate(R.layout.popupwindow_showinfo, null, false);
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
        } else {
            back2Login();
        }
        mHandler.sendEmptyMessageDelayed(LOADING_FINISH, 0);
    }

    private void back2Login() {
        mHandler.sendEmptyMessageDelayed(LOADING_FINISH, 0);
        sIsFirstLogin = true;
        sIsFirstBindListener = true;
        sIsReLogin = true;
        showToastSync("未请求到数据，请稍后再试！");
    }

    private void initStationList() {
        mBaiduMap.clear();
        mPointList.clear();
        LatLng sourcePoint;
        for (ProjectResponse.ProjectListBean.StationListBean projectStation :
                mProjectStationList) {
            mPointList.add(new MonitoringPoint(projectStation.getStationName(), projectStation.getStationType(), projectStation.getStationLastTime(), projectStation.getStationStatus(), projectStation.getDeviceUUID()));
            if (!TextUtils.isEmpty(projectStation.getStationLatitude()) && !TextUtils.isEmpty(projectStation.getStationLongitude())) {
                sourcePoint = new LatLng(Double.parseDouble(projectStation.getStationLatitude()), Double.parseDouble(projectStation.getStationLongitude()));
            } else {
                //若监测点无坐标则跳过
                continue;
            }
            CoordinateConverter converter = new CoordinateConverter().from(CoordinateConverter.CoordType.GPS).coord(sourcePoint);
            LatLng targetPoint = converter.convert();
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
            } else {
                markerBitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_mk_unknown);
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
                    //设置标记的锚点
                    .anchor(0.5f, 0.96875f)
                    .extraInfo(mBundle);
            mBaiduMap.addOverlay(option);
        }
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void navigate() {
        double latitude, longitude;
        LatLng ll;
        boolean hasData = false;
        if (mProjectStationStatus != null && mProjectStationStatus.getTotal() != 0) {
            for (ProjectResponse.ProjectListBean.StationListBean projectStation : mProjectStationList) {
                if (projectStation == null) {
                    continue;
                }
                if (!"".equals(projectStation.getStationLatitude()) && !"".equals(projectStation.getStationLongitude())) {
                    latitude = Double.parseDouble(projectStation.getStationLatitude());
                    longitude = Double.parseDouble(projectStation.getStationLongitude());
                    LatLng sourcePoint = new LatLng(latitude, longitude);
                    CoordinateConverter converter = new CoordinateConverter().from(CoordinateConverter.CoordType.GPS).coord(sourcePoint);
                    ll = converter.convert();
                    MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                    mBaiduMap.animateMapStatus(update);
                    update = MapStatusUpdateFactory.zoomTo(18f);
                    mBaiduMap.animateMapStatus(update);
                    hasData = true;
                    break;
                }
            }
        }
        if (!hasData) {
            showToast("项目所有监测点暂无位置数据！");
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
        // int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置为非0，需设置1000ms以上才有效
        option.setScanSpan(3000);
        //可选，设置是否使用gps，默认false
        //使用高精度和仅使用设备两种定位模式的，参数必须设置为true
        option.setOpenGps(true);
        //可选，设置是否当GPS有效时按照1s/1次频率输出GPS结果，默认false
        option.setLocationNotify(true);
        //可选，定位SDK内部是一个service，并放到了独立进程
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)
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
            case R.id.btn_amount:
                mScrollLayout.scrollToClose();
                v.setBackgroundResource(R.drawable.btn_amount_selected_bg);
                v.animate().scaleX(1.25f).scaleY(1.25f).setDuration(100).start();
                clearAnimation(R.id.btn_online);
                clearAnimation(R.id.btn_warning);
                clearAnimation(R.id.btn_error);
                clearAnimation(R.id.btn_offline);
                mPointList.clear();
                for (ProjectResponse.ProjectListBean.StationListBean projectStation :
                        mProjectStationList) {
                    mPointList.add(new MonitoringPoint(projectStation.getStationName(), projectStation.getStationType(), projectStation.getStationLastTime(), projectStation.getStationStatus(), projectStation.getDeviceUUID()));
                }
                break;
            case R.id.btn_online:
                mScrollLayout.scrollToClose();
                v.setBackgroundResource(R.drawable.btn_online_selected_bg);
                v.animate().scaleX(1.25f).scaleY(1.25f).setDuration(100).start();
                clearAnimation(R.id.btn_amount);
                clearAnimation(R.id.btn_warning);
                clearAnimation(R.id.btn_error);
                clearAnimation(R.id.btn_offline);
                mPointList.clear();
                for (ProjectResponse.ProjectListBean.StationListBean projectStation :
                        mProjectStationList) {
                    int statusCode = Integer.parseInt(projectStation.getStationStatus());
                    if (statusCode >= 10 && statusCode <= 19) {
                        mPointList.add(new MonitoringPoint(projectStation.getStationName(), projectStation.getStationType(), projectStation.getStationLastTime(), projectStation.getStationStatus(), projectStation.getDeviceUUID()));
                    }
                }
                break;
            case R.id.btn_warning:
                mScrollLayout.scrollToClose();
                v.setBackgroundResource(R.drawable.btn_warning_selected_bg);
                v.animate().scaleX(1.25f).scaleY(1.25f).setDuration(100).start();
                clearAnimation(R.id.btn_amount);
                clearAnimation(R.id.btn_online);
                clearAnimation(R.id.btn_error);
                clearAnimation(R.id.btn_offline);
                mPointList.clear();
                for (ProjectResponse.ProjectListBean.StationListBean projectStation :
                        mProjectStationList) {
                    int statusCode = Integer.parseInt(projectStation.getStationStatus());
                    if (statusCode >= 30 && statusCode <= 39) {
                        mPointList.add(new MonitoringPoint(projectStation.getStationName(), projectStation.getStationType(), projectStation.getStationLastTime(), projectStation.getStationStatus(), projectStation.getDeviceUUID()));
                    }
                }
                break;
            case R.id.btn_error:
                mScrollLayout.scrollToClose();
                v.setBackgroundResource(R.drawable.btn_error_selected_bg);
                v.animate().scaleX(1.25f).scaleY(1.25f).setDuration(100).start();
                clearAnimation(R.id.btn_amount);
                clearAnimation(R.id.btn_online);
                clearAnimation(R.id.btn_warning);
                clearAnimation(R.id.btn_offline);
                mPointList.clear();
                for (ProjectResponse.ProjectListBean.StationListBean projectStation :
                        mProjectStationList) {
                    int statusCode = Integer.parseInt(projectStation.getStationStatus());
                    if (statusCode >= 40 && statusCode <= 49) {
                        mPointList.add(new MonitoringPoint(projectStation.getStationName(), projectStation.getStationType(), projectStation.getStationLastTime(), projectStation.getStationStatus(), projectStation.getDeviceUUID()));
                    }
                }
                break;
            case R.id.btn_offline:
                mScrollLayout.scrollToClose();
                v.setBackgroundResource(R.drawable.btn_offline_selected_bg);
                v.animate().scaleX(1.25f).scaleY(1.25f).setDuration(100).start();
                clearAnimation(R.id.btn_amount);
                clearAnimation(R.id.btn_online);
                clearAnimation(R.id.btn_warning);
                clearAnimation(R.id.btn_error);
                mPointList.clear();
                for (ProjectResponse.ProjectListBean.StationListBean projectStation :
                        mProjectStationList) {
                    int statusCode = Integer.parseInt(projectStation.getStationStatus());
                    if (statusCode >= 20 && statusCode <= 29) {
                        mPointList.add(new MonitoringPoint(projectStation.getStationName(), projectStation.getStationType(), projectStation.getStationLastTime(), projectStation.getStationStatus(), projectStation.getDeviceUUID()));
                    }
                }
                break;
            case R.id.iv_refresh:
                FileUtil.fileDelete(mMainActivity.getCacheDir() + "/projectCache-" + getStringFromSP("presentPlatform"));
                doLoadingDialog();
                break;
        }
        ListUtil.sort(mPointList, true, "name", "uuid");
        mPointsAdapter.setData(mPointList);
        mPointsAdapter.notifyDataSetChanged();
    }

    private void clearAnimation(int drawable) {
        switch (drawable) {
            case R.id.btn_amount:
                mBtnAmount.setBackgroundResource(R.drawable.btn_amount_bg);
                mBtnAmount.animate().scaleX(1.0f).scaleY(1.0f).setDuration(10).start();
                break;
            case R.id.btn_online:
                mBtnOnline.setBackgroundResource(R.drawable.btn_online_bg);
                mBtnOnline.animate().scaleX(1.0f).scaleY(1.0f).setDuration(10).start();
                break;
            case R.id.btn_warning:
                mBtnWarning.setBackgroundResource(R.drawable.btn_warning_bg);
                mBtnWarning.animate().scaleX(1.0f).scaleY(1.0f).setDuration(10).start();
                break;
            case R.id.btn_error:
                mBtnError.setBackgroundResource(R.drawable.btn_error_bg);
                mBtnError.animate().scaleX(1.0f).scaleY(1.0f).setDuration(10).start();
                break;
            case R.id.btn_offline:
                mBtnOffline.setBackgroundResource(R.drawable.btn_offline_bg);
                mBtnOffline.animate().scaleX(1.0f).scaleY(1.0f).setDuration(10).start();
                break;
        }
    }

    private class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            MyLocationData.Builder builder = new MyLocationData.Builder();
            builder.latitude(bdLocation.getLatitude()).longitude(bdLocation.getLongitude());

            MyLocationData data = builder.build();
            mBaiduMap.setMyLocationData(data);

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
        }
    }

    public void switchFragment(String projectName, ArrayList<String> stationNameList, int position, ArrayList<String> stationUUIDList) {

        ChartFragment chartFragment = ChartFragment.newInstance(projectName, stationNameList, position, stationUUIDList);
        mMainActivity.setChartFragment(chartFragment);
        mMainActivity.setNowFragment(chartFragment);
        FragmentManager fragmentManager = getFragmentManager();
        assert fragmentManager != null;
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(R.id.layout_home, chartFragment).hide(this);
//        ft.addToBackStack("projectFragment");   //加入到返回栈中
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
        if (mLocationClient != null) {
            mLocationClient.stop();
        }
        if (mBaiduMap != null) {
            mBaiduMap.setMyLocationEnabled(false);
            mBaiduMap.clear();
        }
        if (mMapView != null) {
            mMapView.onDestroy();
            mMapView = null;
        }
        sIsFirstLogin = true;
        sIsFirstBindListener = true;
        sIsReLogin = true;
        super.onDestroy();
    }
}