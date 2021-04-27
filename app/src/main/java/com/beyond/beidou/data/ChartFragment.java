package com.beyond.beidou.data;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
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
import com.beyond.beidou.entites.LoginResponse;
import com.beyond.beidou.login.StartActivity;
import com.beyond.beidou.util.DateUtil;
import com.beyond.beidou.util.FileUtil;
import com.beyond.beidou.util.LogUtil;
import com.beyond.beidou.util.LoginUtil;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
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
    private LineChartView xChart, yChart, HChart,deltaDChart,deltaHChart,heartChart;
    private TextView xChartName, yChartName, hChartName,deltaDChartName,deltaHChartName,heartChartName;
    private TextView xChartCoo, yChartCoo, hChartCoo,deltaDChartCoo,deltaHChartCoo,heartChartCoo;
    private TextView xChartTime, yChartTime, hChartTime,deltaDChartTime,deltaHChartTime,heartChartTime;
    private TextView mTitle;
    private TextView mDownLoadExcel;
    private float convertYMax;
    private float convertYMin;
    private float convertAverage;
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
        getData("GNSSFilterInfo", "6159529a-6bc3-4c73-84d1-e59f6f60ece6", "2021-03-09 00:00:00", "2021-03-09 23:59:59");
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        getData("GNSSFilterInfo", stationUUIDList.get(selectedDevicePosition), df.format(DateUtil.getDayBegin()), df.format(DateUtil.getDayEnd()));

        startTime = "2021-03-09 00:00:00";  //当前只有该天的数据，默认展示本日的数据
        endTime = "2021-03-09 23:59:59";
        drawXYHChart("本日");
        drawDeltaChart("本日");
        drawHeartChart("本日");
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
        HChart = view.findViewById(R.id.chart_H);
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

        mDownLoadExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downLoadExcel();
            }
        });
//        spDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                refresh();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        xChart.setOnTouchListener(touchListener);
        yChart.setOnTouchListener(touchListener);
        HChart.setOnTouchListener(touchListener);

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

    public List<PointValue> monitorDataWeek(List<PointValue> values) {
        List<PointValue> temp = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            temp.add(new PointValue(values.get(i).getX() * 7, values.get(i).getY()));
        }

        return temp;
    }

    public List<PointValue> monitorDataMonth(List<PointValue> values) {
        //模拟数据模块
        int size = values.size();
//        for (int i = 0; i < 11; i++) {
//            values.add(new PointValue(values.get(values.size() - 1).getX() + 1f, values.get(size-1).getY()));
//        }

        List<PointValue> temp = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.CHINA);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        Date date = calendar.getTime();
        calendar.add(Calendar.MONTH, -1);
        calendar.add(Calendar.DATE, -1);
        Date dateBefore = calendar.getTime();
        int days = DateUtil.calcDayOffset(dateBefore, date);
        for (int i = 0; i < values.size(); i++) {
//            Log.e("original", values.get(i).getX() + "");
//            Log.e("monthX", values.get(i).getX() * 30 + "");

            temp.add(new PointValue(values.get(i).getX() * days, values.get(i).getY()));
        }
//        temp.add(new PointValue(1440 * 30, values.get(size - 1).getY()));
        return temp;
    }

    public List<PointValue> monitorDataYear(List<PointValue> values) {
        //模拟数据模块
        int size = values.size();
//        for (int i = 0; i < 11; i++) {
//            values.add(new PointValue(values.get(values.size() - 1).getX() + 1f, values.get(size - 1).getY()));
//        }
        List<PointValue> temp = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.CHINA);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        Date date = calendar.getTime();
        calendar.add(Calendar.YEAR, -1);
        calendar.add(Calendar.DATE, -1);
        Date dateBefore = calendar.getTime();
        int days = DateUtil.calcDayOffset(dateBefore, date);

        for (int i = 0; i < values.size(); i++) {
            temp.add(new PointValue(values.get(i).getX() * days, values.get(i).getY()));

        }

        return temp;
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
        String time = DateUtil.getTimeInterval(selectedTime);
        xChartName.setText("N");
        xChartTime.setText(time);
        xChartCoo.setText("WGS84坐标系|");

        yChartName.setText("E");
        yChartTime.setText(time);
        yChartCoo.setText("WGS84坐标系|");

        hChartName.setText("H");
        hChartTime.setText(time);
        hChartCoo.setText("WGS84坐标系|");

        List<AxisValue> xAxisValues = setXAxisValues(selectedTime);
        List<AxisValue> yLabel = setAxisYLabel(minResponse.get(1).toString(), xConvertData);
        convertLines(xConvertData);
        setChart(xChart, xConvertData, xAxisValues, yLabel);

        xAxisValues = setXAxisValues(selectedTime);
        yLabel = setAxisYLabel(minResponse.get(2).toString(), yConvertData);
        convertLines(yConvertData);
        setChart(yChart, yConvertData, xAxisValues, yLabel);

        xAxisValues = setXAxisValues(selectedTime);
        yLabel = setAxisYLabel(minResponse.get(3).toString(), hConvertData);
        convertLines(hConvertData);
        setChart(HChart, hConvertData, xAxisValues, yLabel);

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
        setChart(deltaDChart, deltaDConvertData, xAxisValues, yLabel);

        xAxisValues = setXAxisValues(selectedTime);
        yLabel = setAxisYLabel(minResponse.get(5).toString(), deltaHConvertData);
        convertLines(deltaHConvertData);
        setChart(deltaHChart, deltaHConvertData, xAxisValues, yLabel);
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

        Viewport viewport = chartView.getMaximumViewport();
        viewport.top = viewport.top + 0.02f;
        viewport.bottom = viewport.bottom - 0.02f;
        viewport.left = viewport.left - 0.02f;
        viewport.right = viewport.right + 0.02f;
        chartView.setMaximumViewport(viewport);

        chartView.setZoomLevel(0f, 0f, 0f);
    }

    public void convertHeartChartData(List<List<Object>> contentList, List<AxisValue> xAxisValues, List<AxisValue> yAxisValues, List<PointValue> pointValues) {
//        Log.wtf("WTF", "XMax: " + maxResponse.get(1).toString()+ " XMin:  "+ minResponse.get(1).toString() + " YMax:" + maxResponse.get(2).toString() + " YMin" + minResponse.get(2).toString());
        double convertYMin, convertYMax, convertXMin, convertXMax;
        DecimalFormat df = new DecimalFormat("#.00");
        String space = "0.01";
        BigDecimal bSpace = new BigDecimal(space);
        BigDecimal bResponseYMin = new BigDecimal(df.format(Double.parseDouble(minResponse.get(2).toString())));
        BigDecimal bResponseYMax = new BigDecimal(df.format(Double.parseDouble(maxResponse.get(2).toString())));

        bResponseYMin = bResponseYMin.subtract(new BigDecimal("0.02"));
        bResponseYMax = bResponseYMax.add(new BigDecimal("0.02"));

        convertYMin = Double.parseDouble(String.valueOf(bResponseYMin));
        convertYMax = Double.parseDouble(String.valueOf(bResponseYMax));
        double tempDouble = Double.parseDouble(String.valueOf(bResponseYMin));
        String tempString = String.valueOf(convertYMin);
        BigDecimal strYMin = new BigDecimal(df.format(Double.parseDouble(minResponse.get(2).toString())));
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

        BigDecimal bResponseXMin = new BigDecimal(df.format(Double.parseDouble(minResponse.get(1).toString())));
        BigDecimal bResponseXMax = new BigDecimal(df.format(Double.parseDouble(maxResponse.get(1).toString())));

        bResponseXMin = bResponseXMin.subtract(new BigDecimal("0.02"));
        bResponseXMax = bResponseXMax.add(new BigDecimal("0.02"));

        convertXMin = Double.parseDouble(String.valueOf(bResponseXMin));
        convertXMax = Double.parseDouble(String.valueOf(bResponseXMax));
        tempDouble = Double.parseDouble(String.valueOf(bResponseXMin));
        tempString = String.valueOf(convertXMin);
        BigDecimal strXMin = new BigDecimal(df.format(Double.parseDouble(minResponse.get(1).toString())));
        strXMin = strXMin.subtract(new BigDecimal("0.02"));
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
        BigDecimal minuendX = new BigDecimal(df.format(Double.parseDouble(minResponse.get(1).toString())));
        minuendX = minuendX.subtract(new BigDecimal("0.02"));
        BigDecimal minuendY = new BigDecimal(df.format(Double.parseDouble(minResponse.get(2).toString())));
        minuendY = minuendY.subtract(new BigDecimal("0.02"));
        for (List<Object> content : contentList) {
            tempX = Double.parseDouble(content.get(1).toString()) - Double.parseDouble(minuendX.toString());
            tempY = Double.parseDouble(content.get(2).toString()) - Double.parseDouble(minuendY.toString());
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

        while (chartValue <= convertYMax) {
            b_ymin = b_ymin.add(b_space);
            s_yMin = s_yMin.add(b_space);
            tempString = String.valueOf(s_yMin);
            chartValue = Float.parseFloat(String.valueOf(b_ymin));
            //LogUtil.e("value lable", String.valueOf(chartValue) + "  " + tempString);
            AxisValue axisValue = new AxisValue(chartValue);
            axisValue.setLabel(tempString);
            axisValues.add(axisValue);
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
     * @param values      图表数据
     * @param xAxisValues X轴标签（时间）
     * @param yAxisValues Y轴标签
     */
    public void setChart(LineChartView chartView, List<PointValue> values, List<AxisValue> xAxisValues, List<AxisValue> yAxisValues) {
        String selectedTime = spTime.getSelectedItem().toString();
        List<Line> lines = new ArrayList<>();
        if ("本日".equals(spTime.getSelectedItem().toString()))   //测试，图表全部画完之后不再需要判断。
        {
            for (int i = 0; i < chartLines.size(); i++) {
                Line line = new Line(chartLines.get(i)).setColor(Color.parseColor("#2196F3")).setCubic(false).setPointRadius(0).setStrokeWidth(2);
                lines.add(line);
            }
        } else {
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

        if("一年".equals(spTime.getSelectedItem().toString())){
            maxWindow.right = maxWindow.right + (float) (xConvertData.size() * 365 / 24);
        }else if("一月".equals(spTime.getSelectedItem().toString())){
            maxWindow.right = maxWindow.right + (float) (xConvertData.size() * 30 / 24);
        }else if("一周".equals(spTime.getSelectedItem().toString())){
            maxWindow.right = maxWindow.right + (float) (xConvertData.size() * 7/ 24);
        }else{
            maxWindow.right = maxWindow.right + (float) (xConvertData.size() / 24);
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
                LogUtil.e("getData执行结束", "*********");
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
        double valueMin = Double.parseDouble(responseMin);
        float convertValue;

        DecimalFormat df = new DecimalFormat("#.00");//只保留小数点后两位，厘米级精度
        BigDecimal convertMin = new BigDecimal(df.format(valueMin));
        valueMin = Double.parseDouble(String.valueOf(convertMin));
        convertAverage = (float) (Double.parseDouble(responseAverage) - valueMin);
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
        }
        return convertData;
    }

    /**
     * 判断转换后的数据有几段，并转换成线数据。
     *
     * @param values      转换后的数据
     */
    public void convertLines(List<PointValue> values) {
        float space = xConvertData.get(1).getX() - xConvertData.get(0).getX();
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
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String graphicType = "GNSSFilterInfo";
        String stationUUID = "6159529a-6bc3-4c73-84d1-e59f6f60ece6";
//        String stationUUID = stationUUIDList.get(spDevice.getSelectedItemPosition());

        switch (spTime.getSelectedItemPosition()) {
            case 0:
                //最近1小时
                //暂时只有3.9号的数据，之后直接把"2021-03-09 "去掉即可。
                startTime = "2021-03-09 " + sdf.format(DateUtil.getHourBegin(1));
                endTime = "2021-03-09 " + sdf.format(DateUtil.getHourEnd());
                getData(graphicType, stationUUID, startTime, endTime);
                drawXYHChart(spTime.getSelectedItem().toString());
                drawDeltaChart(spTime.getSelectedItem().toString());
                drawHeartChart(spTime.getSelectedItem().toString());
                break;
            case 1:
                //最近6小时
                startTime = "2021-03-09 " + sdf.format(DateUtil.getHourBegin(6));
                endTime = "2021-03-09 " + sdf.format(DateUtil.getHourEnd());
                getData(graphicType, stationUUID, startTime, endTime);
                drawXYHChart(spTime.getSelectedItem().toString());
                drawDeltaChart(spTime.getSelectedItem().toString());
                drawHeartChart(spTime.getSelectedItem().toString());
                break;
            case 2:
                //最近12小时
                if (DateUtil.getNowHour() < 12)
                {
                    //12点之前,因为当前只有9号的数据
                    startTime = "2021-03-08 " + sdf.format(DateUtil.getHourBegin(12));
                }
                else {
                    //12点之后
                    startTime = "2021-03-09 " + sdf.format(DateUtil.getHourBegin(12));
                }
                endTime = "2021-03-09 " + sdf.format(DateUtil.getHourEnd());
                getData(graphicType, stationUUID, startTime, endTime);
                drawXYHChart(spTime.getSelectedItem().toString());
                drawDeltaChart(spTime.getSelectedItem().toString());
                drawHeartChart(spTime.getSelectedItem().toString());
                break;
            case 3:
                //本日
                startTime = "2021-03-09 " + sdf.format(DateUtil.getDayBegin());
                endTime = "2021-03-09 " + sdf.format(DateUtil.getDayEnd());
                getData(graphicType, stationUUID, startTime, endTime);
                drawXYHChart(spTime.getSelectedItem().toString());
                drawDeltaChart(spTime.getSelectedItem().toString());
                drawHeartChart(spTime.getSelectedItem().toString());
                break;
            case 4:
                //一周
                //模拟数据
                startTime = "2021-03-09 00:00:00";
                endTime = "2021-03-09 23:59:59";
                getData(graphicType, stationUUID, startTime, endTime);

                //模拟数据模块
                xConvertData = monitorDataWeek(xConvertData);
                yConvertData = monitorDataWeek(yConvertData);
                hConvertData = monitorDataWeek(hConvertData);
                deltaDConvertData = monitorDataWeek(deltaDConvertData);
                deltaHConvertData = monitorDataWeek(deltaHConvertData);
                drawXYHChart(spTime.getSelectedItem().toString());
                drawDeltaChart(spTime.getSelectedItem().toString());
                drawHeartChart(spTime.getSelectedItem().toString());
                break;
            case 5:
                //一月
                //模拟数据
                startTime = "2021-03-09 00:00:00";
                endTime = "2021-03-09 23:59:59";
                getData(graphicType, stationUUID, startTime, endTime);
                //模拟数据模块
                xConvertData = monitorDataMonth(xConvertData);
                yConvertData = monitorDataMonth(yConvertData);
                hConvertData = monitorDataMonth(hConvertData);
                deltaDConvertData = monitorDataMonth(deltaDConvertData);
                deltaHConvertData = monitorDataMonth(deltaHConvertData);
                drawXYHChart(spTime.getSelectedItem().toString());
                drawDeltaChart(spTime.getSelectedItem().toString());
                drawHeartChart(spTime.getSelectedItem().toString());
                break;
            case 6:
                //一年
                //模拟数据
                startTime = "2021-03-09 00:00:00";
                endTime = "2021-03-09 23:59:59";
                getData(graphicType, stationUUID, startTime, endTime);

                //模拟数据模块
                xConvertData = monitorDataYear(xConvertData);
                yConvertData = monitorDataYear(yConvertData);
                hConvertData = monitorDataYear(hConvertData);
                deltaDConvertData = monitorDataYear(deltaDConvertData);
                deltaHConvertData = monitorDataYear(deltaHConvertData);
                drawXYHChart(spTime.getSelectedItem().toString());
                drawDeltaChart(spTime.getSelectedItem().toString());
                drawHeartChart(spTime.getSelectedItem().toString());
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
                .add("StationUUID","6159529a-6bc3-4c73-84d1-e59f6f60ece6")//当前只有该点有数据
//                .add("StationUUID",stationUUIDList.get(selectedDevicePosition))
                .add("StartTime",startTime)
                .add("EndTime",endTime)
//                .add("StartTime","2021-03-09 00:00:00")
//                .add("EndTime","2021-03-09 23:10:00")
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