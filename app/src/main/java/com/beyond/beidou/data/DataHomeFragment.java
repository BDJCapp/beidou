package com.beyond.beidou.data;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.adapter.DeviceListAdapter;
import com.beyond.beidou.util.LogUtil;

import java.util.ArrayList;
import java.util.List;


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
/*        new Thread(new Runnable() {
            @Override
            public void run() {
                LoginUtil.getToken();
            }
        }).start();*/
        return view;
    }

    public void initView(View view) {
        spProjectName = view.findViewById(R.id.spinner_projectName);
        deviceList = view.findViewById(R.id.rv_device);

        /*设置下拉框*/
        final String[] arrayStrings = new String[]{"实验室测试工程", "默认工程", "南京牛首山工程", "北京和平里工程"};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, arrayStrings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spProjectName.setAdapter(adapter);
        spProjectName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /*设置列表*/
        DeviceListAdapter rvAdapter = getRvAdapter();
        deviceList.setAdapter(rvAdapter);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(RecyclerView.VERTICAL);
        deviceList.setLayoutManager(manager);
    }

    /**
     * 模拟列表数据并设置adapter
     * @return 设备列表适配器
     */
    public DeviceListAdapter getRvAdapter() {
        final List<String> deviceNames = new ArrayList<>();
        List<String> deviceTypes = new ArrayList<>();
        List<String> lastTimes = new ArrayList<>();
        List<String> deviceStatus = new ArrayList<>();
        //模拟数据
        for (int i = 1; i < 5; i++) {
            deviceNames.add("监测点" + i);
            lastTimes.add("2020-02-0" + i + "16:20:00");
        }
        deviceStatus.add("在线");
        deviceStatus.add("警告");
        deviceStatus.add("故障");
        deviceStatus.add("离线");

        deviceTypes.add("移动站");
        deviceTypes.add("基准站");
        deviceTypes.add("移动站");
        deviceTypes.add("基准站");

        DeviceListAdapter adapter = new DeviceListAdapter(deviceNames, deviceTypes, lastTimes, deviceStatus);
        adapter.setLookDataListener(new DeviceListAdapter.onItemLookdataClockListener() {
            @Override
            public void onItemClick(View view, int position) {
                LogUtil.e("查看的监测点", deviceNames.get(position));
                switchFragment();
            }
        });
        return adapter;
    }

    public void switchFragment() {
        Fragment chartFragment = new ChartFragment();
        MainActivity activity = (MainActivity) getActivity();
        activity.setChartFragment(chartFragment);
        activity.setNowFragment(chartFragment);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(R.id.layout_home, chartFragment).hide(this);
        ft.addToBackStack(null);   //加入到返回栈中
        ft.commit();
    }

    /*public void getProjects()
    {
        FormBody getProjectsBody = new FormBody.Builder()
                .add("AccessToken", "64eb860b-2af4-451b-07a4-1976cc98f555")
                .add("SessionUUID","b8ee3e95-658f-cae3-4792-f1c251446229")
                .build();
        HttpUtil.postRequestFormBody(APIUtil.getGetProjectsUrl(), getProjectsBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                LogUtil.e("获取工程列表错误",e.getMessage());
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();
                Gson gson = new Gson();
                ProjectResponse returnProjects = gson.fromJson(responseText, ProjectResponse.class);
                LogUtil.e("获取数据成功，返回信息为", returnProjects.getResponseMsg());
                LogUtil.e("获取的工程名称",returnProjects.getProjectList().get(0).getProjectName());
            }
        });

    }*/
}
