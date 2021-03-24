package com.beyond.beidou.data;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.api.Api;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.entites.GetGraphicDataResponse;
import com.beyond.beidou.entites.GetHeartChartDataResponse;
import com.beyond.beidou.util.LogUtil;
import com.beyond.beidou.util.MyPointValue;
import com.beyond.beidou.util.ScreenUtil;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

import static lecho.lib.hellocharts.gesture.ZoomType.HORIZONTAL_AND_VERTICAL;


public class ChartFragment extends BaseFragment implements View.OnClickListener {
    private Spinner spDevice, spChart, spTime;
    private LineChartView chartX, chartY, chartH;
    private List<PointValue> xValues = new ArrayList<>();   //X图表数据
    private List<PointValue> yValues = new ArrayList<>();   //Y图表数据
    private List<PointValue> hValues = new ArrayList<>();   //H图表数据
    private List<PointValue> deltaValues = new ArrayList<>(); //水平位移图表数据
    private List<PointValue> deltaHValues = new ArrayList<>(); //垂直位移图表数据
    private List<MyPointValue> preValues = new ArrayList<>();
    private TextView xChartName, yChartName, hChartName;
    private TextView xChartCoo, yChartCoo, hChartCoo;
    private TextView xChartTime, yChartTime, hChartTime;
    private TextView mTitle;
    private double convertYMax;
    private double convertYMin;
    private double convertXMin;
    private double convertXMax;
    private String responseYMin = "default";
    private String responseYMax;
    private String responseXMin = "default";
    private String responseXMax;

    private List<AxisValue> xAxisValues = new ArrayList<>();
    private List<AxisValue> yAxisValues = new ArrayList<>();

    private LinearLayout layoutChartX, layoutChartY, layoutChartH;
    private ScrollView svCharts;
    private ImageView imgBack;

    private GetHeartChartDataResponse dataResponse;

    public static ChartFragment newInstance(String projectName, ArrayList<String> stationNameList, int position) {
        ChartFragment chartFragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putString("projectName", projectName);
        args.putStringArrayList("stationNameList", stationNameList);
        args.putInt("position", position);
        chartFragment.setArguments(args);
        return chartFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        initView(view);
   /*    getChartData("E");
        getChartData("H");*/
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //请求滤波前数据
        getChartData("NE");
        //应同时判断接口的ResponseCode
        while (responseYMin.equals("default")) {
            try {
                Thread.currentThread().sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        convertHeartChartData(dataResponse.getContent(), "qqq");
        drawHeartChart("最近一小时");
    }

    public void initView(View view) {
        mTitle = view.findViewById(R.id.tv_title);
        spChart = view.findViewById(R.id.sp_chart_type);
        spDevice = view.findViewById(R.id.sp_device);
        spTime = view.findViewById(R.id.sp_chart_time);

        if (getArguments() != null) {
            mTitle.setText(getArguments().getString("projectName"));
            ArrayAdapter<String> deviceAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getArguments().getStringArrayList("stationNameList"));
            deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            spDevice.setAdapter(deviceAdapter);
            spDevice.setSelection(getArguments().getInt("position"), true);
        }

        final String[] charts = new String[]{"XYH", "位移图", "心型图"};
        String[] times = new String[]{"最近1小时", "最近6小时", "最近12小时", "本日", "本周", "本月", "本年"};
        ArrayAdapter<String> chartAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, charts);
        chartAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, times);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spTime.setAdapter(timeAdapter);
        spChart.setAdapter(chartAdapter);

        chartX = view.findViewById(R.id.chart_X);
        chartY = view.findViewById(R.id.chart_Y);
        chartH = view.findViewById(R.id.chart_H);
        xChartName = view.findViewById(R.id.tv_chartNameX);
        xChartCoo = view.findViewById(R.id.tv_coordinateSystemX);
        xChartTime = view.findViewById(R.id.tv_chartTimeX);
        yChartName = view.findViewById(R.id.tv_chartNameY);
        yChartCoo = view.findViewById(R.id.tv_coordinateSystemY);
        yChartTime = view.findViewById(R.id.tv_chartTimeY);
        hChartName = view.findViewById(R.id.tv_chartNameH);
        hChartCoo = view.findViewById(R.id.tv_coordinateSystemH);
        hChartTime = view.findViewById(R.id.tv_chartTimeH);
        layoutChartX = view.findViewById(R.id.layout_chartX);
        layoutChartY = view.findViewById(R.id.layout_chartY);
        layoutChartH = view.findViewById(R.id.layout_chartH);
        imgBack = view.findViewById(R.id.img_chart_back);
        imgBack.setOnClickListener(this);
        svCharts = view.findViewById(R.id.sv_charts);

        spChart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /*LogUtil.e("监测点当前选择为", spDevice.getSelectedItem().toString());
                LogUtil.e("图表当前选择为", spChart.getSelectedItem().toString());
                LogUtil.e("时间当前选择为", spTime.getSelected Item().toString());*/
                switchChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switchChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        chartX.setOnTouchListener(touchListener);
        chartY.setOnTouchListener(touchListener);
        chartH.setOnTouchListener(touchListener);

        //数据选择监听
        chartX.setOnValueTouchListener(new LineChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int i, int i1, PointValue pointValue) {
                Toast.makeText(getActivity(), pointValue.getX() + " , " + pointValue.getY(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onValueDeselected() {

            }
        });
    }

    /**
     * 解决图表与ScrollView滑动冲突
     * 图表可左右滑动，ScrollView可上下滑动
     */
    View.OnTouchListener touchListener = new View.OnTouchListener() {

        float yStart = 0f;
        float yEnd = 0f;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    yStart = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    yEnd = event.getY();
                    if (Math.abs(yEnd - yStart) > 200f) {
                        svCharts.requestDisallowInterceptTouchEvent(false);
                    }
                    break;
            }
            return false;
        }
    };

    public void drawHeartChart(String selectedTime) {
        String time = "2021-03-09 00:00:00";
        xChartName.setText("心型图");
        xChartTime.setText(time);
        xChartCoo.setText("WGS84坐标系|");
        layoutChartX.setVisibility(View.VISIBLE);
        layoutChartY.setVisibility(View.GONE);
        layoutChartH.setVisibility(View.GONE);
        setChart(chartX, xValues, xAxisValues, yAxisValues);
    }

    /**
     * 设置图表属性。填充数据，设置X轴，Y轴的标签。
     *
     * @param chartView   当前图表
     * @param values      图表数据
     * @param xAxisValues X轴标签（时间）
     * @param yAxisValues Y轴标签
     */
    public void setChart(LineChartView chartView, List<PointValue> values, List<AxisValue> xAxisValues, List<AxisValue> yAxisValues) {
        String selectedTime = spTime.getSelectedItem().toString();
        Line line = new Line(values).setColor(Color.parseColor("#2196F3")).setCubic(false).setPointRadius(0).setStrokeWidth(2);
        List<Line> lines = new ArrayList<>();
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);
        //设置X、Y轴
        Axis axisX = new Axis().setHasLines(true).setLineColor(Color.BLACK).setTextColor(Color.BLACK);
        axisX.setValues(xAxisValues);
        axisX.setHasTiltedLabels(true);
        axisX.setMaxLabelChars(10);
        Axis axisY = new Axis().setHasLines(true).setLineColor(Color.BLACK).setTextColor(Color.BLACK);
        axisY.setValues(yAxisValues);
        axisY.setMaxLabelChars(10);
        //绑定数据
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);
        chartView.setLineChartData(data);
        chartView.setInteractive(true);
        chartView.setMaxZoom(20f);
        //设置缩放窗口
        Viewport viewport = chartView.getMaximumViewport();
        viewport.top = viewport.top + 0.01f;
        viewport.bottom = viewport.bottom - 0.01f;
        viewport.left = viewport.left - 0.01f;
        viewport.right = viewport.right + 0.01f;
        chartView.setMaximumViewport(viewport);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_chart_back:
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
                MainActivity activity = (MainActivity) getActivity();
                activity.setChartFragment(null);
                if (fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName().equals("projectFragment")) {
                    activity.setNowFragment(activity.getProjectFragment());
                    activity.getNavigationView().setSelectedItemId(activity.getNavigationView().getMenu().getItem(0).getItemId());
                } else {
                    activity.setNowFragment(activity.getDataFragment());
                    activity.getNavigationView().setSelectedItemId(activity.getNavigationView().getMenu().getItem(1).getItemId());
                }
                break;
        }
    }

    /**
     * 清空数据信息，解决重复出现上次线条的BUG
     */
    public void cleanValues() {
        xValues.clear();
        yValues.clear();
        hValues.clear();
        deltaValues.clear();
        deltaHValues.clear();
        preValues.clear();
    }

    //仅测试用
    public void switchChart() {
        String selectedTime = spTime.getSelectedItem().toString();
        switch (spChart.getSelectedItem().toString()) {
            case "XYH":
                //
                drawHeartChart(selectedTime);
                break;
            case "位移图":
                cleanValues();
//                drawDeltaChart(selectedTime);
                break;
            case "心型图":
                cleanValues();
                drawHeartChart(selectedTime);
                break;
        }
    }

    public void getChartData(final String graphicType) {
        FormBody getChartDataBody = new FormBody.Builder()
                .add("AccessToken", ApiConfig.getAccessToken())
                .add("SessionUUID", ApiConfig.getSessionUUID())
                .add("StationUUID", "6159529a-6bc3-4c73-84d1-e59f6f60ece6")
                .add("GraphicType", graphicType)
                .add("StartTime", "2021-03-09 00:00:00")
                .add("EndTime", "2021-03-09 23:59:59")
                .build();
        Api.config(ApiConfig.GET_GRAPHIC_DATA).postRequestFormBody(getChartDataBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();
                Gson gson = new Gson();
                dataResponse = gson.fromJson(responseText, GetHeartChartDataResponse.class);
                LogUtil.e(graphicType + "获取数据个数", String.valueOf(dataResponse.getContent().size()));
                responseXMin = String.valueOf(dataResponse.getMin().get(1));
                responseXMax = String.valueOf(dataResponse.getMax().get(1));
                responseYMin = String.valueOf(dataResponse.getMin().get(2));
                responseYMax = String.valueOf(dataResponse.getMax().get(2));
                Log.e("response XY Val", "xMax: " + responseXMax + "xMin: " + responseXMin + "yMax: " + responseYMax + "yMin: " + responseYMin);
            }
        });
    }

    //对接口返回的数据进行转化
    public void convertHeartChartData(List<List<Object>> contentList, String startTime) {
        //保留两位小数
        DecimalFormat df = new DecimalFormat("#.00");
        String space = "0.01";
        BigDecimal bSpace = new BigDecimal(space);
        BigDecimal bResponseYMin = new BigDecimal(df.format(Double.parseDouble(responseYMin)));
        BigDecimal bResponseYMax = new BigDecimal(df.format(Double.parseDouble(responseYMax)));
        //转化后的Y轴最小最大值
        convertYMin = Double.parseDouble(String.valueOf(bResponseYMin));
        convertYMax = Double.parseDouble(String.valueOf(bResponseYMax));
        double tempDouble = Double.parseDouble(String.valueOf(bResponseYMin));
        String tempString = String.valueOf(convertYMin);
        BigDecimal strYMin = new BigDecimal(df.format(Double.parseDouble(responseYMin)));
        //添加Y坐标轴数据
        while (tempDouble <= convertYMax) {
            AxisValue axisValue = new AxisValue(Float.parseFloat(df.format(tempDouble - convertYMin)));
            axisValue.setLabel(tempString);
            yAxisValues.add(axisValue);
            bResponseYMin = bResponseYMin.add(bSpace);
            strYMin = strYMin.add(bSpace);
            tempString = String.valueOf(strYMin);
            tempDouble = Double.parseDouble(String.valueOf(bResponseYMin));
        }
        BigDecimal bResponseXMin = new BigDecimal(df.format(Double.parseDouble(responseXMin)));
        BigDecimal bResponseXMax = new BigDecimal(df.format(Double.parseDouble(responseXMax)));
        //转化后的X轴最小最大值
        convertXMin = Double.parseDouble(String.valueOf(bResponseXMin));
        convertXMax = Double.parseDouble(String.valueOf(bResponseXMax));
        tempDouble = Double.parseDouble(String.valueOf(bResponseXMin));
        tempString = String.valueOf(convertXMin);
        BigDecimal strXMin = new BigDecimal(df.format(Double.parseDouble(responseXMin)));
        //添加X坐标轴数据
        while (tempDouble <= convertXMax) {
            AxisValue axisValue = new AxisValue(Float.parseFloat(df.format(tempDouble - convertXMin)));
            axisValue.setLabel(tempString);
            xAxisValues.add(axisValue);
            bResponseXMin = bResponseXMin.add(bSpace);
            strXMin = strXMin.add(bSpace);
            tempString = String.valueOf(strXMin);
            tempDouble = Double.parseDouble(String.valueOf(bResponseXMin));
        }
        double tempX, tempY;
        Log.e("contentList size", contentList.size() + "");
        //添加数据到点集合中
        for (List<Object> content : contentList) {
            tempX = Double.parseDouble(df.format(Double.parseDouble(content.get(1).toString())).toString()) - Double.parseDouble(df.format(Double.parseDouble(responseXMin)));
            tempY = Double.parseDouble(df.format(Double.parseDouble(content.get(2).toString())).toString()) - Double.parseDouble(df.format(Double.parseDouble(responseYMin)));
            xValues.add(new PointValue(Float.parseFloat(String.valueOf(tempX)), Float.parseFloat(String.valueOf(tempY))));Log.e("xValue", "X = " + Float.parseFloat(String.valueOf(df.format(tempX))) + "y = " +  Float.parseFloat(String.valueOf(df.format(tempY))));
        }
        Log.e("xValues size", xValues.size() + "");
    }
}