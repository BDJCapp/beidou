package com.beyond.beidou.data;

import android.graphics.Color;
import android.os.Bundle;
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
import com.beyond.beidou.entites.ProjectResponse;
import com.beyond.beidou.util.DateUtil;
import com.beyond.beidou.util.LogUtil;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * @author: 李垚
 */
public class DataHomeFragment extends BaseFragment {

    private Spinner spProjectName;
    private RecyclerView deviceList;
    ZLoadingDialog dialog;
    MainActivity activity;

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
        dialog = new ZLoadingDialog(getActivity());
        activity = (MainActivity) getActivity();
        spProjectName = view.findViewById(R.id.spinner_projectName);
        deviceList = view.findViewById(R.id.rv_device);
        setViews();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (activity.getNowFragment() == activity.getDataFragment())
        {
            setViews();
        }
    }

    public void setViews()
    {
        //设置Spinner
        final HashMap<String,Object> requestParams = new HashMap<>();
        requestParams.put("AccessToken", ApiConfig.getAccessToken());
        requestParams.put("SessionUUID",ApiConfig.getSessionUUID());

//        dialog.setLoadingBuilder( Z_TYPE.ROTATE_CIRCLE)//设置类型
//                .setLoadingColor(Color.BLACK)//颜色
//                .setHintText("Loading...")
//                .show();

        Api.config(ApiConfig.GET_PROJECTS,requestParams).postRequest(getActivity(), new ApiCallback() {
            @Override
            public void onSuccess(String res) {
                Gson gson = new Gson();
                ProjectResponse projectResponse = gson.fromJson(res, ProjectResponse.class);
                final List<String> projectNameList = new ArrayList<>();
                for (int i = 0; i < projectResponse.getProjectList().size(); i++) {
                    projectNameList.add(projectResponse.getProjectList().get(i).getProjectName());
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, projectNameList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                        spProjectName.setAdapter(adapter);

                        //读取SP上次退出时选中的工程名，若没有。默认展示第一个工程
                        final MainActivity activity = (MainActivity) getActivity();
                        String presentProject = activity.getPresentProject();
                        if (!TextUtils.isEmpty(presentProject))
                        {
                            for (int i = 0; i < projectNameList.size(); i++) {
                                if (presentProject.equals(projectNameList.get(i)))
                                {
                                    spProjectName.setSelection(i,true);
                                    setDeviceList(presentProject);
                                }
                            }
                        }



                        spProjectName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                                SimpleDateFormat sdfTwo =new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒E", Locale.getDefault());
//                                LogUtil.e("Spinner切换工程的时间",sdfTwo.format(System.currentTimeMillis()));
                                setDeviceList(spProjectName.getSelectedItem().toString());  //设置设备列表
                                activity.setPresentProject(spProjectName.getSelectedItem().toString());
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                LogUtil.e("获取工程网络请求失败",e.getMessage());
            }
        });

    }

    public void setDeviceList(String selectedProject)
    {
        HashMap<String,Object> requestParams = new HashMap<>();
        List<String> requestProjectList = new ArrayList<>();
        requestProjectList.add(selectedProject);
        requestParams.put("AccessToken", ApiConfig.getAccessToken());
        requestParams.put("SessionUUID",ApiConfig.getSessionUUID());
        requestParams.put("ProjectName",requestProjectList);

        SimpleDateFormat sdfTwo =new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒E", Locale.getDefault());
        LogUtil.e("请求工程数据开始时间",sdfTwo.format(System.currentTimeMillis()));



        Api.config(ApiConfig.GET_PROJECTS,requestParams)
                .postRequest(getActivity(), new ApiCallback() {
                    @Override
                    public void onSuccess(String res) {

                        SimpleDateFormat sdfTwo =new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒E", Locale.getDefault());
                        LogUtil.e("请求工程数据结束时间",sdfTwo.format(System.currentTimeMillis()));

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
                                        switchFragment(spProjectName.getSelectedItem().toString(), deviceNames,position,stationUUIDList);
                                    }
                                });
                                deviceList.setAdapter(adapter);
                                LinearLayoutManager manager = new LinearLayoutManager(getActivity());
                                manager.setOrientation(RecyclerView.VERTICAL);
                                deviceList.setLayoutManager(manager);

//                                dialog.dismiss();

                            }
                        });
                    }
                    @Override
                    public void onFailure(Exception e) {

                    }
                });
    }

    public String getStationType(String stationType)
    {
        switch (stationType)
        {
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

    public void switchFragment(String projectName,ArrayList<String> stationNameList,int devicePosition,ArrayList<String> stationUUIDList) {
        //Fragment chartFragment = new ChartFragment();
        ChartFragment chartFragment = ChartFragment.newInstance(projectName,stationNameList,devicePosition,stationUUIDList);
        MainActivity activity = (MainActivity) getActivity();
        activity.setChartFragment(chartFragment);
        activity.setNowFragment(chartFragment);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(R.id.layout_home, chartFragment).hide(this);
        ft.addToBackStack("DataHomeFragment");   //加入到返回栈中
        ft.commit();
    }

    public void test() {

        SimpleDateFormat secondFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        LogUtil.e("1小时开始时间",secondFormat.format(DateUtil.getHourBegin(1)));
        LogUtil.e("1小时结束时间",secondFormat.format(DateUtil.getHourEnd()));
        LogUtil.e("6小时开始时间",secondFormat.format(DateUtil.getHourBegin(6)));
        LogUtil.e("6小时结束时间",secondFormat.format(DateUtil.getHourEnd()));
        LogUtil.e("12小时开始时间",secondFormat.format(DateUtil.getHourBegin(12)));
        LogUtil.e("12小时结束时间",secondFormat.format(DateUtil.getHourEnd()));
        LogUtil.e("本天开始时间",secondFormat.format(DateUtil.getDayBegin()));
        LogUtil.e("本天结束时间",secondFormat.format(DateUtil.getDayEnd()));
        LogUtil.e("本周开始时间",secondFormat.format(DateUtil.getBeginDayOfWeek()));
        LogUtil.e("本周结束时间",secondFormat.format(DateUtil.getEndDayOfWeek()));
        LogUtil.e("本月开始时间",secondFormat.format(DateUtil.getBeginDayOfMonth()));
        LogUtil.e("本月结束时间",secondFormat.format(DateUtil.getEndDayOfMonth()));
        LogUtil.e("本年开始时间",secondFormat.format(DateUtil.getBeginDayOfYear()));
        LogUtil.e("本年结束时间",secondFormat.format(DateUtil.getEndDayOfYear()));
//        List<String> timeLabel = DateUtil.getHourXLabel(1);
//        for (String s : timeLabel) {
//            LogUtil.e("111", s);
//        }
//        timeLabel = DateUtil.getHourXLabel(6);
//        for (String s : timeLabel) {
//            LogUtil.e("111", s);
//        }
//        timeLabel = DateUtil.getHourXLabel(12);
//        for (String s : timeLabel) {
//            LogUtil.e("111", s);
//        }
    }
}
