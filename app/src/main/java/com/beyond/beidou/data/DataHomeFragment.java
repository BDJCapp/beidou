package com.beyond.beidou.data;

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
import com.beyond.beidou.util.LogUtil;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * @author: 李垚
 */
public class DataHomeFragment extends BaseFragment {

    private Spinner spProjectName;
    private RecyclerView deviceList;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data, container, false);
        initView(view);
        test();
        return view;
    }

    public void initView(View view) {
        spProjectName = view.findViewById(R.id.spinner_projectName);
        deviceList = view.findViewById(R.id.rv_device);
        setViews();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        LogUtil.e("hiden change","sdasdasd");
        setViews();
    }

    public void setViews()
    {
        //设置Spinner
        final HashMap<String,Object> requestParams = new HashMap<>();
        requestParams.put("AccessToken", ApiConfig.getAccessToken());
        requestParams.put("SessionUUID",ApiConfig.getSessionUUID());
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
                                SimpleDateFormat sdfTwo =new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒E", Locale.getDefault());
                                LogUtil.e("Spinner切换工程的时间",sdfTwo.format(System.currentTimeMillis()));
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
                        List<ProjectResponse.ProjectListBean.StationListBean> stationList = projectResponse.getProjectList().get(0).getStationList();
                        for (int i = 0; i < stationList.size(); i++) {
                            deviceNames.add(stationList.get(i).getStationName());
                            deviceTypes.add(getStationType(stationList.get(i).getStationType()));
                            lastTimes.add(stationList.get(i).getStationLastTime());
                            deviceStatus.add(stationList.get(i).getStationStatus());
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DeviceListAdapter adapter = new DeviceListAdapter(deviceNames, deviceTypes, lastTimes, deviceStatus);
                                adapter.setLookDataListener(new DeviceListAdapter.onItemLookdataClockListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
                                        // LogUtil.e("查看的监测点", deviceNames.get(position));
                                        switchFragment(spProjectName.getSelectedItem().toString(), deviceNames,position);
                                    }
                                });
                                deviceList.setAdapter(adapter);
                                LinearLayoutManager manager = new LinearLayoutManager(getActivity());
                                manager.setOrientation(RecyclerView.VERTICAL);
                                deviceList.setLayoutManager(manager);
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

    public void switchFragment(String projectName,ArrayList<String> stationNameList,int position) {
        //Fragment chartFragment = new ChartFragment();
        ChartFragment chartFragment = ChartFragment.newInstance(projectName,stationNameList,position);
        MainActivity activity = (MainActivity) getActivity();
        activity.setChartFragment(chartFragment);
        activity.setNowFragment(chartFragment);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(R.id.layout_home, chartFragment).hide(this);
        ft.addToBackStack("DataHomeFragment");   //加入到返回栈中
        ft.commit();
    }

    public void test()
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String startTime = simpleDateFormat.format(date) + " 00:00:00";
        String endTime = simpleDateFormat.format(date) + " 23:59:59";
        LogUtil.e("startTime",startTime);
        LogUtil.e("endTime",endTime);
        String startTimeStamp = null;
        String endTimeStamp = null;
        String timeStampGap = null;
        String testTime = null;
        try {
             //转换为秒级时间戳
             testTime = String.valueOf(dateFormat.parse("2021-03-09 00:03:55").getTime()/1000);
             startTimeStamp = String.valueOf(dateFormat.parse(startTime).getTime()/1000);
             endTimeStamp = String.valueOf(dateFormat.parse(endTime).getTime()/1000);
             //timeStampGap = String.valueOf(dateFormat.parse("2021-03-09 00:03:55").getTime()/1000 - dateFormat.parse("2021-03-09 00:00:00").getTime()/1000);
             timeStampGap = String.valueOf(dateFormat.parse(endTime).getTime()/1000 - dateFormat.parse(startTime).getTime()/1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        LogUtil.e("测试时间戳", testTime);
        LogUtil.e("一天开始时间+时间戳",simpleDateFormat.format(date) + " 00:00:00" + "  " + startTimeStamp);
        LogUtil.e("一天结束时间+时间戳",simpleDateFormat.format(date) + " 23:59:59" + "  " + endTimeStamp);
        LogUtil.e("时间差",String.valueOf(Float.parseFloat(timeStampGap)));
    }
}
