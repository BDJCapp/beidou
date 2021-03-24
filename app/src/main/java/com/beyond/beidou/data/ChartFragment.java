package com.beyond.beidou.data;

import android.graphics.Color;
import android.os.Bundle;

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

import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.api.Api;
import com.beyond.beidou.api.ApiCallback;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.entites.GNSSFilterInfoResponse;
import com.beyond.beidou.util.LogUtil;
import com.beyond.beidou.util.MyPointValue;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import okhttp3.FormBody;


public class ChartFragment extends BaseFragment implements View.OnClickListener {
    private Spinner spDevice, spChart, spTime;
    private LineChartView chartX, chartY, chartH;
    //    private List<PointValue> xValues = new ArrayList<>();   //X图表数据
//    private List<PointValue> yValues = new ArrayList<>();   //Y图表数据
//    private List<PointValue> hValues = new ArrayList<>();   //H图表数据
//    private List<PointValue> deltaValues = new ArrayList<>(); //水平位移图表数据
//    private List<PointValue> deltaHValues = new ArrayList<>(); //垂直位移图表数据
//    private List<MyPointValue> preValues = new ArrayList<>();
    private TextView xChartName, yChartName, hChartName;
    private TextView xChartCoo, yChartCoo, hChartCoo;
    private TextView xChartTime, yChartTime, hChartTime;
    private TextView mTitle;
    private float convertYMax;
    private float convertYMin;
    private float convertAverage;
    private List<Object> maxResponse = new ArrayList<>();
    private List<Object> minResponse = new ArrayList<>();
    private List<Object> averageResponse = new ArrayList<>();
    private LinearLayout layoutChartX, layoutChartY, layoutChartH;
    private ScrollView svCharts;
    private ImageView imgBack;
    private List<PointValue> xConvertData = new ArrayList<>();
    private List<PointValue> yConvertData = new ArrayList<>();
    private List<PointValue> hConvertData = new ArrayList<>();
    private List<PointValue> deltaDConvertData = new ArrayList<>();
    private List<PointValue> deltaHConvertData = new ArrayList<>();
    private List<List<Object>> contentResponse = new ArrayList<>();
    private List<List<PointValue>> chartLines = new ArrayList<>();
    private int interval,maxInterval;
    private boolean isFirstTimeSelectTime = true;
    private boolean isFirstTimeSelectChart = true;

    public static ChartFragment newInstance(String projectName, ArrayList<String> stationNameList, int position)
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
        getData("GNSSFilterInfo","6159529a-6bc3-4c73-84d1-e59f6f60ece6","2021-03-09 00:00:00","2021-03-09 23:59:59");
        drawXYHChart("本日");  //默认展示XYH一小时的图表
        return view;
    }

    public void initView(View view) {
        LogUtil.e("initView开始执行","////////");
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

        final String[] charts = new String[]{"XYH", "位移图", "心型图"};
        String[] times = new String[]{"最近1小时", "最近6小时", "最近12小时", "本日", "本周", "本月", "本年"};
        ArrayAdapter<String> chartAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, charts);
        chartAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, times);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spTime.setAdapter(timeAdapter);
        spTime.setSelection(3, true);
        spChart.setAdapter(chartAdapter);
        spChart.setSelection(0, true);

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
//                if(isFirstTimeSelectChart){
//                    isFirstTimeSelectChart = false;
//                    return;
//                }
                Log.e("spChart", "CNM" );
                switchChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if(isFirstTimeSelectTime){
//                    isFirstTimeSelectTime = false;
//                    return;
//                }
                Log.e("spTime", "CNM");
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
        /*chartX.setOnValueTouchListener(new LineChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int i, int i1, PointValue pointValue) {
                Toast.makeText(getActivity(),pointValue.getX() + " , " + pointValue.getY(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onValueDeselected() {

            }
        });

        chartY.setOnValueTouchListener(new LineChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int i, int i1, PointValue pointValue) {
                Toast.makeText(getActivity(),pointValue.getX() + " , " + pointValue.getY(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onValueDeselected() {

            }
        });

        chartH.setOnValueTouchListener(new LineChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int i, int i1, PointValue pointValue) {
                Toast.makeText(getActivity(),pointValue.getX() + " , " + pointValue.getY(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onValueDeselected() {

            }
        });*/
    }

    /**
     * 解决图表与ScrollView滑动冲突
     * 图表可上下左右滑动，ScrollView可上下滑动
     */
    View.OnTouchListener touchListener = new View.OnTouchListener() {

        float yStart = 0f;
        float yEnd = 0f;
        long startTime;
        long endTime;
        float speed = 0f;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    yStart = event.getY();
                    startTime = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_MOVE:
                    yEnd = event.getY();
                    endTime = System.currentTimeMillis();
                    speed = (yEnd - yStart) / (endTime - startTime);
                    //LogUtil.e("speed",speed + "");
/*                    if (Math.abs(yEnd - yStart) > 300f) {
                        svCharts.requestDisallowInterceptTouchEvent(false);
                    }*/
                    if (speed > 1f || speed < -1.2f) {
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
                time = "2021-03-09 00:00:00~2021-03-09 24:00:00";
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
        if ("本日".equals(selectedTime))
        {
            List<AxisValue> xAxisValues = setXAxisValues("本日", xConvertData);
            List<AxisValue> yLabel = setAxisYLabel(minResponse.get(1).toString(), xConvertData);
            convertLines(xConvertData,interval,maxInterval);
            setChart(chartX,xConvertData,xAxisValues,yLabel);

            xAxisValues = setXAxisValues("本日", yConvertData);
            yLabel = setAxisYLabel(minResponse.get(2).toString(), yConvertData);
            convertLines(yConvertData,interval,maxInterval);
            setChart(chartY,yConvertData,xAxisValues,yLabel);

            xAxisValues = setXAxisValues("本日", hConvertData);
            yLabel = setAxisYLabel(minResponse.get(3).toString(), hConvertData);
            convertLines(hConvertData,interval,maxInterval);
            setChart(chartH,hConvertData,xAxisValues,yLabel);
        }
        else {
//            layoutChartX.setVisibility(View.INVISIBLE);
//            layoutChartY.setVisibility(View.INVISIBLE);
//            layoutChartH.setVisibility(View.INVISIBLE);
//            chartX.setVisibility(View.INVISIBLE);
//            initXYHChartValue();
//            List<AxisValue> xAxisValues = setXAxisValues(selectedTime, xValues);
//            List<AxisValue> yAxisValues = setAxisYLabel("3550716.439", xValues);
//            setChart(chartX, xValues, xAxisValues, yAxisValues);
//            setChart(chartY, yValues, xAxisValues, yAxisValues);
//            setChart(chartH, hValues, xAxisValues, yAxisValues);

        }
    }


    /**
     * 绘制位移图表
     */
    public void drawDeltaChart(String selectedTime) {
//        Log.e("drawDeltaChart", "deltaD size: " + deltaDConvertData.size() + " deltaH size: " + deltaHConvertData.size());
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
        if ("本日".equals(selectedTime))
        {
            xChartName.setText("水平位移图");
            xChartTime.setText(time);
            xChartCoo.setText("WGS84坐标系|");

            yChartName.setText("垂直位移图");
            yChartTime.setText(time);
            yChartCoo.setText("WGS84坐标系|");

            layoutChartX.setVisibility(View.VISIBLE);
            layoutChartY.setVisibility(View.VISIBLE);
            layoutChartH.setVisibility(View.GONE);

            List<AxisValue> xAxisValues = setXAxisValues("本日", deltaDConvertData);
            List<AxisValue> yLabel = setAxisYLabel(minResponse.get(4).toString(), deltaDConvertData);
            convertLines(deltaDConvertData,interval,maxInterval);
            setChart(chartX,deltaDConvertData,xAxisValues,yLabel);

            xAxisValues = setXAxisValues("本日", deltaHConvertData);
            yLabel = setAxisYLabel(minResponse.get(5).toString(), deltaHConvertData);
            convertLines(deltaHConvertData,interval,maxInterval);
            setChart(chartY,deltaHConvertData,xAxisValues,yLabel);
        }
        else {
            /*initDeltaChartValue();
            xChartName.setText("水平位移图");
            xChartTime.setText(time);
            xChartCoo.setText("WGS84坐标系|");

            yChartName.setText("垂直位移图");
            yChartTime.setText(time);
            yChartCoo.setText("WGS84坐标系|");*/

            layoutChartX.setVisibility(View.GONE);
            layoutChartY.setVisibility(View.GONE);
            layoutChartH.setVisibility(View.GONE);

/*            List<AxisValue> xAxisValues = setXAxisValues(selectedTime, deltaValues);
            List<AxisValue> yAxisValues = setAxisYLabel("",deltaValues);
            setChart(chartX, deltaValues, xAxisValues, yAxisValues);
            setChart(chartY, deltaValues, xAxisValues, yAxisValues);*/
        }
    }

    public void drawHeartChart(String selectedTime) {
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
                time = "2021-03-09 00:00:00~2021-03-09 24:00:00";
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
        xChartName.setText("心型图");
        xChartTime.setText(time);
        xChartCoo.setText("WGS84坐标系|");

        layoutChartX.setVisibility(View.VISIBLE);
        layoutChartY.setVisibility(View.GONE);
        layoutChartH.setVisibility(View.GONE);

        List<AxisValue> xAxisValues = new ArrayList<>();
        List<AxisValue> yAxisValues = new ArrayList<>();
        List<PointValue> pointValues = new ArrayList<>();

        convertHeartChartData(contentResponse, xAxisValues, yAxisValues, pointValues);
//        Log.e("afterConvert", "xAxisValues size: " + xAxisValues.size() + " yAxisValues size: " + yAxisValues.size());

//        setChart(chartX, xValues, xAxisValues, yAxisValues);
        setHeartChart(chartX, pointValues, xAxisValues, yAxisValues);
    }

    public void setHeartChart(LineChartView chartView, List<PointValue> values, List<AxisValue> xAxisValues, List<AxisValue> yAxisValues) {
        String selectedTime = spTime.getSelectedItem().toString();
        Line line = new Line(values).setColor(Color.parseColor("#2196F3")).setCubic(false).setPointRadius(0).setStrokeWidth(2);
        List<Line> lines = new ArrayList<>();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        Axis axisX = new Axis().setHasLines(true).setLineColor(Color.BLACK).setTextColor(Color.BLACK);
        axisX.setValues(xAxisValues);
        axisX.setHasTiltedLabels(true);
        axisX.setMaxLabelChars(10);
        Axis axisY = new Axis().setHasLines(true).setLineColor(Color.BLACK).setTextColor(Color.BLACK);
        axisY.setValues(yAxisValues);
        axisY.setMaxLabelChars(10);

        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        chartView.setLineChartData(data);
        chartView.setInteractive(true);
        chartView.setMaxZoom(20f);

        Viewport viewport = chartView.getMaximumViewport();
        viewport.top = viewport.top + 0.01f;
        viewport.bottom = viewport.bottom - 0.01f;
        viewport.left = viewport.left - 0.01f;
        viewport.right = viewport.right + 0.01f;
        chartView.setMaximumViewport(viewport);

    }

    public void convertHeartChartData(List<List<Object>> contentList, List<AxisValue> xAxisValues, List<AxisValue> yAxisValues,  List<PointValue> pointValues) {
//        Log.wtf("WTF", "XMax: " + maxResponse.get(1).toString()+ " XMin:  "+ minResponse.get(1).toString() + " YMax:" + maxResponse.get(2).toString() + " YMin" + minResponse.get(2).toString());
        double convertYMin, convertYMax, convertXMin, convertXMax;
        DecimalFormat df = new DecimalFormat("#.00");
        String space = "0.01";
        BigDecimal bSpace = new BigDecimal(space);
        BigDecimal bResponseYMin = new BigDecimal(df.format(Double.parseDouble(minResponse.get(2).toString())));
        BigDecimal bResponseYMax = new BigDecimal(df.format(Double.parseDouble(maxResponse.get(2).toString())));

        convertYMin = Double.parseDouble(String.valueOf(bResponseYMin));
        convertYMax = Double.parseDouble(String.valueOf(bResponseYMax));
        double tempDouble = Double.parseDouble(String.valueOf(bResponseYMin));
        String tempString = String.valueOf(convertYMin);
        BigDecimal strYMin = new BigDecimal(df.format(Double.parseDouble(minResponse.get(2).toString())));
        while (tempDouble <= convertYMax) {
            AxisValue axisValue = new AxisValue(Float.parseFloat(df.format(tempDouble - convertYMin)));
            axisValue.setLabel(tempString);
            yAxisValues.add(axisValue);
            bResponseYMin = bResponseYMin.add(bSpace);
            strYMin = strYMin.add(bSpace);
            tempString = String.valueOf(strYMin);
            tempDouble = Double.parseDouble(String.valueOf(bResponseYMin));
        }

        BigDecimal bResponseXMin = new BigDecimal(df.format(Double.parseDouble(minResponse.get(1).toString())));
        BigDecimal bResponseXMax = new BigDecimal(df.format(Double.parseDouble(maxResponse.get(1).toString())));


        convertXMin = Double.parseDouble(String.valueOf(bResponseXMin));
        convertXMax = Double.parseDouble(String.valueOf(bResponseXMax));
        tempDouble = Double.parseDouble(String.valueOf(bResponseXMin));
        tempString = String.valueOf(convertXMin);
        BigDecimal strXMin = new BigDecimal(df.format(Double.parseDouble(minResponse.get(1).toString())));
        while (tempDouble <= convertXMax) {
            AxisValue axisValue = new AxisValue(Float.parseFloat(df.format(tempDouble - convertXMin)));
            axisValue.setLabel(tempString);
            xAxisValues.add(axisValue);
            bResponseXMin = bResponseXMin.add(bSpace);
            strXMin = strXMin.add(bSpace);
            tempString = String.valueOf(strXMin);
            tempDouble = Double.parseDouble(String.valueOf(bResponseXMin));
//            Log.e("loop", "bResponseXMin: " + String.valueOf(bResponseXMin) + " strXMin : " + strXMin + " tempString: " + tempString + " tempFloat: " + String.valueOf(tempDouble));

        }
        double tempX, tempY;
        for (List<Object> content : contentList) {

            tempX = Double.parseDouble(df.format(Double.parseDouble(content.get(1).toString())).toString()) - Double.parseDouble(df.format(Double.parseDouble(minResponse.get(1).toString())));
            tempY = Double.parseDouble(df.format(Double.parseDouble(content.get(2).toString())).toString()) - Double.parseDouble(df.format(Double.parseDouble(minResponse.get(2).toString())));
            pointValues.add(new PointValue(Float.parseFloat(String.valueOf(tempX)), Float.parseFloat(String.valueOf(tempY))));
        }
    }

    /**
     * 模拟XYH图表的数据并进行处理，数据的获取解析以及裁剪
     */
//    public void initXYHChartValue() {
//        /*接口完成后，在此获取返回的XYH数据*/
//        if ("最近1小时".equals(spTime.getSelectedItem().toString())) {
//            preValues.add(new MyPointValue(1, 3550716.442));
//            preValues.add(new MyPointValue(2, 3550716.439));
//            preValues.add(new MyPointValue(3, 3550716.440));
//            preValues.add(new MyPointValue(4, 3550716.443));
//            preValues.add(new MyPointValue(5, 3550716.442));
//            preValues.add(new MyPointValue(6, 3550716.442));
//            preValues.add(new MyPointValue(7, 3550716.442));
//            preValues.add(new MyPointValue(8, 3550716.443));
//            preValues.add(new MyPointValue(9, 3550716.443));
//            preValues.add(new MyPointValue(10, 3550716.443));
//            preValues.add(new MyPointValue(11, 3550716.444));
//            preValues.add(new MyPointValue(12, 3550716.444));
//            preValues.add(new MyPointValue(13, 3550716.444));
//        } else if ("本周".equals(spTime.getSelectedItem().toString())) {
//            preValues.add(new MyPointValue(1, 3550716.442));
//            preValues.add(new MyPointValue(2, 3550716.439));
//            preValues.add(new MyPointValue(3, 3550716.444));
//            preValues.add(new MyPointValue(4, 3550716.440));
//            preValues.add(new MyPointValue(5, 3550716.441));
//            preValues.add(new MyPointValue(6, 3550716.443));
//            preValues.add(new MyPointValue(7, 3550716.442));
//            preValues.add(new MyPointValue(8, 3550716.443));
//            preValues.add(new MyPointValue(9, 3550716.441));
//            preValues.add(new MyPointValue(10, 3550716.443));
//            preValues.add(new MyPointValue(11, 3550716.444));
//            preValues.add(new MyPointValue(12, 3550716.444));
//            preValues.add(new MyPointValue(13, 3550716.440));
//            preValues.add(new MyPointValue(14, 3550716.442));
//            preValues.add(new MyPointValue(15, 3550716.443));
//        } else if ("本月".equals(spTime.getSelectedItem().toString())) {
//            preValues.add(new MyPointValue(1, 3550716.442));
//            preValues.add(new MyPointValue(2, 3550716.439));
//            preValues.add(new MyPointValue(3, 3550716.442));
//            preValues.add(new MyPointValue(4, 3550716.440));
//            preValues.add(new MyPointValue(5, 3550716.441));
//            preValues.add(new MyPointValue(6, 3550716.440));
//            preValues.add(new MyPointValue(7, 3550716.442));
//            preValues.add(new MyPointValue(8, 3550716.443));
//            preValues.add(new MyPointValue(9, 3550716.445));
//            preValues.add(new MyPointValue(10, 3550716.443));
//            preValues.add(new MyPointValue(11, 3550716.441));
//            preValues.add(new MyPointValue(12, 3550716.444));
//            preValues.add(new MyPointValue(13, 3550716.440));
//            preValues.add(new MyPointValue(14, 3550716.442));
//            preValues.add(new MyPointValue(15, 3550716.443));
//            //preValues.add(new MyPointValue(16, 3550716.445));
//        } else if ("本年".equals(spTime.getSelectedItem().toString())) {
//            preValues.add(new MyPointValue(1, 3550716.442));
//            preValues.add(new MyPointValue(2, 3550716.439));
//            preValues.add(new MyPointValue(3, 3550716.444));
//            preValues.add(new MyPointValue(4, 3550716.440));
//            preValues.add(new MyPointValue(5, 3550716.441));
//            preValues.add(new MyPointValue(6, 3550716.443));
//            preValues.add(new MyPointValue(7, 3550716.442));
//            preValues.add(new MyPointValue(8, 3550716.443));
//            preValues.add(new MyPointValue(9, 3550716.441));
//            preValues.add(new MyPointValue(10, 3550716.443));
//            preValues.add(new MyPointValue(11, 3550716.444));
//            preValues.add(new MyPointValue(12, 3550716.444));
//            preValues.add(new MyPointValue(13, 3550716.440));
//        } else {   //数据变化
//            preValues.add(new MyPointValue(1, 3550716.444));
//            preValues.add(new MyPointValue(2, 3550716.443));
//            preValues.add(new MyPointValue(3, 3550716.443));
//            preValues.add(new MyPointValue(4, 3550716.443));
//            preValues.add(new MyPointValue(5, 3550716.444));
//            preValues.add(new MyPointValue(6, 3550716.440));
//            preValues.add(new MyPointValue(7, 3550716.444));
//            preValues.add(new MyPointValue(8, 3550716.442));
//            preValues.add(new MyPointValue(9, 3550716.439));
//            preValues.add(new MyPointValue(10, 3550716.440));
//            preValues.add(new MyPointValue(11, 3550716.443));
//            preValues.add(new MyPointValue(12, 3550716.442));
//            preValues.add(new MyPointValue(13, 3550716.442));
//        }
//
//        //将数据转换成与格式化后最小值的差值，可以不再对数据进行裁剪
//        double preYMin = 0;
//        double preYMax = 0;
//        preYMin = preValues.get(0).getY();
//        preYMax = preValues.get(0).getY();
//        for (MyPointValue pointValue: preValues) {
//            if (pointValue.getY() > preYMax)
//            {
//                preYMax = pointValue.getY();
//            }
//            if (pointValue.getY() < preYMin)
//            {
//                preYMin = pointValue.getY();
//            }
//        }
//        DecimalFormat df = new DecimalFormat("#.00");//只保留小数点后两位，厘米级精度
//        BigDecimal tempYmin = new BigDecimal(df.format(preYMin));
//        preYMin = Double.parseDouble(String.valueOf(tempYmin));
//        float convertValue;
//        for (MyPointValue pointValue: preValues) {
//            convertValue = (float) (pointValue.getY() - preYMin);
//            xValues.add(new PointValue((float) pointValue.getX(), convertValue));
//            yValues.add(new PointValue((float) pointValue.getX(), convertValue));
//            hValues.add(new PointValue((float) pointValue.getX(), convertValue));
//        }
//
//    }

    /**
     * 模拟位移图的数据
     */
//    public void initDeltaChartValue() {
//        List<MyPointValue> returnValues = new ArrayList<>();  //模拟返回数据
//        if ("本周".equals(spTime.getSelectedItem().toString())) {
//            returnValues.add(new MyPointValue(3550716.4521, 404979.4331));
//            returnValues.add(new MyPointValue(3550716.446, 404979.4331));
//            returnValues.add(new MyPointValue(3550716.4455, 404979.4332));
//            returnValues.add(new MyPointValue(3550716.4446, 404979.4335));
//            returnValues.add(new MyPointValue(3550716.4441, 404979.4342));
//            returnValues.add(new MyPointValue(3550716.444, 404979.4346));
//            returnValues.add(new MyPointValue(3550716.4443, 404979.4348));
//            returnValues.add(new MyPointValue(3550716.4457, 404979.4338));
//            returnValues.add(new MyPointValue(3550716.446, 404979.434));
//            returnValues.add(new MyPointValue(3550716.4471, 404979.434));
//            returnValues.add(new MyPointValue(3550716.4485, 404979.4339));
//            returnValues.add(new MyPointValue(3550716.4481, 404979.4334));
//            returnValues.add(new MyPointValue(3550716.4481, 404979.4343));
//            returnValues.add(new MyPointValue(3550716.4487, 404979.4331));
//            returnValues.add(new MyPointValue(3550716.4485, 404979.4335));
//        } else if ("本月".equals(spTime.getSelectedItem().toString())) {
//            returnValues.add(new MyPointValue(3550716.446, 404979.434));
//            returnValues.add(new MyPointValue(3550716.4471, 404979.434));
//            returnValues.add(new MyPointValue(3550716.4485, 404979.4339));
//            returnValues.add(new MyPointValue(3550716.4481, 404979.4334));
//            returnValues.add(new MyPointValue(3550716.4481, 404979.4343));
//            returnValues.add(new MyPointValue(3550716.4487, 404979.4331));
//            returnValues.add(new MyPointValue(3550716.4485, 404979.4335));
//            returnValues.add(new MyPointValue(3550716.4485, 404979.4335));
//            returnValues.add(new MyPointValue(3550716.4521, 404979.4331));
//            returnValues.add(new MyPointValue(3550716.446, 404979.4331));
//            returnValues.add(new MyPointValue(3550716.4455, 404979.4332));
//            returnValues.add(new MyPointValue(3550716.4446, 404979.4335));
//            returnValues.add(new MyPointValue(3550716.4441, 404979.4342));
//            returnValues.add(new MyPointValue(3550716.444, 404979.4346));
//            returnValues.add(new MyPointValue(3550716.4443, 404979.4348));
//            returnValues.add(new MyPointValue(3550716.4457, 404979.4338));
//        } else if ("本年".equals(spTime.getSelectedItem().toString())) {
//            returnValues.add(new MyPointValue(3550716.4521, 404979.4331));
//            returnValues.add(new MyPointValue(3550716.446, 404979.4331));
//            returnValues.add(new MyPointValue(3550716.4455, 404979.4332));
//            returnValues.add(new MyPointValue(3550716.4446, 404979.4335));
//            returnValues.add(new MyPointValue(3550716.4441, 404979.4342));
//            returnValues.add(new MyPointValue(3550716.444, 404979.4346));
//            returnValues.add(new MyPointValue(3550716.4443, 404979.4348));
//            returnValues.add(new MyPointValue(3550716.4457, 404979.4338));
//            returnValues.add(new MyPointValue(3550716.446, 404979.434));
//            returnValues.add(new MyPointValue(3550716.4471, 404979.434));
//            returnValues.add(new MyPointValue(3550716.4485, 404979.4339));
//            returnValues.add(new MyPointValue(3550716.4481, 404979.4334));
//            returnValues.add(new MyPointValue(3550716.4481, 404979.4333));
//        } else {
//            returnValues.add(new MyPointValue(3550716.4521, 404979.4331));
//            returnValues.add(new MyPointValue(3550716.446, 404979.4331));
//            returnValues.add(new MyPointValue(3550716.4455, 404979.4332));
//            returnValues.add(new MyPointValue(3550716.4446, 404979.4335));
//            returnValues.add(new MyPointValue(3550716.4441, 404979.4342));
//            returnValues.add(new MyPointValue(3550716.444, 404979.4346));
//            returnValues.add(new MyPointValue(3550716.4443, 404979.4348));
//            returnValues.add(new MyPointValue(3550716.4457, 404979.4338));
//            returnValues.add(new MyPointValue(3550716.446, 404979.434));
//            returnValues.add(new MyPointValue(3550716.4471, 404979.434));
//            returnValues.add(new MyPointValue(3550716.4485, 404979.4339));
//            returnValues.add(new MyPointValue(3550716.4481, 404979.4334));
//            returnValues.add(new MyPointValue(3550716.4481, 404979.4333));
//        }
//        /*数据处理，如果接口直接返回处理过的数据最好*/
//        double x0, y0, result;
//        float i = 0;
//        x0 = returnValues.get(0).getX();   //模拟基准点
//        y0 = returnValues.get(0).getY();
//        for (MyPointValue point : returnValues) {
//            i++;
//            result = Math.sqrt(Math.pow(point.getX() - x0, 2) + Math.pow(point.getY() - y0, 2));
//            deltaValues.add(new PointValue(i, (float) result));
//        }
//    }


    //修改之后
    public List<AxisValue> setAxisYLabel(String yMin, List<PointValue> values) {
        Log.e("setAxisYLabel", values.size() + "");
        String tempString;
        float chartValue;
        List<AxisValue> axisValues = new ArrayList<>();
        float valueYMax = values.get(0).getY();
        float valueYMin = values.get(0).getY();

        for (PointValue pointValue : values) {
            //确定最大最小值
            if (pointValue.getY() >= valueYMax) {
                valueYMax = pointValue.getY();
            }
            if (pointValue.getY() <= valueYMin) {
                valueYMin = pointValue.getY();
            }
        }

        String space = "0.01";     //每格大小为0.01m
        convertYMax = valueYMax + 0.1f;
        convertYMin = valueYMin - 0.1f;

        DecimalFormat df = new DecimalFormat("#.00");//只保留小数点后两位，厘米级精度
        BigDecimal b_ymin = new BigDecimal(df.format(convertYMin));
        BigDecimal tempYmin = new BigDecimal(df.format(Double.parseDouble(yMin)));
        BigDecimal b_space = new BigDecimal(space);
        chartValue = convertYMin;
        tempYmin = tempYmin.subtract(new BigDecimal("0.1"));

/*      LogUtil.e("减0.1之后的最小值", String.valueOf(tempYmin));
        LogUtil.e("tempfloat", String.valueOf(tempfloat));
        LogUtil.e("yMax after", String.valueOf(df.format(convertYMax)));
        LogUtil.e("yMin after", String.valueOf(df.format(convertYMin)));
        LogUtil.e("bymin", String.valueOf(b_ymin));*/

        BigDecimal s_yMin = new BigDecimal(df.format(tempYmin));  //传入格式化后的最小值

        while (chartValue <= convertYMax) {
            b_ymin = b_ymin.add(b_space);
            s_yMin =s_yMin.add(b_space);
            tempString = String.valueOf(s_yMin);
            chartValue = Float.parseFloat(String.valueOf(b_ymin));
            //LogUtil.e("value lable", String.valueOf(chartValue) + "  " + tempString);
            AxisValue axisValue = new AxisValue(chartValue);
            axisValue.setLabel(tempString);
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
        for (int i = 0; i <= 1440; i += 120) {
            AxisValue axisValue = new AxisValue(i);
            axisValue.setLabel(labels[i / 120]);
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
        List<Line> lines = new ArrayList<>();
        String[] lineColor = {"#2196F3","#DC143C"};
        if ("本日".equals(spTime.getSelectedItem().toString()))   //测试，图表全部画完之后不再需要判断。
        {
            for (int i = 0; i < chartLines.size(); i++) {
                Line line = new Line(chartLines.get(i)).setColor(Color.parseColor(lineColor[i % 2])).setCubic(false).setPointRadius(0).setStrokeWidth(2);
                lines.add(line);
            }
        }
        else
        {
            Line line = new Line(values).setColor(Color.parseColor("#2196F3")).setCubic(false).setPointRadius(0).setStrokeWidth(2);
            lines.add(line);
        }
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
        maxWindow.bottom = convertYMin;
        maxWindow.top = convertYMax;

        //设置当前窗口，将每格长度大约设置成物理上的1cm
        Viewport currentWindow = new Viewport(chartView.getMaximumViewport());
        currentWindow.bottom = convertYMin + 0.0635f;
        currentWindow.top = convertYMax - 0.0635f;
        switch (selectedTime) {
            case "本周":
                maxWindow.right = 15.2f;
                currentWindow.right = 15.2f;
                break;
            case "本月":
                maxWindow.right = 16.2f;
                currentWindow.right = 16.2f;
                break;
            case "本日":
                //经过计算：1dp = 0.015875cm；600dp = 9.525cm，所以设置当前窗口显示9个刻度即可保证一格为1cm
                currentWindow.bottom = convertAverage - 0.05f;
                currentWindow.top = convertAverage + 0.04f;
                maxWindow.right = maxWindow.right + 8f;
                maxWindow.left = 0f;
                currentWindow.left = maxWindow.left;
                currentWindow.right = (float) (maxWindow.right * 0.5);
                break;
            default:
                maxWindow.right = 13.2f;
                currentWindow.right = 13.2f;
                break;
        }
        //LogUtil.e("1cm转换成px",String.valueOf(ScreenUtil.cm2px(getActivity(),1)));
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
                if(fm.getBackStackEntryAt(fm.getBackStackEntryCount()-1).getName().equals("projectFragment")){
                    activity.setNowFragment(activity.getProjectFragment());
                    activity.getNavigationView().setSelectedItemId(activity.getNavigationView().getMenu().getItem(0).getItemId());
                }else{
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
        xConvertData.clear();
        yConvertData.clear();
        hConvertData.clear();
        deltaDConvertData.clear();
        deltaHConvertData.clear();
    }

    public void switchChart() {
        Log.e("switchChart", "switching...");
        String selectedTime = spTime.getSelectedItem().toString();
        switch (spChart.getSelectedItem().toString()) {
            case "XYH":
                cleanValues();
                drawXYHChart(selectedTime);
                break;
            case "位移图":
                //cleanValues();
                drawDeltaChart(selectedTime);
                break;
            case "心型图":
                cleanValues();
                drawHeartChart(selectedTime);
                break;
        }
    }

    public void requestChartDataSync(final String graphicType, String stationUUID, String startTime, String endTime)
    {
        FormBody getChartDataBody = new FormBody.Builder()
                .add("AccessToken", ApiConfig.getAccessToken())
                .add("SessionUUID",ApiConfig.getSessionUUID())
                .add("StationUUID",stationUUID)
                .add("GraphicType",graphicType)
                .add("StartTime",startTime)
                .add("EndTime",endTime)
                .build();
        final SimpleDateFormat sdfTwo =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        LogUtil.e("请求点数据开始时间",sdfTwo.format(System.currentTimeMillis()));
        Api.config(ApiConfig.GET_GRAPHIC_DATA).postRequestFormBodySync(getChartDataBody, new ApiCallback() {
            @Override
            public void onSuccess(String res) {
                LogUtil.e("请求点数据结束时间",sdfTwo.format(System.currentTimeMillis()));
                Gson gson = new Gson();
                GNSSFilterInfoResponse gnssFilterInfoResponse = gson.fromJson(res, GNSSFilterInfoResponse.class);
                contentResponse = gnssFilterInfoResponse.getContent();
                maxResponse = gnssFilterInfoResponse.getMax();
                minResponse = gnssFilterInfoResponse.getMin();
                averageResponse = gnssFilterInfoResponse.getAverage();
                interval = gnssFilterInfoResponse.getData().getInterval();
                maxInterval = gnssFilterInfoResponse.getData().getMaxInterval();
                List<List<String>> xResponseData = new ArrayList<>();
                List<List<String>> yResponseData = new ArrayList<>();
                List<List<String>> hResponseData = new ArrayList<>();
                List<List<String>> deltaDResponseData = new ArrayList<>();
                List<List<String>> deltaHResponseData = new ArrayList<>();
                //数据提取
                for (List<Object> responseData : contentResponse) {
                    List<String> xValue = new ArrayList<>();
                    List<String> yValue = new ArrayList<>();
                    List<String> hValue = new ArrayList<>();
                    List<String> deltaDValue = new ArrayList<>();
                    List<String> deltaHValue = new ArrayList<>();
                    //时间
                    xValue.add(String.valueOf(Math.round((double)responseData.get(0))));
                    yValue.add(String.valueOf(Math.round((double)responseData.get(0))));
                    hValue.add(String.valueOf(Math.round((double)responseData.get(0))));
                    deltaDValue.add(String.valueOf(Math.round((double)responseData.get(0))));
                    deltaHValue.add(String.valueOf(Math.round((double)responseData.get(0))));
                    //数值
                    xValue.add(String.valueOf(responseData.get(1)));
                    yValue.add(String.valueOf(responseData.get(2)));
                    hValue.add(String.valueOf(responseData.get(3)));
                    deltaDValue.add(String.valueOf(responseData.get(4)));
                    deltaHValue.add(String.valueOf(responseData.get(5)));
                    //添加到各自的数据列表中
                    xResponseData.add(xValue);
                    yResponseData.add(yValue);
                    hResponseData.add(hValue);
                    deltaDResponseData.add(deltaDValue);
                    deltaHResponseData.add(deltaHValue);
                }
                xConvertData = convertData(xResponseData, minResponse.get(1).toString(), "2021-03-09 00:00:00", averageResponse.get(1).toString());
                yConvertData = convertData(yResponseData, minResponse.get(2).toString(), "2021-03-09 00:00:00", averageResponse.get(2).toString());
                hConvertData = convertData(hResponseData, minResponse.get(3).toString(), "2021-03-09 00:00:00", averageResponse.get(3).toString());
                deltaDConvertData = convertData(deltaDResponseData, minResponse.get(4).toString(), "2021-03-09 00:00:00", averageResponse.get(4).toString());
                deltaHConvertData = convertData(deltaHResponseData, minResponse.get(5).toString(), "2021-03-09 00:00:00", averageResponse.get(5).toString());
                LogUtil.e("getData执行结束","*********");
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    /**
     * 处理接口返回数据
     * @param responseData 接口返回的数据列表。内层列表中，索引0存储时间，索引1存储数据.
     * @param responseMin 接口返回值的最小值.
     * @return  转换后可用于画图的数据。
     */
    public List<PointValue> convertData(List<List<String>> responseData,String responseMin,String startTime,String responseAverage)
    {
        List<PointValue> convertData = new ArrayList<>();
        String time;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long axisXValue = null;
        Long axisX0 = null;
        double valueMin = Double.parseDouble(responseMin);
        float convertValue;

        DecimalFormat df = new DecimalFormat("#.00");//只保留小数点后两位，厘米级精度
        BigDecimal convertMin = new BigDecimal(df.format(valueMin));
        valueMin = Double.parseDouble(String.valueOf(convertMin));
        convertAverage = (float)(Double.parseDouble(responseAverage) - valueMin);

        try {
            axisX0 = simpleDateFormat.parse(startTime).getTime() / 1000 / 60; //获取分钟级时间戳
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (List<String> data : responseData) {
            time = data.get(0);   //0是时间，1是数据
            convertValue = (float) (Double.parseDouble(data.get(1)) - valueMin);
            axisXValue = Long.parseLong(time) / 60;
            convertData.add(new PointValue((float) (axisXValue - axisX0),convertValue));
        }
/*        for (PointValue pointValue: convertData) {
            LogUtil.e("转换后的时间数据+点数据", pointValue.getX() + "  " + pointValue.getY());
        }*/
        return convertData;
    }

    /**
     * 判断转换后的数据有几段，并转换成线数据。
     * @param values 转换后的数据
     * @param interval 数据间隔
     * @param maxInterval 数据最大间隔
     */
    public void convertLines(List<PointValue> values,int interval,int maxInterval)
    {
        chartLines.clear();
        if (interval == maxInterval)
        {
            chartLines.add(values);
        }
        else {
            List<PointValue> tempLines = new ArrayList<>();
            tempLines.add(new PointValue(values.get(0).getX(),values.get(0).getY()));   //加入第一个数据
            for (int i = 1;i < values.size();i++)
            {
                if ((values.get(i).getX() - values.get(i-1).getX()) != 1)
                {
//                LogUtil.e("断点处",values.get(i-1).getX() + "后一个点为" + values.get(i).getX());
                    chartLines.add(tempLines);
                    tempLines = new ArrayList<>();  //如果用clear()方法清空，那么所有使用该列表的数据都空了。
                }
                tempLines.add(new PointValue(values.get(i).getX(),values.get(i).getY()));
            }
            chartLines.add(tempLines);   //将最后一段数据加入
        }
    }

    public void getData(final String graphicType, final String stationUUID, final String startTime, final String endTime)
    {
        Thread httpThread = new Thread(new Runnable()
        {
            @Override
            public void run() {
                requestChartDataSync(graphicType,stationUUID,startTime,endTime);
            }
        });
        httpThread.start();
        try {
            httpThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}