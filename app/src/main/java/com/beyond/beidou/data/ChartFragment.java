package com.beyond.beidou.data;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;

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
import com.beyond.beidou.util.LogUtil;
import com.beyond.beidou.util.LoginUtil;
import com.beyond.beidou.util.MyPointValue;
import com.beyond.beidou.util.ScreenUtil;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private float valueYMax, valueYMin, yMax, yMin;
    private String cutNum;
    private LinearLayout layoutChartX, layoutChartY, layoutChartH;
    private ScrollView svCharts;
    private ImageView imgBack;
    private String aa;

    public static ChartFragment newInstance(String projectName,ArrayList<String> stationNameList,int position)
    {
        ChartFragment chartFragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putString("projectName",projectName);
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
        drawXYHChart("最近1小时");  //默认展示XYH一小时的图表
/*      getChartData("N");
        getChartData("E");
        getChartData("H");*/
        return view;
    }

    public void initView(View view) {
        mTitle = view.findViewById(R.id.tv_title);
        spChart = view.findViewById(R.id.sp_chart_type);
        spDevice = view.findViewById(R.id.sp_device);
        spTime = view.findViewById(R.id.sp_chart_time);

        if (getArguments() != null)
        {
            mTitle.setText(getArguments().getString("projectName"));
            ArrayAdapter<String> deviceAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getArguments().getStringArrayList("stationNameList"));
            deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            spDevice.setAdapter(deviceAdapter);
            spDevice.setSelection(getArguments().getInt("position"),true);
        }

        /*String[] devices = new String[]{"监测点1", "监测点2", "监测点3"};
        ArrayAdapter<String> deviceAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, devices);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spDevice.setAdapter(deviceAdapter);
        */
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
                Toast.makeText(getActivity(),pointValue.getX() + " , " + pointValue.getY(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onValueDeselected() {

            }
        });

    }

    /**
     * 解决图表于ScrollView滑动冲突
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

    /**
     * 绘制XYH图表
     */
    public void drawXYHChart(String selectedTime) {
        String time = null;     //模拟选择时间
        switch (selectedTime) {
            case "最近1小时":
                time = "2021-02-02 10:00:00~2021-02-02 11:00:00";
                break;
            case "最近6小时":
                time = "2021-02-02 10:00:00~2021-02-02 16:00:00";
                break;
            case "最近12小时":
                time = "2021-02-02 10:00:00~2021-02-02 22:00:00";
                break;
            case "本日":
                time = "2021-02-02 00:00:00~2021-02-02 24:00:00";
                break;
            case "本周":
                time = "2021-02-01 00:00:00~2021-02-07 24:00:00";
                break;
            case "本月":
                time = "2021-02-01 00:00:00~2021-02-08 24:00:00";
                break;
            case "本年":
                time = "2020-01-01 00:00:00~2020-12-31 24:00:00";
                break;
        }
        initXYHChartValue();
        xChartName.setText("X");
        xChartTime.setText(time);
        xChartCoo.setText("WGS84坐标系|");

        yChartName.setText("Y");
        yChartTime.setText(time);
        yChartCoo.setText("WGS84坐标系|");

        hChartName.setText("H");
        hChartTime.setText(time);
        hChartCoo.setText("WGS84坐标系|");

        layoutChartX.setVisibility(View.VISIBLE);
        layoutChartY.setVisibility(View.VISIBLE);
        layoutChartH.setVisibility(View.VISIBLE);

        List<AxisValue> xAxisValues = setXAxisValues(selectedTime, xValues);
        List<AxisValue> yAxisValues = setAxisYLabel(cutNum, xValues);
        setChart(chartX, xValues, xAxisValues, yAxisValues);
        setChart(chartY, yValues, xAxisValues, yAxisValues);
        setChart(chartH, hValues, xAxisValues, yAxisValues);
    }


    /**
     * 绘制位移图表
     */
    public void drawDeltaChart(String selectedTime) {
        String time = null;     //模拟时间，时间区间可由后端传
        switch (selectedTime) {
            case "最近1小时":
                time = "2021-02-02 10:00:00~2021-02-02 11:00:00";
                break;
            case "最近6小时":
                time = "2021-02-02 10:00:00~2021-02-02 16:00:00";
                break;
            case "最近12小时":
                time = "2021-02-02 10:00:00~2021-02-02 22:00:00";
                break;
            case "本日":
                time = "2021-02-02 00:00:00~2021-02-02 24:00:00";
                break;
            case "本周":
                time = "2021-02-01 00:00:00~2021-02-07 24:00:00";
                break;
            case "本月":
                time = "2021-02-01 00:00:00~2021-02-28 24:00:00";
                break;
            case "本年":
                time = "2020-01-01 00:00:00~2020-12-31 24:00:00";
                break;
        }
        initDeltaChartValue();
        xChartName.setText("水平位移图");
        xChartTime.setText(time);
        xChartCoo.setText("WGS84坐标系|");

        yChartName.setText("垂直位移图");
        yChartTime.setText(time);
        yChartCoo.setText("WGS84坐标系|");

        layoutChartX.setVisibility(View.VISIBLE);
        layoutChartY.setVisibility(View.VISIBLE);
        layoutChartH.setVisibility(View.GONE);

        List<AxisValue> xAxisValues = setXAxisValues(selectedTime, deltaValues);
        List<AxisValue> yAxisValues = setAxisYLabel("",deltaValues);
        setChart(chartX, deltaValues, xAxisValues, yAxisValues);
        setChart(chartY, deltaValues, xAxisValues, yAxisValues);
    }

    public void drawHeartChart() {
        layoutChartX.setVisibility(View.GONE);
        layoutChartY.setVisibility(View.GONE);
        layoutChartH.setVisibility(View.GONE);
    }

    /**
     * 模拟XYH图表的数据并进行处理，数据的获取解析以及裁剪
     */
    public void initXYHChartValue() {
        /*接口完成后，在此获取返回的XYH数据*/
        if ("最近1小时".equals(spTime.getSelectedItem().toString())) {
            preValues.add(new MyPointValue(1, 3550716.442));
            preValues.add(new MyPointValue(2, 3550716.439));
            preValues.add(new MyPointValue(3, 3550716.440));
            preValues.add(new MyPointValue(4, 3550716.443));
            preValues.add(new MyPointValue(5, 3550716.442));
            preValues.add(new MyPointValue(6, 3550716.442));
            preValues.add(new MyPointValue(7, 3550716.442));
            preValues.add(new MyPointValue(8, 3550716.443));
            preValues.add(new MyPointValue(9, 3550716.443));
            preValues.add(new MyPointValue(10, 3550716.443));
            preValues.add(new MyPointValue(11, 3550716.444));
            preValues.add(new MyPointValue(12, 3550716.444));
            preValues.add(new MyPointValue(13, 3550716.444));
        } else if ("本周".equals(spTime.getSelectedItem().toString())) {
            preValues.add(new MyPointValue(1, 3550716.442));
            preValues.add(new MyPointValue(2, 3550716.439));
            preValues.add(new MyPointValue(3, 3550716.444));
            preValues.add(new MyPointValue(4, 3550716.440));
            preValues.add(new MyPointValue(5, 3550716.441));
            preValues.add(new MyPointValue(6, 3550716.443));
            preValues.add(new MyPointValue(7, 3550716.442));
            preValues.add(new MyPointValue(8, 3550716.443));
            preValues.add(new MyPointValue(9, 3550716.441));
            preValues.add(new MyPointValue(10, 3550716.443));
            preValues.add(new MyPointValue(11, 3550716.444));
            preValues.add(new MyPointValue(12, 3550716.444));
            preValues.add(new MyPointValue(13, 3550716.440));
            preValues.add(new MyPointValue(14, 3550716.442));
            preValues.add(new MyPointValue(15, 3550716.443));
        } else if ("本月".equals(spTime.getSelectedItem().toString())) {
            preValues.add(new MyPointValue(1, 3550716.442));
            preValues.add(new MyPointValue(2, 3550716.439));
            preValues.add(new MyPointValue(3, 3550716.442));
            preValues.add(new MyPointValue(4, 3550716.440));
            preValues.add(new MyPointValue(5, 3550716.441));
            preValues.add(new MyPointValue(6, 3550716.440));
            preValues.add(new MyPointValue(7, 3550716.442));
            preValues.add(new MyPointValue(8, 3550716.443));
            preValues.add(new MyPointValue(9, 3550716.445));
            preValues.add(new MyPointValue(10, 3550716.443));
            preValues.add(new MyPointValue(11, 3550716.441));
            preValues.add(new MyPointValue(12, 3550716.444));
            preValues.add(new MyPointValue(13, 3550716.440));
            preValues.add(new MyPointValue(14, 3550716.442));
            preValues.add(new MyPointValue(15, 3550716.443));
            //preValues.add(new MyPointValue(16, 3550716.445));
        } else if ("本年".equals(spTime.getSelectedItem().toString())) {
            preValues.add(new MyPointValue(1, 3550716.442));
            preValues.add(new MyPointValue(2, 3550716.439));
            preValues.add(new MyPointValue(3, 3550716.444));
            preValues.add(new MyPointValue(4, 3550716.440));
            preValues.add(new MyPointValue(5, 3550716.441));
            preValues.add(new MyPointValue(6, 3550716.443));
            preValues.add(new MyPointValue(7, 3550716.442));
            preValues.add(new MyPointValue(8, 3550716.443));
            preValues.add(new MyPointValue(9, 3550716.441));
            preValues.add(new MyPointValue(10, 3550716.443));
            preValues.add(new MyPointValue(11, 3550716.444));
            preValues.add(new MyPointValue(12, 3550716.444));
            preValues.add(new MyPointValue(13, 3550716.440));
        } else {   //数据变化
            preValues.add(new MyPointValue(1, 3550716.444));
            preValues.add(new MyPointValue(2, 3550716.443));
            preValues.add(new MyPointValue(3, 3550716.443));
            preValues.add(new MyPointValue(4, 3550716.443));
            preValues.add(new MyPointValue(5, 3550716.444));
            preValues.add(new MyPointValue(6, 3550716.440));
            preValues.add(new MyPointValue(7, 3550716.444));
            preValues.add(new MyPointValue(8, 3550716.442));
            preValues.add(new MyPointValue(9, 3550716.439));
            preValues.add(new MyPointValue(10, 3550716.440));
            preValues.add(new MyPointValue(11, 3550716.443));
            preValues.add(new MyPointValue(12, 3550716.442));
            preValues.add(new MyPointValue(13, 3550716.442));
        }

        //裁剪
        String tempstring;
        float tempfloat;
        cutNum = String.valueOf(preValues.get(0).getY()).substring(0, 3);
        for (MyPointValue point : preValues) {
            //数据预处理，将去掉公共前两位
            tempstring = String.valueOf(point.getY()).substring(3);
            tempfloat = Float.valueOf(tempstring);
            xValues.add(new PointValue((float) point.getX(), tempfloat));
            yValues.add(new PointValue((float) point.getX(), tempfloat));
            hValues.add(new PointValue((float) point.getX(), tempfloat));
        }

    }

    /**
     * 模拟位移图的数据
     */
    public void initDeltaChartValue() {
        List<MyPointValue> returnValues = new ArrayList<>();  //模拟返回数据
        if ("本周".equals(spTime.getSelectedItem().toString())) {
            returnValues.add(new MyPointValue(3550716.4521, 404979.4331));
            returnValues.add(new MyPointValue(3550716.446, 404979.4331));
            returnValues.add(new MyPointValue(3550716.4455, 404979.4332));
            returnValues.add(new MyPointValue(3550716.4446, 404979.4335));
            returnValues.add(new MyPointValue(3550716.4441, 404979.4342));
            returnValues.add(new MyPointValue(3550716.444, 404979.4346));
            returnValues.add(new MyPointValue(3550716.4443, 404979.4348));
            returnValues.add(new MyPointValue(3550716.4457, 404979.4338));
            returnValues.add(new MyPointValue(3550716.446, 404979.434));
            returnValues.add(new MyPointValue(3550716.4471, 404979.434));
            returnValues.add(new MyPointValue(3550716.4485, 404979.4339));
            returnValues.add(new MyPointValue(3550716.4481, 404979.4334));
            returnValues.add(new MyPointValue(3550716.4481, 404979.4343));
            returnValues.add(new MyPointValue(3550716.4487, 404979.4331));
            returnValues.add(new MyPointValue(3550716.4485, 404979.4335));
        } else if ("本月".equals(spTime.getSelectedItem().toString())) {
            returnValues.add(new MyPointValue(3550716.446, 404979.434));
            returnValues.add(new MyPointValue(3550716.4471, 404979.434));
            returnValues.add(new MyPointValue(3550716.4485, 404979.4339));
            returnValues.add(new MyPointValue(3550716.4481, 404979.4334));
            returnValues.add(new MyPointValue(3550716.4481, 404979.4343));
            returnValues.add(new MyPointValue(3550716.4487, 404979.4331));
            returnValues.add(new MyPointValue(3550716.4485, 404979.4335));
            returnValues.add(new MyPointValue(3550716.4485, 404979.4335));
            returnValues.add(new MyPointValue(3550716.4521, 404979.4331));
            returnValues.add(new MyPointValue(3550716.446, 404979.4331));
            returnValues.add(new MyPointValue(3550716.4455, 404979.4332));
            returnValues.add(new MyPointValue(3550716.4446, 404979.4335));
            returnValues.add(new MyPointValue(3550716.4441, 404979.4342));
            returnValues.add(new MyPointValue(3550716.444, 404979.4346));
            returnValues.add(new MyPointValue(3550716.4443, 404979.4348));
            returnValues.add(new MyPointValue(3550716.4457, 404979.4338));
        } else if ("本年".equals(spTime.getSelectedItem().toString())) {
            returnValues.add(new MyPointValue(3550716.4521, 404979.4331));
            returnValues.add(new MyPointValue(3550716.446, 404979.4331));
            returnValues.add(new MyPointValue(3550716.4455, 404979.4332));
            returnValues.add(new MyPointValue(3550716.4446, 404979.4335));
            returnValues.add(new MyPointValue(3550716.4441, 404979.4342));
            returnValues.add(new MyPointValue(3550716.444, 404979.4346));
            returnValues.add(new MyPointValue(3550716.4443, 404979.4348));
            returnValues.add(new MyPointValue(3550716.4457, 404979.4338));
            returnValues.add(new MyPointValue(3550716.446, 404979.434));
            returnValues.add(new MyPointValue(3550716.4471, 404979.434));
            returnValues.add(new MyPointValue(3550716.4485, 404979.4339));
            returnValues.add(new MyPointValue(3550716.4481, 404979.4334));
            returnValues.add(new MyPointValue(3550716.4481, 404979.4333));
        } else {
            returnValues.add(new MyPointValue(3550716.4521, 404979.4331));
            returnValues.add(new MyPointValue(3550716.446, 404979.4331));
            returnValues.add(new MyPointValue(3550716.4455, 404979.4332));
            returnValues.add(new MyPointValue(3550716.4446, 404979.4335));
            returnValues.add(new MyPointValue(3550716.4441, 404979.4342));
            returnValues.add(new MyPointValue(3550716.444, 404979.4346));
            returnValues.add(new MyPointValue(3550716.4443, 404979.4348));
            returnValues.add(new MyPointValue(3550716.4457, 404979.4338));
            returnValues.add(new MyPointValue(3550716.446, 404979.434));
            returnValues.add(new MyPointValue(3550716.4471, 404979.434));
            returnValues.add(new MyPointValue(3550716.4485, 404979.4339));
            returnValues.add(new MyPointValue(3550716.4481, 404979.4334));
            returnValues.add(new MyPointValue(3550716.4481, 404979.4333));
        }
        /*数据处理，如果接口直接返回处理过的数据最好*/
        double x0, y0, result;
        float i = 0;
        x0 = returnValues.get(0).getX();   //模拟基准点
        y0 = returnValues.get(0).getY();
        for (MyPointValue point : returnValues) {
            i++;
            result = Math.sqrt(Math.pow(point.getX() - x0, 2) + Math.pow(point.getY() - y0, 2));
            deltaValues.add(new lecho.lib.hellocharts.model.PointValue(i, (float) result));
        }
    }

    /**
     * 设置图表的Y轴的标签
     * @param cutNum 裁剪的整数
     * @param values 图表数据
     * @return Y轴标签列表
     */
    public List<AxisValue> setAxisYLabel(String cutNum, List<PointValue> values) {
        String tempstring;
        float tempfloat;
        List<AxisValue> axisValues = new ArrayList<>();
        valueYMax = values.get(0).getY();
        valueYMin = values.get(0).getY();
        for (lecho.lib.hellocharts.model.PointValue pointValue : values) {
            //确定最大最小值
            if (pointValue.getY() >= valueYMax) {
                valueYMax = pointValue.getY();
            }
            if (pointValue.getY() <= valueYMin) {
                valueYMin = pointValue.getY();
            }
        }

        String space = "0.01";     //每格大小为0.01m
        yMax = valueYMax + 0.1f;
        yMin = valueYMin - 0.1f;

        DecimalFormat df = new DecimalFormat("#.00");//只保留小数点后两位，厘米级精度

        BigDecimal b_ymin = new BigDecimal(df.format(yMin));//解决浮点型数据加减运算精度问题
        BigDecimal b_space = new BigDecimal(space);
        tempfloat = yMin;

        while (tempfloat <= yMax) {
            b_ymin = b_ymin.add(b_space);
            tempstring = cutNum + String.valueOf(b_ymin);

            tempfloat = Float.parseFloat(String.valueOf(b_ymin));
            AxisValue axisValue = new AxisValue(tempfloat);
            axisValue.setLabel(tempstring);
            axisValues.add(axisValue);
        }
        return axisValues;
    }

    public List<AxisValue> set1HourAxisXLabel(List<PointValue> values) {
        /*模拟时间数据*/
        String[] labels = new String[]{"10:00", "10:05", "10:10", "10:15", "10:20", "10:25", "10:30", "10:35", "10:40", "10:45", "10:50", "10:55", "11:00"};
        List<AxisValue> axisValues = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            AxisValue axisValue = new AxisValue(values.get(i).getX());
            axisValue.setLabel(labels[i]);
            axisValues.add(axisValue);
        }
        return axisValues;
    }

    public List<AxisValue> set6HourAxisXLabel(List<PointValue> values) {
        /*模拟时间标签数据*/
        String[] labels = new String[]{"10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00"};
        List<AxisValue> axisValues = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            AxisValue axisValue = new AxisValue(values.get(i).getX());
            axisValue.setLabel(labels[i]);
            axisValues.add(axisValue);
        }
        return axisValues;
    }

    public List<AxisValue> set12HourAxisXLabel(List<PointValue> values) {
        /*模拟时间标签数据*/
        String[] labels = new String[]{"10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00"};
        List<AxisValue> axisValues = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            AxisValue axisValue = new AxisValue(values.get(i).getX());
            axisValue.setLabel(labels[i]);
            axisValues.add(axisValue);
        }
        return axisValues;
    }

    public List<AxisValue> setDayHourAxisXLabel(List<PointValue> values) {
        /*模拟时间标签1数据*/
        String[] labels = new String[]{"00:00", "02:00", "04:00", "06:00", "08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00", "22:00", "24:00"};
        List<AxisValue> axisValues = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            AxisValue axisValue = new AxisValue(values.get(i).getX());
            axisValue.setLabel(labels[i]);
            axisValues.add(axisValue);
        }
        return axisValues;
    }

    public List<AxisValue> setWeekHourAxisXLabel(List<PointValue> values) {
        /*模拟时间标签1数据*/
        String[] labels = new String[]{"周一 00:00", "周一 12:00", "周二 00:00", "周二 12:00", "周三 00:00", "周三 12:00", "周四 00:00", "周四 12:00", "周五 00:00", "周五 12:00", "周六 00:00", "周六 12:00", "周日 00:00", "周日 12:00", "周日 24:00"};
        List<AxisValue> axisValues = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            AxisValue axisValue = new AxisValue(values.get(i).getX());
            axisValue.setLabel(labels[i]);
            axisValues.add(axisValue);
        }
        return axisValues;
    }

    public List<AxisValue> setMonthHourAxisXLabel(List<PointValue> values) {
        /*模拟时间标签1数据*/
        String[] labels = new String[]{"2号", "4号", "6号", "8号", "10号", "12号", "14号", "16号", "18号", "20号", "22号", "24号", "26号", "28号", "30号"};
        List<AxisValue> axisValues = new ArrayList<>();
        for (int i = 2; i <= 16; i++) {
            //标签不直接与数据绑定，否则当数据不完整时，标签也不完整
            AxisValue axisValue = new AxisValue(i);
            axisValue.setLabel(labels[i - 2]);
            axisValues.add(axisValue);
        }
        return axisValues;
    }

    public List<AxisValue> setYearHourAxisXLabel(List<PointValue> values) {
        /*模拟时间标签1数据*/
        String[] labels = new String[]{"1.31", "2.28", "3.31", "4.30", "5.31", "6.30", "7.31", "8.31", "9.30", "10.31", "11.30", "12.31"};
        List<AxisValue> axisValues = new ArrayList<>();
        for (int i = 1; i <= labels.length; i++) {
            AxisValue axisValue = new AxisValue(values.get(i).getX());
            axisValue.setLabel(labels[i - 1]);
            axisValues.add(axisValue);
        }
        return axisValues;
    };

    /**
     * 设置X轴标签
     *
     * @param selectedTime 当前所选时间
     * @param values       图表数据
     * @return 标签列表
     */
    public List<AxisValue> setXAxisValues(String selectedTime, List<PointValue> values) {
        List<AxisValue> xAxisValues = new ArrayList<>();
        switch (selectedTime) {
            case "最近1小时":
                xAxisValues = set1HourAxisXLabel(values);
                break;
            case "最近6小时":
                xAxisValues = set6HourAxisXLabel(values);
                break;
            case "最近12小时":
                xAxisValues = set12HourAxisXLabel(values);
                break;
            case "本日":
                xAxisValues = setDayHourAxisXLabel(values);
                break;
            case "本周":
                xAxisValues = setWeekHourAxisXLabel(values);
                break;
            case "本月":
                xAxisValues = setMonthHourAxisXLabel(values);
                break;
            case "本年":
                xAxisValues = setYearHourAxisXLabel(values);
                break;
        }
        return xAxisValues;
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

        Axis axisX = new Axis().setHasLines(true).setLineColor(Color.BLACK).setTextColor(Color.BLACK);
        axisX.setValues(xAxisValues);
        axisX.setHasTiltedLabels(true);      //设置旋转45°
        //setHasLines(true),设定是否有网格线
        Axis axisY = new Axis().setHasLines(true).setLineColor(Color.BLACK).setTextColor(Color.BLACK);
        //axisY.setInside(true);
        axisY.setValues(yAxisValues);

        //设置坐标轴标签宽度
        if ("位移图".equals(spChart.getSelectedItem().toString()))
            axisY.setMaxLabelChars(5);
        else
            axisY.setMaxLabelChars(10);

        if ("本周".equals(selectedTime))
            axisX.setMaxLabelChars(8);

        //为两个坐标系设定名称
        axisY.setName("单位(米)");
        //设置图标所在位置
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);
        //将数据添加到View中
        chartView.setLineChartData(data);
        chartView.setInteractive(true);
        chartView.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        chartView.setMaxZoom(4f);
        chartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        chartView.setVisibility(View.VISIBLE);


        Viewport maxWindow = new Viewport(chartView.getMaximumViewport());
        maxWindow.bottom = yMin;
        maxWindow.top = yMax;

        //设置当前窗口，将每格长度大约设置成物理上的1cm
        Viewport currentWindow = new Viewport(chartView.getMaximumViewport());
        currentWindow.bottom = yMin + 0.0635f;
        currentWindow.top = yMax - 0.0635f;
        switch (selectedTime) {
            case "本周":
                maxWindow.right = 15.2f;
                currentWindow.right = 15.2f;
                break;
            case "本月":
                maxWindow.right = 16.2f;
                currentWindow.right = 16.2f;
                break;
            default:
                maxWindow.right = 13.2f;
                currentWindow.right = 13.2f;
                break;
        }
        LogUtil.e("1cm转换成px",String.valueOf(ScreenUtil.cm2px(getActivity(),1)));
        chartView.setMaximumViewport(maxWindow);
        chartView.setCurrentViewport(currentWindow);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_chart_back:
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
                MainActivity activity = (MainActivity) getActivity();
                activity.setChartFragment(null);
                activity.setNowFragment(activity.getDataFragment());
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

    public void switchChart() {
        String selectedTime = spTime.getSelectedItem().toString();
        switch (spChart.getSelectedItem().toString()) {
            case "XYH":
                cleanValues();
                drawXYHChart(selectedTime);
                break;
            case "位移图":
                cleanValues();
                drawDeltaChart(selectedTime);
                break;
            case "心型图":
                cleanValues();
                drawHeartChart();
                break;
        }
    }

    public void getChartData(final String graphicType)
    {
        FormBody getChartDataBody = new FormBody.Builder()
                .add("AccessToken", ApiConfig.getAccessToken())
                .add("SessionUUID",ApiConfig.getSessionUUID())
                .add("StationUUID","6159529a-6bc3-4c73-84d1-e59f6f60ece6")
                .add("GraphicType",graphicType)
                .add("StartTime","2021-03-09 00:00:00")
                .add("EndTime","2021-03-09 23:59:59")
                .build();

        Api.config(ApiConfig.GET_GRAPHIC_DATA).postRequestFormBody(getChartDataBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();
                Gson gson = new Gson();
                GetGraphicDataResponse dataResponse = gson.fromJson(responseText, GetGraphicDataResponse.class);
                LogUtil.e(graphicType + "获取数据个数", String.valueOf(dataResponse.getContent().size()));
                for (int i = 0; i < 10; i++) {
                    String time = dataResponse.getContent().get(i).get(0);
                    LogUtil.e("时间",time);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = null;
                    try {
                        date = simpleDateFormat.parse(time);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long ts = date.getTime();
                    String res = String.valueOf(ts);
                    res = "31535999";
                    LogUtil.e("时间戳",res);
                    float convertTime = Float.parseFloat(res);
                    LogUtil.e("float时间戳", String.valueOf(convertTime));
                    //时间戳太大解决思路，结束时间的时间戳减去开始时间的时间戳。但是一年的时间戳还是大，精度会丢一位
                }

            }
        });

    }
}