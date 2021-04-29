package com.beyond.beidou.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.beyond.beidou.api.ApiCallback;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.entites.GNSSFilterInfoResponse;
import com.beyond.beidou.util.DateUtil;
import com.beyond.beidou.util.FileUtil;
import com.beyond.beidou.util.LogUtil;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

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
    private Spinner spDevice, spTime;
    private LineChartView xChart, yChart, hChart,deltaDChart,deltaHChart,heartChart;
    private TextView xChartName, yChartName, hChartName,deltaDChartName,deltaHChartName,heartChartName;
    private TextView xChartCoo, yChartCoo, hChartCoo,deltaDChartCoo,deltaHChartCoo,heartChartCoo;
    private TextView xChartTime, yChartTime, hChartTime,deltaDChartTime,deltaHChartTime,heartChartTime;
    private TextView mTitle;
    private TextView mDownLoadExcel;
    private float convertYMax;
    private float convertYMin;
    private float nConvertAverage, eConvertAverage,hConvertAverage,deltaConvertAverage,deltaHConvertAverage;
    private List<Object> maxResponse = new ArrayList<>();
    private List<Object> minResponse = new ArrayList<>();
    private List<Object> averageResponse = new ArrayList<>();
    private ScrollView svCharts;
    private ImageView imgBack;
    private List<PointValue> xConvertData = new ArrayList<>();
    private List<PointValue> yConvertData = new ArrayList<>();
    private List<PointValue> hConvertData = new ArrayList<>();
    private List<PointValue> deltaDConvertData = new ArrayList<>();
    private List<PointValue> deltaHConvertData = new ArrayList<>();
    private List<List<Object>> contentResponse = new ArrayList<>();
    private List<List<PointValue>> chartLines = new ArrayList<>();
    private int interval, maxInterval;
    private boolean isFirstTimeSelectTime = true;
    private ArrayList<String> stationUUIDList = new ArrayList<>();
    private int selectedDevicePosition;
    private LinearLayout chartXLayout;
    private String startTime = null;
    private String endTime = null;
    private boolean hasData = true;

    private static final int LOADING = 1;
    private ZLoadingDialog dialog;
    public Handler handler = new  Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what)
            {
                case LOADING:
                    LogUtil.e("请求的参数为", stationUUIDList.get(spDevice.getSelectedItemPosition()) + ",  " + startTime + ",   " + endTime);
                    getData("GNSSFilterInfo", stationUUIDList.get(spDevice.getSelectedItemPosition()),startTime,endTime);
                    drawChart();
                    break;
            }
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        dialog = new ZLoadingDialog((MainActivity)context);
    }

    private void doLoadingDialog(){
        handler.sendEmptyMessageDelayed(LOADING,0);
        dialog.setLoadingBuilder(Z_TYPE.ROTATE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK)//颜色
                .setHintText("Loading...")
                .show();
    }

    public static ChartFragment newInstance(String projectName, ArrayList<String> stationNameList, int position,ArrayList<String> stationUUIDList) {
        ChartFragment chartFragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putString("projectName", projectName);
        args.putStringArrayList("stationNameList", stationNameList);
        args.putInt("position", position);
        args.putStringArrayList("stationUUIDList",stationUUIDList);
        chartFragment.setArguments(args);
        return chartFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        initView(view);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        startTime = df.format(DateUtil.getDayBegin());
        endTime = df.format(DateUtil.getDayEnd());
//
//        getData("GNSSFilterInfo", stationUUIDList.get(selectedDevicePosition),startTime,endTime);
//        drawChart();
        doLoadingDialog();
        return view;
    }

    public void initView(final View view) {
        mTitle = view.findViewById(R.id.tv_title);
        spDevice = view.findViewById(R.id.sp_device);
        spTime = view.findViewById(R.id.sp_chart_time);
        mDownLoadExcel = view.findViewById(R.id.tv_load_excel);
        if (getArguments() != null) {
            mTitle.setText(getArguments().getString("projectName"));
            ArrayAdapter<String> deviceAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getArguments().getStringArrayList("stationNameList"));
            deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            spDevice.setAdapter(deviceAdapter);
            selectedDevicePosition = getArguments().getInt("position");
            spDevice.setSelection(selectedDevicePosition, true);
            stationUUIDList = getArguments().getStringArrayList("stationUUIDList");
        }
        String[] times = new String[]{"最近1小时", "最近6小时", "最近12小时", "本日", "一周", "一月", "一年"};
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, times);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spTime.setAdapter(timeAdapter);
        spTime.setSelection(3);

        chartXLayout = view.findViewById(R.id.layout_chartX);

        xChart = view.findViewById(R.id.chart_X);
        yChart = view.findViewById(R.id.chart_Y);
        hChart = view.findViewById(R.id.chart_H);
        deltaDChart = view.findViewById(R.id.chart_DeltaD);
        deltaHChart = view.findViewById(R.id.chart_DeltaH);
        heartChart = view.findViewById(R.id.chart_Heart);

        xChartName = view.findViewById(R.id.tv_chartNameX);
        xChartCoo = view.findViewById(R.id.tv_coordinateSystemX);
        xChartTime = view.findViewById(R.id.tv_chartTimeX);

        yChartName = view.findViewById(R.id.tv_chartNameY);
        yChartCoo = view.findViewById(R.id.tv_coordinateSystemY);
        yChartTime = view.findViewById(R.id.tv_chartTimeY);

        hChartName = view.findViewById(R.id.tv_chartNameH);
        hChartCoo = view.findViewById(R.id.tv_coordinateSystemH);
        hChartTime = view.findViewById(R.id.tv_chartTimeH);

        deltaDChartName = view.findViewById(R.id.tv_DeltaDChartName);
        deltaDChartCoo = view.findViewById(R.id.tv_coordinateSystemDeltaD);
        deltaDChartTime = view.findViewById(R.id.tv_chartTimeDeltaD);

        deltaHChartName = view.findViewById(R.id.tv_chartNameDeltaH);
        deltaHChartCoo = view.findViewById(R.id.tv_coordinateSystemDeltaH);
        deltaHChartTime = view.findViewById(R.id.tv_chartTimeDeltaH);

        heartChartName = view.findViewById(R.id.tv_chartNameHeart);
        heartChartCoo = view.findViewById(R.id.tv_coordinateSystemHeart);
        heartChartTime = view.findViewById(R.id.tv_chartTimeHeart);

        imgBack = view.findViewById(R.id.img_chart_back);
        imgBack.setOnClickListener(this);

        svCharts = view.findViewById(R.id.sv_charts);

        spTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFirstTimeSelectTime) {
                    isFirstTimeSelectTime = false;
                    return;
                }
               refresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mDownLoadExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downLoadExcel();
            }
        });

        xChart.setOnTouchListener(touchListener);
        yChart.setOnTouchListener(touchListener);
        hChart.setOnTouchListener(touchListener);
        deltaHChart.setOnTouchListener(touchListener);
        deltaDChart.setOnTouchListener(touchListener);
        heartChart.setOnTouchListener(touchListener);

//        chartX.setViewportChangeListener(new ViewportChangeListener() {
//            @Override
//            public void onViewportChanged(Viewport viewport) {
//                //1494就是给ViewPort设置的MaxRight
//                if (viewport.right == 1494)
//                {
//                    LogUtil.e("已滑到最右边","1111111");
//                }
////                LogUtil.e("当前ViewPort的最右边的位置",viewport.right + " ");
////                LogUtil.e("当前ViewPort的最左边的位置",viewport.left + " ");
//            }
//        });

//        数据选择监听
//        chartX.setOnValueTouchListener(new LineChartOnValueSelectListener() {
//            @Override
//            public void onValueSelected(int i, int i1, PointValue pointValue) {
//                Toast.makeText(getActivity(), pointValue.getX() + " , " + pointValue.getY(), Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onValueDeselected() {
//
//            }
//        });
//
//        chartY.setOnValueTouchListener(new LineChartOnValueSelectListener() {
//            @Override
//            public void onValueSelected(int i, int i1, PointValue pointValue) {
//                Toast.makeText(getActivity(), pointValue.getX() + " , " + pointValue.getY(), Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onValueDeselected() {
//
//            }
//        });
//
//        chartH.setOnValueTouchListener(new LineChartOnValueSelectListener() {
//            @Override
//            public void onValueSelected(int i, int i1, PointValue pointValue) {
//                Toast.makeText(getActivity(), pointValue.getX() + " , " + pointValue.getY(), Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onValueDeselected() {
//
//            }
//        });

//        chartX.setViewportChangeListener(new ViewportChangeListener() {
//            @Override
//            public void onViewportChanged(Viewport viewport) {
//                LogUtil.e("视图变化","1111");
//                LogUtil.e("ZoomLevel",chartX.getZoomLevel() + " ");
//            }
//        });
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
        List<AxisValue> xAxisValues = setXAxisValues(selectedTime);
        List<AxisValue> yLabel = setAxisYLabel(minResponse.get(1).toString(), xConvertData);
        convertLines(xConvertData);
        setChart(xChart,  xAxisValues, yLabel, nConvertAverage);

        xAxisValues = setXAxisValues(selectedTime);
        yLabel = setAxisYLabel(minResponse.get(2).toString(), yConvertData);
        convertLines(yConvertData);
        setChart(yChart,  xAxisValues, yLabel, eConvertAverage);

        xAxisValues = setXAxisValues(selectedTime);
        yLabel = setAxisYLabel(minResponse.get(3).toString(), hConvertData);
        convertLines(hConvertData);
        setChart(hChart,xAxisValues, yLabel,hConvertAverage);

    }


    /**
     * 绘制位移图表
     */
    public void drawDeltaChart(String selectedTime) {
//        Log.e("drawDeltaChart", "deltaD size: " + deltaDConvertData.size() + " deltaH size: " + deltaHConvertData.size());
        String time = DateUtil.getTimeInterval(selectedTime);
        deltaDChartName.setText("水平位移图");
        deltaDChartTime.setText(time);
        deltaDChartCoo.setText("WGS84坐标系|");

        deltaHChartName.setText("垂直位移图");
        deltaHChartTime.setText(time);
        deltaHChartCoo.setText("WGS84坐标系|");

        List<AxisValue> xAxisValues = setXAxisValues(selectedTime);
        List<AxisValue> yLabel = setAxisYLabel(minResponse.get(4).toString(), deltaDConvertData);
        convertLines(deltaDConvertData);
        setChart(deltaDChart,xAxisValues, yLabel,deltaConvertAverage);

        xAxisValues = setXAxisValues(selectedTime);
        yLabel = setAxisYLabel(minResponse.get(5).toString(), deltaHConvertData);
        convertLines(deltaHConvertData);
        setChart(deltaHChart,xAxisValues, yLabel,deltaHConvertAverage);
    }

    public void drawHeartChart(String selectedTime) {
        String time = DateUtil.getTimeInterval(selectedTime);
        heartChartName.setText("心型图");
        heartChartTime.setText(time);
        heartChartCoo.setText("WGS84坐标系|");

        List<AxisValue> xAxisValues = new ArrayList<>();
        List<AxisValue> yAxisValues = new ArrayList<>();
        List<PointValue> pointValues = new ArrayList<>();

        convertHeartChartData(contentResponse, xAxisValues, yAxisValues, pointValues);
        setHeartChart(heartChart, pointValues, xAxisValues, yAxisValues);

    }

    public void setHeartChart(LineChartView chartView, List<PointValue> values, List<AxisValue> xAxisValues, List<AxisValue> yAxisValues) {
        Line line = new Line(values).setColor(Color.parseColor("#2196F3")).setCubic(false).setPointRadius(0).setStrokeWidth(2);
        List<Line> lines = new ArrayList<>();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        Axis axisX = new Axis().setHasLines(true).setLineColor(Color.BLACK).setTextColor(Color.BLACK);
        axisX.setValues(xAxisValues);
        axisX.setHasTiltedLabels(true);
        axisX.setMaxLabelChars(10);
        axisX.setName("单位(米)");
        Axis axisY = new Axis().setHasLines(true).setLineColor(Color.BLACK).setTextColor(Color.BLACK);
        axisY.setValues(yAxisValues);
        axisY.setMaxLabelChars(10);
        axisY.setName("单位(米)");

        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        chartView.setLineChartData(data);
        chartView.setInteractive(true);
        chartView.setMaxZoom(20f);
        chartView.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        heartChart.setVisibility(View.VISIBLE);

        Viewport viewport = chartView.getMaximumViewport();
        viewport.top = viewport.top + 0.02f;
        viewport.bottom = viewport.bottom - 0.02f;
        viewport.left = viewport.left - 0.02f;
        viewport.right = viewport.right + 0.02f;
        chartView.setMaximumViewport(viewport);

        chartView.setZoomLevel(0f, 0f, 0f);
    }

    public void convertHeartChartData(List<List<Object>> contentList, List<AxisValue> xAxisValues, List<AxisValue> yAxisValues, List<PointValue> pointValues) {
        double convertXMin, convertXMax, convertYMin, convertYMax;
        DecimalFormat df = new DecimalFormat("#.00");
        String space = "0.01";
        BigDecimal bSpace = new BigDecimal(space);
        BigDecimal bResponseXmin = new BigDecimal(df.format(Double.parseDouble(minResponse.get(2).toString())));
        BigDecimal bResponseXMax = new BigDecimal(df.format(Double.parseDouble(maxResponse.get(2).toString())));

        bResponseXmin = bResponseXmin.subtract(new BigDecimal("0.02"));
        bResponseXMax = bResponseXMax.add(new BigDecimal("0.02"));

        convertXMin = Double.parseDouble(String.valueOf(bResponseXmin));
        convertXMax = Double.parseDouble(String.valueOf(bResponseXMax));
        double tempDouble = Double.parseDouble(String.valueOf(bResponseXmin));
        String tempString = String.valueOf(convertXMin);
        BigDecimal strXMin = new BigDecimal(df.format(Double.parseDouble(minResponse.get(2).toString())));
        strXMin = strXMin.subtract(new BigDecimal("0.02"));
        while (tempDouble <= convertXMax) {
            AxisValue axisValue = new AxisValue(Float.parseFloat(df.format(tempDouble - convertXMin)));
            axisValue.setLabel(tempString);
            xAxisValues.add(axisValue);
            bResponseXmin = bResponseXmin.add(bSpace);
            strXMin = strXMin.add(bSpace);
            tempString = String.valueOf(strXMin);
            tempDouble = Double.parseDouble(String.valueOf(bResponseXmin));
        }

        BigDecimal bResponseYMin = new BigDecimal(df.format(Double.parseDouble(minResponse.get(1).toString())));
        BigDecimal bResponseYMax = new BigDecimal(df.format(Double.parseDouble(maxResponse.get(1).toString())));

        bResponseYMin = bResponseYMin.subtract(new BigDecimal("0.02"));
        bResponseYMax = bResponseYMax.add(new BigDecimal("0.02"));

        convertYMin = Double.parseDouble(String.valueOf(bResponseYMin));
        convertYMax = Double.parseDouble(String.valueOf(bResponseYMax));
        tempDouble = Double.parseDouble(String.valueOf(bResponseYMin));
        tempString = String.valueOf(convertYMin);
        BigDecimal strYMin = new BigDecimal(df.format(Double.parseDouble(minResponse.get(1).toString())));
        strYMin = strYMin.subtract(new BigDecimal("0.02"));
        while (tempDouble <= convertYMax) {
            AxisValue axisValue = new AxisValue(Float.parseFloat(df.format(tempDouble - convertYMin)));
            axisValue.setLabel(tempString);
            yAxisValues.add(axisValue);
            bResponseYMin = bResponseYMin.add(bSpace);
            strYMin = strYMin.add(bSpace);
            tempString = String.valueOf(strYMin);
            tempDouble = Double.parseDouble(String.valueOf(bResponseYMin));
        }


        double tempX, tempY;
        BigDecimal minuendY1 = new BigDecimal(df.format(Double.parseDouble(minResponse.get(1).toString())));
        minuendY1 = minuendY1.subtract(new BigDecimal("0.02"));
        BigDecimal minuendX1 = new BigDecimal(df.format(Double.parseDouble(minResponse.get(2).toString())));
        minuendX1 = minuendX1.subtract(new BigDecimal("0.02"));
        for (List<Object> content : contentList) {
            tempY = Double.parseDouble(content.get(1).toString()) - Double.parseDouble(minuendY1.toString());
            tempX = Double.parseDouble(content.get(2).toString()) - Double.parseDouble(minuendX1.toString());
            pointValues.add(new PointValue(Float.parseFloat(String.valueOf(tempX)), Float.parseFloat(String.valueOf(tempY))));
        }
    }

    public List<AxisValue> setAxisYLabel(String yMin, List<PointValue> values) {
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

        //错误原因：传回的最小值和实际最小值不一样

//        LogUtil.e("setYLable valueYMax",valueYMax + "");
//        LogUtil.e("setYLable valueYMin",valueYMin + "");
//        LogUtil.e("setYLable 传入的最小值VS返回的最小值",yMin + "" + minResponse.get(1));

        String space = "0.01";     //每格大小为0.01m
        convertYMax = valueYMax + 0.1f;
        convertYMin = valueYMin - 0.1f;

        //保证convertYmin和yMin是真实值。这样即使转成只有两位小数，也是对应的。
        DecimalFormat df = new DecimalFormat("#.00");//只保留小数点后两位，厘米级精度
        BigDecimal b_ymin = new BigDecimal(df.format(convertYMin));
        BigDecimal tempYmin = new BigDecimal(df.format(Double.parseDouble(yMin)));
        BigDecimal b_space = new BigDecimal(space);
        chartValue = convertYMin;
        tempYmin = tempYmin.subtract(new BigDecimal("0.1"));
        BigDecimal s_yMin = new BigDecimal(df.format(tempYmin));  //传入格式化后的最小值

//        LogUtil.e("convertYMin + tempYmin",convertYMin + "  " + tempYmin);

        while (chartValue <= convertYMax) {
            b_ymin = b_ymin.add(b_space);
            s_yMin = s_yMin.add(b_space);
            tempString = String.valueOf(s_yMin);
            chartValue = Float.parseFloat(String.valueOf(b_ymin));
            AxisValue axisValue = new AxisValue(chartValue);
            axisValue.setLabel(tempString);
            axisValues.add(axisValue);
//            LogUtil.e("value lable", String.valueOf(chartValue) + "  " + tempString);
        }
        return axisValues;
    }

    /**
     * 设置X轴标签
     *
     * @param selectedTime 当前所选时间
     * @return 标签列表
     */
    public List<AxisValue> setXAxisValues(String selectedTime) {
        List<AxisValue> xAxisValues = new ArrayList<>();
        switch (selectedTime) {
            case "最近1小时":
                xAxisValues = DateUtil.getHourXAxisLabel(1);
                break;
            case "最近6小时":
                xAxisValues = DateUtil.getHourXAxisLabel(6);
                break;
            case "最近12小时":
                xAxisValues = DateUtil.getHourXAxisLabel(12);
                break;
            case "本日":
                xAxisValues = DateUtil.getDayXAxisLabel();   //本日
                break;
            case "一周":
                xAxisValues = DateUtil.getRecentWeekXAxis();
                break;
            case "一月":
                xAxisValues = DateUtil.getRecentMonthXAxis();
                break;
            case "一年":
                xAxisValues = DateUtil.getRecentYearXAxis();
                break;
        }
        return xAxisValues;
    }

    /**
     * 设置图表属性。填充数据，设置X轴，Y轴的标签。
     *
     * @param chartView   当前图表
     * @param xAxisValues X轴标签（时间）
     * @param yAxisValues Y轴标签
     */
    public void setChart(LineChartView chartView, List<AxisValue> xAxisValues, List<AxisValue> yAxisValues,float convertAverage) {
        String selectedTime = spTime.getSelectedItem().toString();
        List<Line> lines = new ArrayList<>();
            for (int i = 0; i < chartLines.size(); i++) {
                Line line = new Line(chartLines.get(i)).setColor(Color.parseColor("#2196F3")).setCubic(false).setPointRadius(0).setStrokeWidth(2);
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

        axisY.setMaxLabelChars(7);

        if ("一周".equals(selectedTime)) {
            axisX.setMaxLabelChars(8);
        } else if ("一月".equals(selectedTime)) {
            axisX.setMaxLabelChars(10);
        } else if ("一年".equals(selectedTime)) {
            axisX.setMaxLabelChars(12);
        }
//        if ("最近1小时".equals(selectedTime) || "最近6小时".equals(selectedTime) || "最近12小时".equals(selectedTime)) {
//            axisX.setMaxLabelChars(7);
//        }
        //为两个坐标系设定名称
        axisY.setName("单位(米)");
        //设置图标所在位置
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);
        //将数据添加到View中
        chartView.setLineChartData(data);
        chartView.setInteractive(true);
        chartView.setZoomType(ZoomType.HORIZONTAL);
        chartView.setMaxZoom(10f);
        chartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        chartView.setVisibility(View.VISIBLE);

        Viewport maxWindow = new Viewport(chartView.getMaximumViewport());
        maxWindow.bottom = convertYMin;
        maxWindow.top = convertYMax;

        //设置当前窗口，将每格长度大约设置成物理上的1cm
        Viewport currentWindow = new Viewport(chartView.getMaximumViewport());
        //经过计算：1dp = 0.015875cm；600dp = 9.525cm，所以设置当前窗口显示9个刻度即可保证一格为1cm
        currentWindow.bottom = convertAverage - 0.05f;
        currentWindow.top = convertAverage + 0.04f;
        LogUtil.e("currentWindow", "bottom: " + currentWindow.bottom + " , top: " + currentWindow.top);

        switch (spTime.getSelectedItem().toString()) {
            case "最近1小时":
                maxWindow.right = 60 + (float) (60 / 24);
                break;
            case "最近6小时":
                maxWindow.right = 360 + (float) (360 / 24);
                break;
            case "最近12小时":
                maxWindow.right = 720 + (float) (720 / 24);
                break;
            case "本日":
                maxWindow.right = 1440 + (float) (1440 / 24);
                break;
            case "一周":
                maxWindow.right = 1440*7 + (float) (1440*7 / 24);
                break;
            case "一月":
                maxWindow.right = 1440*30 + (float) (1440*30 / 24);
                break;
            case "一年":
                maxWindow.right = 1440*365 + (float) (1440*365 / 24);
                break;
        }

        maxWindow.left = 0f;
        currentWindow.left = maxWindow.left;
        currentWindow.right = (float) (maxWindow.right * 0.5);
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

    public void requestChartDataSync(final String graphicType, String stationUUID, final String startTime, String endTime) {
        FormBody getChartDataBody = new FormBody.Builder()
                .add("AccessToken", ApiConfig.getAccessToken())
                .add("SessionUUID", ApiConfig.getSessionUUID())
                .add("StationUUID", stationUUID)
                .add("GraphicType", graphicType)
                .add("StartTime", startTime)
                .add("EndTime", endTime)
                .add("DeltaTime","60")
                .build();
        final SimpleDateFormat sdfTwo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        LogUtil.e("请求点数据开始时间", sdfTwo.format(System.currentTimeMillis()));
        Api.config(ApiConfig.GET_GRAPHIC_DATA).postRequestFormBodySync(getActivity(),getChartDataBody,new ApiCallback() {
            @Override
            public void onSuccess(String res) {
                LogUtil.e("请求点数据结束时间", sdfTwo.format(System.currentTimeMillis()));
                Gson gson = new Gson();
                GNSSFilterInfoResponse gnssFilterInfoResponse = gson.fromJson(res, GNSSFilterInfoResponse.class);
                contentResponse = gnssFilterInfoResponse.getContent();
                LogUtil.e("返回的Content值",contentResponse + " ");
                if (contentResponse.size() == 0)
                {
                    hasData = false;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            xChart.setVisibility(View.GONE);
                            yChart.setVisibility(View.GONE);
                            hChart.setVisibility(View.GONE);
                            deltaDChart.setVisibility(View.GONE);
                            deltaHChart.setVisibility(View.GONE);
                            heartChart.setVisibility(View.GONE);
                            showToast("监测点暂无数据");
                        }
                    });
                    return;
                }
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
                    xValue.add(String.valueOf(Math.round((double) responseData.get(0))));
                    yValue.add(String.valueOf(Math.round((double) responseData.get(0))));
                    hValue.add(String.valueOf(Math.round((double) responseData.get(0))));
                    deltaDValue.add(String.valueOf(Math.round((double) responseData.get(0))));
                    deltaHValue.add(String.valueOf(Math.round((double) responseData.get(0))));
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
                xConvertData = convertData(xResponseData, minResponse.get(1).toString(), startTime, averageResponse.get(1).toString());
                yConvertData = convertData(yResponseData, minResponse.get(2).toString(), startTime, averageResponse.get(2).toString());
                hConvertData = convertData(hResponseData, minResponse.get(3).toString(), startTime, averageResponse.get(3).toString());
                deltaDConvertData = convertData(deltaDResponseData, minResponse.get(4).toString(), startTime, averageResponse.get(4).toString());
                deltaHConvertData = convertData(deltaHResponseData, minResponse.get(5).toString(), startTime, averageResponse.get(5).toString());
                nConvertAverage = (float) (Double.parseDouble(averageResponse.get(1).toString()) - Double.parseDouble(minResponse.get(1).toString()));
                eConvertAverage = (float) (Double.parseDouble(averageResponse.get(2).toString()) - Double.parseDouble(minResponse.get(2).toString()));
                hConvertAverage = (float) (Double.parseDouble(averageResponse.get(3).toString()) - Double.parseDouble(minResponse.get(3).toString()));
                deltaConvertAverage = (float) (Double.parseDouble(averageResponse.get(4).toString()) - Double.parseDouble(minResponse.get(4).toString()));
                deltaHConvertAverage = (float) (Double.parseDouble(averageResponse.get(5).toString()) - Double.parseDouble(minResponse.get(5).toString()));
                LogUtil.e("getData执行结束", "*********");
                hasData = true;
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    /**
     * 处理接口返回数据
     *
     * @param responseData 接口返回的数据列表。内层列表中，索引0存储时间，索引1存储数据.
     * @param responseMin  接口返回值的最小值.
     * @return 转换后可用于画图的数据。
     */
    public List<PointValue> convertData(List<List<String>> responseData, String responseMin, String startTime, String responseAverage) {
        List<PointValue> convertData = new ArrayList<>();
        String time;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long axisXValue = null;
        Long axisX0 = null;
        float convertValue;
        double valueMin = Double.parseDouble(responseMin);

        DecimalFormat df = new DecimalFormat("#.00");//只保留小数点后两位，厘米级精度
        BigDecimal convertMin = new BigDecimal(df.format(valueMin));
        valueMin = Double.parseDouble(String.valueOf(convertMin));
        float convertAverage = (float) (Double.parseDouble(responseAverage) - valueMin);
        try {
            axisX0 = simpleDateFormat.parse(startTime).getTime() / 1000 / 60; //获取分钟级时间戳
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (List<String> data : responseData) {
            time = data.get(0);   //0是时间，1是数据
            convertValue = (float) (Double.parseDouble(data.get(1)) - valueMin);
            axisXValue = Long.parseLong(time) / 60;
            convertData.add(new PointValue((float) (axisXValue - axisX0), convertValue));
//            LogUtil.e("原始值，转换后数据",Double.parseDouble(data.get(1)) + "  " + convertValue);
        }
        return convertData;
    }

    /**
     * 判断转换后的数据有几段，并转换成线数据。
     *
     * @param values      转换后的数据
     */
    public void convertLines(List<PointValue> values) {
        if (values.size() == 1)
        {
            return;
        }
        float space = values.get(1).getX() - values.get(0).getX();
        float maxSpace = space * 2;
        float tempSpace;
        chartLines.clear();
        List<PointValue> tempLines = new ArrayList<>();
        tempLines.add(new PointValue(values.get(0).getX(), values.get(0).getY()));   //加入第一个数据
        for (int i = 1; i < values.size(); i++) {
            tempSpace = xConvertData.get(i).getX() - xConvertData.get(i - 1).getX();
            if (tempSpace > maxSpace) {
                chartLines.add(tempLines);
                tempLines = new ArrayList<>();  //如果用clear()方法清空，那么所有使用该列表的数据都空了。
            }
            space = tempSpace;
            maxSpace = space * 2;
            tempLines.add(new PointValue(values.get(i).getX(), values.get(i).getY()));
        }
        chartLines.add(tempLines);   //将最后一段数据加入

    }

    /**
     * 同步获取数据
     *
     * @param graphicType 图表数据类型
     * @param stationUUID 设备ID
     * @param startTime   开始时间
     * @param endTime     结束时间
     */
    public void getData(final String graphicType, final String stationUUID, final String startTime, final String endTime) {
        String time = DateUtil.getTimeInterval(spTime.getSelectedItem().toString());
        xChartName.setText("N");
        xChartTime.setText(time);
        xChartCoo.setText("WGS84坐标系|");

        yChartName.setText("E");
        yChartTime.setText(time);
        yChartCoo.setText("WGS84坐标系|");

        hChartName.setText("H");
        hChartTime.setText(time);
        hChartCoo.setText("WGS84坐标系|");
        Thread httpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                requestChartDataSync(graphicType, stationUUID, startTime, endTime);
            }
        });
        httpThread.start();
        try {
            httpThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据选中的监测点和时间重新获取数据并重绘图表
     */
    public void refresh()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String graphicType = "GNSSFilterInfo";
        String stationUUID = stationUUIDList.get(spDevice.getSelectedItemPosition());
        switch (spTime.getSelectedItemPosition()) {
            case 0:
                //最近1小时
                startTime = sdf.format(DateUtil.getHourBegin(1));
                endTime = sdf.format(DateUtil.getHourEnd());
//                getData(graphicType, stationUUID, startTime, endTime);
//                drawChart();
                doLoadingDialog();
                break;
            case 1:
                //最近6小时
                startTime = sdf.format(DateUtil.getHourBegin(6));
                endTime = sdf.format(DateUtil.getHourEnd());
//                getData(graphicType, stationUUID, startTime, endTime);
//                drawChart();
                doLoadingDialog();
                break;
            case 2:
                startTime = sdf.format(DateUtil.getHourBegin(12));
                endTime = sdf.format(DateUtil.getHourEnd());
//                getData(graphicType, stationUUID, startTime, endTime);
//                drawChart();
                doLoadingDialog();
                break;
            case 3:
                //本日
                startTime = sdf.format(DateUtil.getDayBegin());
                endTime = sdf.format(DateUtil.getDayEnd());
//                getData(graphicType, stationUUID, startTime, endTime);
//                drawChart();
                doLoadingDialog();
                break;
            case 4:
                //一周
                startTime = sdf.format(DateUtil.getBeginDayOfWeek());
                endTime = sdf.format(DateUtil.getEndDayOfWeek());
                LogUtil.e("starttime-week", startTime);
                LogUtil.e("endtime-week", endTime);
//                getData(graphicType, stationUUID, startTime, endTime);
//                drawChart();
                doLoadingDialog();
                break;
            case 5:
                //一月
                startTime = sdf.format(DateUtil.getBeginDayOfMonth());
                endTime = sdf.format(DateUtil.getEndDayOfMonth());
                LogUtil.e("starttime-month", startTime);
                LogUtil.e("endtime-month", endTime);
//                getData(graphicType, stationUUID, startTime, endTime);
//                drawChart();
                doLoadingDialog();
                break;
            case 6:
                //一年
                startTime = sdf.format(DateUtil.getBeginDayOfYear());
                endTime = sdf.format(DateUtil.getEndDayOfYear());
                LogUtil.e("starttime-year", startTime);
                LogUtil.e("endtime-year", endTime);
//                getData(graphicType, stationUUID, startTime, endTime);
//                drawChart();
                doLoadingDialog();
                break;
        }
    }

    public void downLoadExcel()
    {
        //获取URL
        LogUtil.e("导出报表开始时间",startTime);
        LogUtil.e("导出报表结束时间",endTime);
        FormBody body = new FormBody.Builder()
                .add("AccessToken", ApiConfig.getAccessToken())
                .add("SessionUUID", ApiConfig.getSessionUUID())
                .add("StationUUID",stationUUIDList.get(spDevice.getSelectedItemPosition()))
                .add("StartTime",startTime)
                .add("EndTime",endTime)
                .build();
        final Api api = Api.config(ApiConfig.GET_STATION_REPORT);
        api.postRequestFormBody(getActivity(), body, new ApiCallback() {
            @Override
            public void onSuccess(String res) {
                String url = api.parseJSONObject(res,"ReportFilePath");
                LogUtil.e("获取的下载·地址为",url);
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null && !"".equals(url))
                {
                    mainActivity.getDownloadBinder().startDownload(url);
                }else {
                    Toast.makeText(getActivity(),"获取URL有误，请稍后再试",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                LogUtil.e("downloadReport network failure",e.toString());
                Toast.makeText(getActivity(),"网络连接错误，请稍后再试",Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void drawChart()
    {
        if (hasData)
        {
            drawXYHChart(spTime.getSelectedItem().toString());
            drawDeltaChart(spTime.getSelectedItem().toString());
            drawHeartChart(spTime.getSelectedItem().toString());
        }
        dialog.dismiss();
    }

    public void test() {
        chartXLayout.post(new Runnable() {
            @Override
            public void run() {
                int height = chartXLayout.getHeight();
                int width = chartXLayout.getWidth();
                /*for (int i = 0; i < chartXLayout.getChildCount(); i++) {
                    LogUtil.e("child + width",chartXLayout.getChildAt(i).toString() + chartXLayout.getChildAt(i).getWidth());
                    width += chartXLayout.getChildAt(i).getWidth();
                }
                LogUtil.e("图表布局的width和height", width + "  " + height);

                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                chartXLayout.draw(canvas);*/
                xChart.setDrawingCacheEnabled(true);
                Bitmap bitmap = xChart.getDrawingCache();
                FileUtil.saveBitmap("test.jpg",bitmap,getContext());
            }
        });
    }

}