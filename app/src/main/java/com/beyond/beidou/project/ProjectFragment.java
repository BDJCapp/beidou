package com.beyond.beidou.project;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
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
import com.beyond.beidou.MyRecycleView;
import com.beyond.beidou.R;
import com.beyond.beidou.adapter.MonitoringPointsAdapter;
import com.beyond.beidou.api.Api;
import com.beyond.beidou.api.ApiCallback;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.data.ChartFragment;
import com.beyond.beidou.entites.MonitoringPoint;
import com.beyond.beidou.entites.ProjectResponse;
import com.beyond.beidou.login.LoginActivity;
import com.beyond.beidou.util.LoginUtil;
import com.beyond.beidou.util.ScreenUtil;
import com.google.gson.Gson;
import com.yinglan.scrolllayout.ScrollLayout;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
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
    private HashMap<String, Object> mParams = new HashMap<String, Object>();
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

    private List<ProjectResponse.ProjectListBean> projectList = new ArrayList<>();
    private ProjectResponse.ProjectListBean.ProjectStationStatusBean projectStationStatus;
    private ArrayList<String> projectNameList = new ArrayList<>();
    private String netTime;
    private List<ProjectResponse.ProjectListBean.StationListBean> projectStationList = new ArrayList<>();
    private MonitoringPointsAdapter mPointsAdapter;
    private LinearLayoutManager layoutManager;
    private ArrayList<String> stationNameList = new ArrayList<>();
    private ArrayList<String> stationUUIDList = new ArrayList<>();

    private boolean isFirstLocate = true;
    private static boolean isFirstLogin = true;
    private static String presentProject;
    public static boolean isReLogin = false;
    private static boolean isFirstBindListener = true;
    private static final int LOADING = 1;
    private static final int LOADING_FINISH = 200;
    private ZLoadingDialog dialog;
    public Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case LOADING:
                    getData();
//                    while (!isFinishLoading) {
//                    }
//                    dialog.dismiss();
                    break;
                case LOADING_FINISH:
                    dialog.dismiss();
                    break;
            }
        }
    };


    private void doLoadingDialog() {
        handler.sendEmptyMessageDelayed(LOADING, 50);
        dialog.setLoadingBuilder(Z_TYPE.ROTATE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK)//颜色
                .setHintText("Loading...")
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .show();
    }

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
//                getData();
                doLoadingDialog();
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        getData();
        doLoadingDialog();
    }

    public static ProjectFragment newInstance() {
        ProjectFragment fragment = new ProjectFragment();
        return fragment;
        
    }

    public int initLayout() {
        SDKInitializer.initialize(getContext().getApplicationContext());
        return R.layout.fragment_project;
    }

    public void initView(View view) {
        mMapView = view.findViewById(R.id.bdmapView);
        mScrollLayout = view.findViewById(R.id.scrollLayout);
        mSpinner = view.findViewById(R.id.spinner);
        mRecyclerView = view.findViewById(R.id.recycle_view);
        mTvAmount = view.findViewById(R.id.tv_amount);
        mBtnAmount = view.findViewById(R.id.btn_amount);
        mBtnOnline = view.findViewById(R.id.btn_online);
        mBtnWarning = view.findViewById(R.id.btn_warning);
        mBtnError = view.findViewById(R.id.btn_error);
        mBtnOffline = view.findViewById(R.id.btn_offline);
        mTvTime = view.findViewById(R.id.tv_time);
        mIvRefresh = view.findViewById(R.id.iv_refresh);
        mIvSign = view.findViewById(R.id.iv_minus);

//        设置 setting
        mScrollLayout.setMinOffset(300);
        mScrollLayout.setMaxOffset(800);
        mScrollLayout.setExitOffset(400);
        mScrollLayout.setIsSupportExit(true);
        mScrollLayout.setToOpen();
//        mScrollLayout.setDuplicateParentStateEnabled(true);
//        mScrollLayout.setHasTransientState(true);
//        mScrollLayout.setActivated(true);

        mScrollLayout.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(oldScrollY > scrollY && mScrollLayout.getScrollY() == 800 - ScreenUtil.getScreenHeight(getActivity()) || oldScrollY < scrollY && mScrollLayout.getScrollY() == 800 - ScreenUtil.getScreenHeight(getActivity())){
                    mIvSign.setImageResource(R.drawable.ic_minus);
                }else if(oldScrollY > scrollY && mScrollLayout.getScrollY() == 400 - ScreenUtil.getScreenHeight(getActivity())){
                    mIvSign.setImageResource(R.drawable.ic_more);
                }else if(oldScrollY < scrollY && mScrollLayout.getScrollY() == -300){
                    mIvSign.setImageResource(R.drawable.ic_less);
                }
            }
        });

        mSpinner.setDropDownVerticalOffset(ScreenUtil.dip2px(getContext(), 30f));
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                try {
//                    Field field = AdapterView.class.getDeclaredField("mOldSelectedPosition");
//                    field.setAccessible(true);
//                    field.setInt(mSpinner, AdapterView.INVALID_POSITION);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                Log.wtf("onItemSelected", "position:   " + position);
                presentProject = (String) mSpinner.getItemAtPosition(position);
                isFirstLocate = true;
                MainActivity mMainActivity = (MainActivity) getActivity();
                mMainActivity.setPresentProject(presentProject);
                if (isFirstBindListener) {
                    isFirstBindListener = false;
                    return;
                }
//                mPointList.clear();
//                getData();
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

        final int width = ScreenUtil.getScreenXRatio(getActivity());
        final int height = ScreenUtil.getScreenXRatio(getActivity());

        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                //待改动调整
                mMapView.setZoomControlsPosition(new Point((int) (width * 0.88 + 0.5f), (int) (height * 0.73 + 0.5f)));
            }
        });
        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    mScrollLayout.scrollToExit();
                    mIvSign.setImageResource(R.drawable.ic_more);
                }
            }
        });

        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMaxAndMinZoomLevel(4f, 21f);
//        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        dialog = new ZLoadingDialog(getActivity());
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
        Log.wtf("getData", "================Begin================");
        projectNameList.clear();
        stationNameList.clear();
        stationUUIDList.clear();
        if (isReLogin) {
            isFirstLogin = true;
            isReLogin = false;
        }
        if (isFirstLogin) {
            String spVal = getStringFromSP("lastProjectName");
            presentProject = spVal;
            isFirstLogin = false;
        } else {
            MainActivity mMainActivity = (MainActivity) getActivity();
            presentProject = mMainActivity.getPresentProject();
            if (presentProject == null) {
                presentProject = "";
            }
            Log.e("presentProject", presentProject);
        }
        mParams.clear();
        projectList.clear();
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
                        //项目名为空则直接跳过
                        if (project.getProjectName().equals("")) {
                            continue;
                        }
                        projectNameList.add(project.getProjectName());
                    }
                    Log.wtf("projectNameList", projectNameList.toString());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), R.layout.item_select, projectNameList);
                            arrayAdapter.setDropDownViewResource(R.layout.item_drop);
                            mSpinner.setAdapter(arrayAdapter);
                            ProjectFragment.isFirstBindListener = true;

                            if (presentProject.equals("")) {
//                                presentProject = mSpinner.getSelectedItem() == null ? "" : mSpinner.getSelectedItem() .toString();
                                presentProject = mSpinner.getSelectedItem().toString();
                                MainActivity mMainActivity = (MainActivity) getActivity();
                                mMainActivity.setPresentProject(presentProject);
                                for (ProjectResponse.ProjectListBean project : projectList) {
                                    if (project.getProjectName().equals(presentProject)) {
                                        projectStationStatus = project.getProjectStationStatus();
                                        projectStationList = project.getStationList();
                                        for (ProjectResponse.ProjectListBean.StationListBean station : project.getStationList()) {
                                            stationNameList.add(station.getStationName());
                                            stationUUIDList.add(station.getStationUUID());
                                        }
                                    }
                                }
                            } else {
                                for (ProjectResponse.ProjectListBean project : projectList) {
                                    if (project.getProjectName().equals(presentProject)) {
                                        projectStationStatus = project.getProjectStationStatus();
                                        projectStationList = project.getStationList();
                                        mSpinner.setSelection(projectList.indexOf(project), true);
                                        for (ProjectResponse.ProjectListBean.StationListBean station : project.getStationList()) {
                                            stationNameList.add(station.getStationName());
                                            stationUUIDList.add(station.getStationUUID());

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
                                    if(!LoginUtil.isNetworkUsable(getContext())){
                                        return;
                                    }
                                    switchFragment(presentProject, stationNameList, position, stationUUIDList);
                                    Log.e("project", "you click " + position);
                                    MainActivity activity = (MainActivity) getActivity();
                                    activity.getNavigationView().setSelectedItemId(activity.getNavigationView().getMenu().getItem(1).getItemId());
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
                    Log.e("ResponseMsg", response.getResponseMsg());
                }
                //其他返回码重新登录
                else {
                    showToastSync("未请求到数据，请重新登录！");
                    isReLogin = true;
//                    ApiConfig.setSessionUUID("00000000-0000-0000-0000-000000000000");
                    while (!LoginUtil.getAccessToken(getContext()) && !LoginUtil.getSessionId(getContext())) {
                    }
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
//                isFinishLoading = true;
                handler.sendEmptyMessageDelayed(LOADING_FINISH,0);
            }

            @Override
            public void onFailure(Exception e) {
                dialog.dismiss();
                showToastSync("网络请求失败，请检查网络连接，稍后再试");
            }
        });
        Log.wtf("getData", "================End================");
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
        LatLng sourcePoint = null;
        for (ProjectResponse.ProjectListBean.StationListBean projectStation :
                projectStationList) {
            mPointList.add(new MonitoringPoint(projectStation.getStationName(), projectStation.getStationType(), projectStation.getStationLastTime(), projectStation.getStationStatus()));
            if (!TextUtils.isEmpty(projectStation.getStationLatitude()) && !TextUtils.isEmpty(projectStation.getStationLongitude())) {
                sourcePoint = new LatLng(Double.parseDouble(projectStation.getStationLatitude()), Double.parseDouble(projectStation.getStationLongitude()));
            } else {
//                mBaiduMap.clear();
                //若监测点无坐标则跳过
                continue;
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

    /**
     * 初始化前台服务
     */
//    private void initNotification () {
//        //设置后台定位
//        //android8.0及以上使用NotificationUtils
//        if ( Build.VERSION.SDK_INT >= 26) {
//            NotificationUtils notificationUtils = new NotificationUtils(this);
//            Notification.Builder builder = notificationUtils.getAndroidChannelNotification
//                    ("适配android 8限制后台定位功能", "正在后台定位");
//            mNotification = builder.build();
//        } else {
//            //获取一个Notification构造器
//            Notification.Builder builder = new Notification.Builder(MainActivity.this);
//            Intent nfIntent = new Intent(MainActivity.this, MainActivity.class);
//
//            builder.setContentIntent(PendingIntent.
//                    getActivity(MainActivity.this, 0, nfIntent, 0)) // 设置PendingIntent
//                    .setContentTitle("适配android 8限制后台定位功能") // 设置下拉列表里的标题
//                    .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
//                    .setContentText("正在后台定位") // 设置上下文内容
//                    .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
//
//            mNotification = builder.build(); // 获取构建好的Notification
//        }
//        mNotification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
//    }
    private void navigateTo(BDLocation location) {
        Double latitude = location.getLatitude(), longitude = location.getLongitude();
        LatLng ll = new LatLng(latitude, longitude);
        if (isFirstLocate) {
            if (projectStationStatus.getTotal() != 0) {
//                ProjectResponse.ProjectListBean.StationListBean projectStation = projectStationList.get(0);
//                if(projectStation == null){
//                    MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
//                    mBaiduMap.animateMapStatus(update);
//                    update = MapStatusUpdateFactory.zoomTo(16f);
//                    mBaiduMap.animateMapStatus(update);
//                    isFirstLocate = false;
//                    return;
//                }
//                if(!"".equals(projectStation.getStationLatitude()) && !"".equals(projectStation.getStationLongitude())){
//                    latitude = Double.parseDouble(projectStation.getStationLatitude());
//                    longitude = Double.parseDouble(projectStation.getStationLongitude());
//                    LatLng sourcePoint = new LatLng(latitude, longitude);
//                    CoordinateConverter converter = new CoordinateConverter().from(CoordinateConverter.CoordType.GPS).coord(sourcePoint);
//                    ll = converter.convert();
//                }

                for (ProjectResponse.ProjectListBean.StationListBean projectStation : projectStationList) {
                    if (projectStation == null) {
                        continue;
                    }
                    if (!"".equals(projectStation.getStationLatitude()) && !"".equals(projectStation.getStationLongitude())) {
                        latitude = Double.parseDouble(projectStation.getStationLatitude());
                        longitude = Double.parseDouble(projectStation.getStationLongitude());
                        LatLng sourcePoint = new LatLng(latitude, longitude);
                        CoordinateConverter converter = new CoordinateConverter().from(CoordinateConverter.CoordType.GPS).coord(sourcePoint);
                        ll = converter.convert();
                        break;
                    }
                }
            }
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(18f);
            mBaiduMap.animateMapStatus(update);
            isFirstLocate = false;
        } else {
            MyLocationData.Builder builder = new MyLocationData.Builder();
            builder.latitude(latitude).longitude(longitude);
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
                mPointList.clear();
                for (ProjectResponse.ProjectListBean.StationListBean projectStation :
                        projectStationList) {
                    mPointList.add(new MonitoringPoint(projectStation.getStationName(), projectStation.getStationType(), projectStation.getStationLastTime(), projectStation.getStationStatus()));
                }
                mPointsAdapter.setData(mPointList);
                mPointsAdapter.notifyDataSetChanged();
                mScrollLayout.scrollToClose();
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
                mScrollLayout.scrollToClose();
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
                mScrollLayout.scrollToClose();
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
                mScrollLayout.scrollToClose();
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
                mScrollLayout.scrollToClose();
                break;
            case R.id.iv_refresh:
                doLoadingDialog();
//               getData();
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
//            //mapView 销毁后不在处理新接收的位置
//            if (bdLocation == null || mMapView == null) {
//                return;
//            }
//            MyLocationData locData = new MyLocationData.Builder()
//                    .accuracy(bdLocation.getRadius())
//                    .direction(bdLocation.getDirection()).latitude(bdLocation.getLatitude())
//                    .longitude(bdLocation.getLongitude()).build();
//            mBaiduMap.setMyLocationData(locData);

        }
    }

    public void switchFragment(String projectName, ArrayList<String> stationNameList, int position, ArrayList<String> stationUUIDList) {

        ChartFragment chartFragment = ChartFragment.newInstance(projectName, stationNameList, position, stationUUIDList);
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
        if (mLocationClient != null) {
            mLocationClient.stop();
        }
        if(mBaiduMap != null){
            mBaiduMap.setMyLocationEnabled(false);
            mBaiduMap.clear();
        }
        if(mMapView != null){
            mMapView.onDestroy();
            mMapView = null;
        }
        super.onDestroy();
    }
}