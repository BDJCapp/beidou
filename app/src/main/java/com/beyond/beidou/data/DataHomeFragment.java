package com.beyond.beidou.data;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.adapter.DeviceListAdapter;
import com.beyond.beidou.api.Api;
import com.beyond.beidou.api.ApiCallback;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.entites.GetStationsResponse;
import com.beyond.beidou.entites.ProjectResponse;
import com.beyond.beidou.util.LogUtil;
import com.beyond.beidou.util.LoginUtil;
import com.google.gson.Gson;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author: 李垚
 */
public class DataHomeFragment extends BaseFragment {

    private Spinner mProjectSp;
    private RecyclerView mDevicesRv;
    private ZLoadingDialog mLoadingDlg;
    private SmartRefreshLayout mPageRefreshLayout;
    private ArrayList<String> mStationNameList = new ArrayList<>();
    private Map<String,Object> mProjectName2UUID = new HashMap<>();
    private Integer mPageNum = 1;
    private Integer mPageSize = 2;
    MainActivity mainActivity;
    private static final int LOADING = 1;
    private static final int DEVICE_LIST = 2;
    private static final int LOADING_FINISH = 200;
    private static final int REQUEST_FAILED = 400;
    private static final int REFRESH = 201;
    private static final int LOAD_MORE = 202;
    public Handler pHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case LOADING:
                    LogUtil.e("LOADING", "Loading===========");
                    setViews();
                    break;
                case DEVICE_LIST:
                    LogUtil.e("DEVICE_LIST", "DEVICE_LIST===========");
                    setmDevicesRv(mProjectSp.getSelectedItem().toString());
                    break;
                case LOADING_FINISH:
                    mLoadingDlg.dismiss();
                    break;
                case REFRESH:
                    setmDevicesRv(mProjectSp.getSelectedItem().toString());
                    mPageRefreshLayout.finishRefresh(true);
                    break;
                case LOAD_MORE:
                    setmDevicesRv(mProjectSp.getSelectedItem().toString());
                    mPageRefreshLayout.finishLoadMore(true);
                    break;
                case REQUEST_FAILED:
                    mLoadingDlg.dismiss();
                    showToast("网络请求失败，请检查网络连接，稍后再试");
                    break;
            }
        }
    };


    private void doLoadingDialog(int type) {
        if(type == 1){
            pHandler.sendEmptyMessageDelayed(LOADING, 150);
        }else{
            pHandler.sendEmptyMessageDelayed(DEVICE_LIST, 150);
        }

        mLoadingDlg.setLoadingBuilder(Z_TYPE.ROTATE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK)//颜色
                .setHintText("Loading...")
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data, container, false);
        initView(view);
        return view;
    }

    public void initView(View view) {
        mLoadingDlg = new ZLoadingDialog(getActivity());
        mainActivity = (MainActivity) getActivity();
        mProjectSp = view.findViewById(R.id.spinner_projectName);
        mDevicesRv = view.findViewById(R.id.rv_device);
        mPageRefreshLayout = view.findViewById(R.id.layout_refresh);
        mLoadingDlg = new ZLoadingDialog(getActivity());
        doLoadingDialog(1);
        pageInit();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (mProjectSp.getSelectedItem() != null)
        {
            if (mainActivity.getNowFragment() == mainActivity.getDataFragment() && !mProjectSp.getSelectedItem().toString().equals(((MainActivity)getActivity()).getPresentProject())) {
                mProjectSp.setSelection(mStationNameList.indexOf(((MainActivity)getActivity()).getPresentProject()), true);
                //设置spinner选中项
                doLoadingDialog(2);
//            setViews();
            }
        }
    }

    public void setViews() {
//        isFinishLoading = false;
        //设置Spinner
        final HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put("AccessToken", ApiConfig.getAccessToken());
        requestParams.put("SessionUUID", ApiConfig.getSessionUUID());

        Api.config(ApiConfig.GET_PROJECTS, requestParams).postRequest(getActivity(), new ApiCallback() {
            @Override
            public void onSuccess(String res) {
                Gson gson = new Gson();
                ProjectResponse projectResponse = gson.fromJson(res, ProjectResponse.class);
                final List<String> projectNameList = new ArrayList<>();
                for (int i = 0; i < projectResponse.getProjectList().size(); i++) {
                    String projectName = projectResponse.getProjectList().get(i).getProjectName();
                    projectNameList.add(projectName);
                    mProjectName2UUID.put(projectName,projectResponse.getProjectList().get(i).getProjectUUID());
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.item_select, projectNameList);
                        adapter.setDropDownViewResource(R.layout.item_drop);
                        mProjectSp.setAdapter(adapter);

                        //读取SP上次退出时选中的工程名，若没有。默认展示第一个工程
                        final MainActivity activity = (MainActivity) getActivity();
                        String presentProject = activity.getPresentProject();
                        if (!TextUtils.isEmpty(presentProject)) {
                            for (int i = 0; i < projectNameList.size(); i++) {
                                mStationNameList.add(projectNameList.get(i));
                                if (presentProject.equals(projectNameList.get(i))) {
                                    mProjectSp.setSelection(i, true);
                                    setmDevicesRv(presentProject);
                                }
                            }
                        }

                        mProjectSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                ((MainActivity)getActivity()).setPresentProject(mProjectSp.getSelectedItem().toString());
                                doLoadingDialog(2);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
                });
                pHandler.sendEmptyMessageDelayed(200,0);
//                isFinishLoading = true;
            }

            @Override
            public void onFailure(Exception e) {
                LogUtil.e("获取工程网络请求失败", e.getMessage());
                pHandler.sendEmptyMessageDelayed(REQUEST_FAILED,0);
            }
        });
    }

    public void setmDevicesRv(String selectedProject) {
        JSONObject jsonData = new JSONObject();
        JSONArray projectUUIDArray = new JSONArray();
        JSONArray pageArray = new JSONArray();
        try {
            jsonData.put("AccessToken",ApiConfig.getAccessToken());
            jsonData.put("SessionUUID",ApiConfig.getSessionUUID());
            jsonData.put("ProjectUUID",projectUUIDArray);
            jsonData.put("PageInfo",pageArray);

            projectUUIDArray.put(0,mProjectName2UUID.get(selectedProject));

            JSONObject pageObject = new JSONObject();
            pageObject.put("PageNumber",mPageNum.toString());
            pageObject.put("PageSize", mPageSize.toString());
            pageArray.put(0,pageObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Api.config(ApiConfig.GET_STATIONS).postJsonString(getActivity(), jsonData.toString(), new ApiCallback() {
            @Override
            public void onSuccess(String res) {
                Gson gson = new Gson();
                GetStationsResponse stationsResponse = gson.fromJson(res, GetStationsResponse.class);
                final ArrayList<String> deviceNames = new ArrayList<>();
                final List<String> deviceTypes = new ArrayList<>();
                final List<String> lastTimes = new ArrayList<>();
                final List<String> deviceStatus = new ArrayList<>();
                final ArrayList<String> stationUUIDList = new ArrayList<>();
                List<GetStationsResponse.StationListBean> stationList = stationsResponse.getStationList();
                for (int i = 0; i < stationList.size(); i++) {
                    deviceNames.add(stationList.get(i).getStationName());
                    deviceTypes.add(getStationType(stationList.get(i).getStationType()));
                    lastTimes.add(stationList.get(i).getStationLastTime());
                    deviceStatus.add(stationList.get(i).getStationStatus());
                    stationUUIDList.add(stationList.get(i).getStationUUID());
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DeviceListAdapter adapter = new DeviceListAdapter(deviceNames, deviceTypes, lastTimes, deviceStatus);
                        adapter.setLookDataListener(new DeviceListAdapter.onItemLookdataClockListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                if (LoginUtil.isNetworkUsable(getActivity()))
                                {
                                    switchFragment(mProjectSp.getSelectedItem().toString(), deviceNames, position, stationUUIDList);
                                }
                            }
                        });
                        mDevicesRv.setAdapter(adapter);
                        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
                        manager.setOrientation(RecyclerView.VERTICAL);
                        mDevicesRv.setLayoutManager(manager);
                    }
                });
                pHandler.sendEmptyMessageDelayed(LOADING_FINISH,0);
//                        isFinishLoading = true;
            }

            @Override
            public void onFailure(Exception e) {
                pHandler.sendEmptyMessageDelayed(REQUEST_FAILED,0);
            }
        });
    }


    //无刷新，直接请求GetProjects接口
    /*public void setmDevicesRv(String selectedProject) {
        HashMap<String, Object> requestParams = new HashMap<>();
        List<String> requestProjectList = new ArrayList<>();
        requestProjectList.add(selectedProject);

        requestParams.put("AccessToken", ApiConfig.getAccessToken());
        requestParams.put("SessionUUID", ApiConfig.getSessionUUID());
        requestParams.put("ProjectName", requestProjectList);

        SimpleDateFormat sdfTwo = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒E", Locale.getDefault());
        LogUtil.e("请求工程数据开始时间", sdfTwo.format(System.currentTimeMillis()));

        Api.config(ApiConfig.GET_PROJECTS, requestParams)
                .postRequest(getActivity(), new ApiCallback() {
                    @Override
                    public void onSuccess(String res) {

                        LogUtil.e("DataHome setmDevicesRv() 返回值",res);
//                        SimpleDateFormat sdfTwo = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒E", Locale.getDefault());
//                        LogUtil.e("请求工程数据结束时间", sdfTwo.format(System.currentTimeMillis()));

                        Gson gson = new Gson();
                        ProjectResponse projectResponse = gson.fromJson(res, ProjectResponse.class);
                        final ArrayList<String> deviceNames = new ArrayList<>();
                        final List<String> deviceTypes = new ArrayList<>();
                        final List<String> lastTimes = new ArrayList<>();
                        final List<String> deviceStatus = new ArrayList<>();
                        final ArrayList<String> stationUUIDList = new ArrayList<>();
                        List<ProjectResponse.ProjectListBean.StationListBean> stationList = projectResponse.getProjectList().get(0).getStationList();
                        for (int i = 0; i < stationList.size(); i++) {
                            deviceNames.add(stationList.get(i).getStationName());
                            deviceTypes.add(getStationType(stationList.get(i).getStationType()));
                            lastTimes.add(stationList.get(i).getStationLastTime());
                            deviceStatus.add(stationList.get(i).getStationStatus());
                            stationUUIDList.add(stationList.get(i).getStationUUID());
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DeviceListAdapter adapter = new DeviceListAdapter(deviceNames, deviceTypes, lastTimes, deviceStatus);
                                adapter.setLookDataListener(new DeviceListAdapter.onItemLookdataClockListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
                                        // LogUtil.e("查看的监测点", deviceNames.get(position));
                                        if (LoginUtil.isNetworkUsable(getActivity()))
                                        {
                                            switchFragment(mProjectSp.getSelectedItem().toString(), deviceNames, position, stationUUIDList);
                                        }
                                    }
                                });
                                mDevicesRv.setAdapter(adapter);
                                LinearLayoutManager manager = new LinearLayoutManager(getActivity());
                                manager.setOrientation(RecyclerView.VERTICAL);
                                mDevicesRv.setLayoutManager(manager);
                            }
                        });
                        pHandler.sendEmptyMessageDelayed(LOADING_FINISH,0);
//                        isFinishLoading = true;
                    }

                    @Override
                    public void onFailure(Exception e) {
                        pHandler.sendEmptyMessageDelayed(REQUEST_FAILED,0);
                    }
                });
    }*/

    public String getStationType(String stationType) {
        switch (stationType) {
            case "0":
                return "未知";
            case "1":
                return "基准站";
            case "2":
                return "移动站";
            default:
                return "错误";
        }
    }

    public void pageInit()
    {
        if (getActivity() != null){
            mPageRefreshLayout.setRefreshHeader(new ClassicsHeader(getActivity()));
            mPageRefreshLayout.setRefreshFooter(new ClassicsFooter(getActivity()));
        }
        mPageRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                pHandler.sendEmptyMessage(REFRESH);
            }
        });

        mPageRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (mPageSize < mStationNameList.size()){
                    mPageSize+=mPageSize;
                    pHandler.sendEmptyMessage(LOAD_MORE);
                }else {
                    mPageRefreshLayout.finishLoadMoreWithNoMoreData();
                }
            }
        });

    }

    public void switchFragment(String projectName, ArrayList<String> stationNameList, int devicePosition, ArrayList<String> stationUUIDList) {
        //Fragment chartFragment = new ChartFragment();
        ChartFragment chartFragment = ChartFragment.newInstance(projectName, stationNameList, devicePosition, stationUUIDList);
        MainActivity activity = (MainActivity) getActivity();
        activity.setChartFragment(chartFragment);
        activity.setNowFragment(chartFragment);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(R.id.layout_home, chartFragment).hide(this);
        ft.addToBackStack("DataHomeFragment");   //加入到返回栈中
        ft.commit();
    }
}
