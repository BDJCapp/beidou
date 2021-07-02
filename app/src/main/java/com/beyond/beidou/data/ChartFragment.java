package com.beyond.beidou.data;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MyDialog;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.MySpinner;
import com.beyond.beidou.R;
import com.beyond.beidou.api.Api;
import com.beyond.beidou.api.ApiCallback;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.entites.GNSSFilterInfoResponse;
import com.beyond.beidou.util.DateUtil;
import com.beyond.beidou.util.LogUtil;
import com.beyond.beidou.util.LoginUtil;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.*;
import lecho.lib.hellocharts.view.LineChartView;
import okhttp3.FormBody;

public class ChartFragment extends BaseFragment implements View.OnClickListener {
    private Spinner mDeviceSp;
    private MySpinner mTimeSp;
    private LineChartView mNChart, mEChart, mHChart, mDeltaDChart, mDeltaHChart, mHeartChart;
    private TextView mNChartNameTv, mEChartNameTv, mHChartNameTv, mDeltaDChartNameTv, mDeltaHChartNameTv, mHeartChartNameTv;
    private TextView mNChartCooTv, mEChartCooTv, mHChartCooTv, mDeltaDChartCooTv, mDeltaHChartCooTv, mHeartChartCooTv;
    private TextView mNChartTimeTv, mEChartTimeTv, mHChartTimeTv, mDeltaDChartTimeTv, mDeltaHChartTimeTv, mHeartChartTimeTv;
    private TextView mTitleTv;
    private TextView mDownLoadExcelTv;
    private float mConvertYMax;
    private float mConvertYMin;
    private float mNConvertAvg, mEConvertAvg, mHConvertAvg, mDeltaDConvertAvg, mDeltaHConvertAvg;
    private List<Object> mMaxResponse = new ArrayList<>();
    private List<Object> mMinResponse = new ArrayList<>();
    private List<Object> mAvgResponse = new ArrayList<>();
    private ScrollView mChartsSv;
    private ImageView mBackIv;
    private List<PointValue> mNConvertData = new ArrayList<>();
    private List<PointValue> mEConvertData = new ArrayList<>();
    private List<PointValue> mHConvertData = new ArrayList<>();
    private List<PointValue> mDeltaDConvertData = new ArrayList<>();
    private List<PointValue> mDeltaHConvertData = new ArrayList<>();
    private List<List<Object>> mContentResponse = new ArrayList<>();
    private List<List<PointValue>> mChartLines = new ArrayList<>();
    private int mInterval, mMaxInterval;
    private boolean isFirstTimeSelectTime = true;
    private ArrayList<String> mStationUUIDList = new ArrayList<>();
    private String mStartTime = null;
    private String mEndTime = null;
    private String mDeltaTime = null;
    private boolean hasData = true;
    private Map<String, Integer> mTitleIndex = new HashMap<>();
    private MyDialog mTimePickerDlg;
    private int mLastSelectedTimePosition;
    private static final int LOADING = 1;
    private static final int GET_DATA_SUCCESS = 200;
    private ZLoadingDialog mLoadingDlg;
    public Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case LOADING:
                    getData("GNSSFilterInfo", mStationUUIDList.get(mDeviceSp.getSelectedItemPosition()), mStartTime, mEndTime, mDeltaTime);
                    break;
                case GET_DATA_SUCCESS:
                    drawChart();
                    break;
            }
        }
    };


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mLoadingDlg = new ZLoadingDialog(context);
    }

    private void doLoadingDialog() {
        mLoadingDlg.setLoadingBuilder(Z_TYPE.ROTATE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK)//颜色
                .setHintText("Loading...")
                .setCanceledOnTouchOutside(false)
                .show();
        handler.sendEmptyMessageDelayed(LOADING, 0);
    }

    public static ChartFragment newInstance(String projectName, ArrayList<String> stationNameList, int position, ArrayList<String> stationUUIDList) {
        ChartFragment chartFragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putString("projectName", projectName);
        args.putStringArrayList("stationNameList", stationNameList);
        args.putInt("position", position);
        args.putStringArrayList("stationUUIDList", stationUUIDList);
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
        mStartTime = df.format(DateUtil.getDayBegin());
        mEndTime = df.format(DateUtil.getDayEnd());
        mDeltaTime = "60";
        doLoadingDialog();
        return view;
    }

    public void initView(final View view) {
        mTitleTv = view.findViewById(R.id.tv_title);
        mDeviceSp = view.findViewById(R.id.sp_device);
        mTimeSp = view.findViewById(R.id.sp_chart_time);
        mDownLoadExcelTv = view.findViewById(R.id.tv_load_excel);
        mTimePickerDlg = new MyDialog(getActivity());
        if (getArguments() != null) {
            mTitleTv.setText(getArguments().getString("projectName"));
            ArrayAdapter<String> deviceAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getArguments().getStringArrayList("stationNameList"));
            deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            mDeviceSp.setAdapter(deviceAdapter);
            int selectedDevicePosition = getArguments().getInt("position");
            mDeviceSp.setSelection(selectedDevicePosition, true);
            mStationUUIDList = getArguments().getStringArrayList("stationUUIDList");
        }
//        String[] times = new String[]{"最近1小时", "最近6小时", "最近12小时", "本日", "一周", "一月", "一年"};
        final String[] times = new String[]{"最近1小时", "最近6小时", "最近12小时", "本日", "一周", "一月", "一年", "自定义时间"};
        final ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, times);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mTimeSp.setAdapter(timeAdapter);
        mTimeSp.setSelection(3);

        mLastSelectedTimePosition = 3;

        mNChart = view.findViewById(R.id.chart_X);
        mEChart = view.findViewById(R.id.chart_Y);
        mHChart = view.findViewById(R.id.chart_H);
        mDeltaDChart = view.findViewById(R.id.chart_DeltaD);
        mDeltaHChart = view.findViewById(R.id.chart_DeltaH);
        mHeartChart = view.findViewById(R.id.chart_Heart);

        mNChartNameTv = view.findViewById(R.id.tv_chartNameX);
        mNChartCooTv = view.findViewById(R.id.tv_coordinateSystemX);
        mNChartTimeTv = view.findViewById(R.id.tv_chartTimeX);

        mEChartNameTv = view.findViewById(R.id.tv_chartNameY);
        mEChartCooTv = view.findViewById(R.id.tv_coordinateSystemY);
        mEChartTimeTv = view.findViewById(R.id.tv_chartTimeY);

        mHChartNameTv = view.findViewById(R.id.tv_chartNameH);
        mHChartCooTv = view.findViewById(R.id.tv_coordinateSystemH);
        mHChartTimeTv = view.findViewById(R.id.tv_chartTimeH);

        mDeltaDChartNameTv = view.findViewById(R.id.tv_DeltaDChartName);
        mDeltaDChartCooTv = view.findViewById(R.id.tv_coordinateSystemDeltaD);
        mDeltaDChartTimeTv = view.findViewById(R.id.tv_chartTimeDeltaD);

        mDeltaHChartNameTv = view.findViewById(R.id.tv_chartNameDeltaH);
        mDeltaHChartCooTv = view.findViewById(R.id.tv_coordinateSystemDeltaH);
        mDeltaHChartTimeTv = view.findViewById(R.id.tv_chartTimeDeltaH);

        mHeartChartNameTv = view.findViewById(R.id.tv_chartNameHeart);
        mHeartChartCooTv = view.findViewById(R.id.tv_coordinateSystemHeart);
        mHeartChartTimeTv = view.findViewById(R.id.tv_chartTimeHeart);

        mBackIv = view.findViewById(R.id.img_chart_back);
        mBackIv.setOnClickListener(this);

        mChartsSv = view.findViewById(R.id.sv_charts);

        mTimeSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                if (LoginUtil.isNetworkUsable(getActivity()))
                {
                    if (isFirstTimeSelectTime) {
                        isFirstTimeSelectTime = false;
                        return;
                    }


                    if (parent.getSelectedItem().toString().equals("自定义时间")) {
                        mTimePickerDlg.setOnClickBottomListener(new MyDialog.OnClickBottomListener() {
                            @Override
                            public void onPositiveClick() {
                                mStartTime = mTimePickerDlg.getStartTime();
                                mEndTime = mTimePickerDlg.getEndTime();
                                if (mStartTime.equals("") | mEndTime.equals("") | DateUtil.calcHourOffset(mStartTime, mEndTime) <= 0)
                                {
                                    showToast("请选择正确的时间区间！");
                                }
                                else if (DateUtil.calcHourOffset(mStartTime, mEndTime) >= 8784)
                                {
                                    showToast("最大查询间隔为两年，请重新选择");
                                }
                                else {
                                    mTimePickerDlg.dismiss();
                                    refresh();
                                    mLastSelectedTimePosition = position;
                                }
                            }

                            @Override
                            public void onNegativeClick() {
                                mTimePickerDlg.dismiss();
                                if (mLastSelectedTimePosition != 7)
                                {
                                    mTimeSp.setSelection(mLastSelectedTimePosition);
                                }
                            }
                        });
                        mTimePickerDlg.setOnClickTextViewListener(new MyDialog.OnClickTextViewListener() {
                            @Override
                            public void onStartTimeClick(View v) {
                                timePicker((TextView) v);
                            }

                            @Override
                            public void onEndTimeClick(View v) {
                                timePicker((TextView) v);
                            }
                        });
                        mTimePickerDlg.show();
                    }else {
                        refresh();
                        mLastSelectedTimePosition = position;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                LogUtil.e("NothingSelected","1111");
            }
        });

        mDeviceSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (LoginUtil.isNetworkUsable(getActivity()))
                {
                    refresh();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mDownLoadExcelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LoginUtil.isNetworkUsable(getActivity()))
                {
                    downLoadExcel();
                }

            }
        });

        mNChart.setOnTouchListener(touchListener);
        mEChart.setOnTouchListener(touchListener);
        mHChart.setOnTouchListener(touchListener);
        mDeltaHChart.setOnTouchListener(touchListener);
        mDeltaDChart.setOnTouchListener(touchListener);
        mHeartChart.setOnTouchListener(touchListener);




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
                        mChartsSv.requestDisallowInterceptTouchEvent(false);
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
//        String time = DateUtil.getTimeInterval(spTime.getSelectedItem().toString());
        String time = mStartTime + "~" + mEndTime;
        mNChartNameTv.setText("N");
        mNChartTimeTv.setText(time);
        mNChartCooTv.setText("WGS84坐标系|");

        mEChartNameTv.setText("E");
        mEChartTimeTv.setText(time);
        mEChartCooTv.setText("WGS84坐标系|");

        mHChartNameTv.setText("H");
        mHChartTimeTv.setText(time);
        mHChartCooTv.setText("WGS84坐标系|");
        List<AxisValue> xAxisValues = setXAxisValues(selectedTime);
        List<AxisValue> yLabel = setAxisYLabel(mMinResponse.get(mTitleIndex.get("GNSSFilterInfoN")).toString(), mNConvertData);
        convertLines(mNConvertData);
        setChart(mNChart, xAxisValues, yLabel, mNConvertAvg);

        xAxisValues = setXAxisValues(selectedTime);
        yLabel = setAxisYLabel(mMinResponse.get(mTitleIndex.get("GNSSFilterInfoE")).toString(), mEConvertData);
        convertLines(mEConvertData);
        setChart(mEChart, xAxisValues, yLabel, mEConvertAvg);

        xAxisValues = setXAxisValues(selectedTime);
        yLabel = setAxisYLabel(mMinResponse.get(mTitleIndex.get("GNSSFilterInfoH")).toString(), mHConvertData);
        convertLines(mHConvertData);
        setChart(mHChart, xAxisValues, yLabel, mHConvertAvg);

    }


    /**
     * 绘制位移图表
     */
    public void drawDeltaChart(String selectedTime) {
//        String time = DateUtil.getTimeInterval(selectedTime);
        String time = mStartTime + "~" + mEndTime;
        mDeltaDChartNameTv.setText("水平位移图");
        mDeltaDChartTimeTv.setText(time);
        mDeltaDChartCooTv.setText("WGS84坐标系|");

        mDeltaHChartNameTv.setText("垂直位移图");
        mDeltaHChartTimeTv.setText(time);
        mDeltaHChartCooTv.setText("WGS84坐标系|");

        List<AxisValue> xAxisValues = setXAxisValues(selectedTime);
        List<AxisValue> yLabel = setAxisYLabel(mMinResponse.get(mTitleIndex.get("GNSSFilterInfoDeltaD")).toString(), mDeltaDConvertData);
        convertLines(mDeltaDConvertData);
        setChart(mDeltaDChart, xAxisValues, yLabel, mDeltaDConvertAvg);

        xAxisValues = setXAxisValues(selectedTime);
        yLabel = setAxisYLabel(mMinResponse.get(mTitleIndex.get("GNSSFilterInfoDeltaH")).toString(), mDeltaHConvertData);
        convertLines(mDeltaHConvertData);
        setChart(mDeltaHChart, xAxisValues, yLabel, mDeltaHConvertAvg);
    }

    public void drawHeartChart(String selectedTime) {
//        String time = DateUtil.getTimeInterval(selectedTime);
        String time = mStartTime + "~" + mEndTime;
        mHeartChartNameTv.setText("心型图");
        mHeartChartTimeTv.setText(time);
        mHeartChartCooTv.setText("WGS84坐标系|");

        List<AxisValue> nAxisValues = new ArrayList<>();
        List<AxisValue> eAxisValues = new ArrayList<>();
        List<PointValue> pointValues = new ArrayList<>();

        convertHeartChartData(mContentResponse, nAxisValues, eAxisValues, pointValues);
        setHeartChart(mHeartChart, pointValues, nAxisValues, eAxisValues);

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
        chartView.setMaxZoom(5f);
        chartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        chartView.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        mHeartChart.setVisibility(View.VISIBLE);

        Viewport viewport = chartView.getMaximumViewport();
        viewport.top = viewport.top + 0.02f;
        viewport.bottom = viewport.bottom - 0.02f;
        viewport.left = viewport.left - 0.02f;
        viewport.right = viewport.right + 0.02f;
        chartView.setMaximumViewport(viewport);
        chartView.setCurrentViewport(viewport);


    }

    public void convertHeartChartData(List<List<Object>> contentList, List<AxisValue> xAxisValues, List<AxisValue> yAxisValues, List<PointValue> pointValues) {
        int nIndex = mTitleIndex.get("GNSSFilterInfoN");
        int eIndex = mTitleIndex.get("GNSSFilterInfoE");
        double convertEMin, convertEMax, convertNMin, convertNMax;
        DecimalFormat df = new DecimalFormat("#.00");
        String space = "0.01";
        BigDecimal bSpace = new BigDecimal(space);
        BigDecimal bResponseEmin = new BigDecimal(df.format(Double.parseDouble(mMinResponse.get(eIndex).toString())));
        BigDecimal bResponseEMax = new BigDecimal(df.format(Double.parseDouble(mMaxResponse.get(eIndex).toString())));

        bResponseEmin = bResponseEmin.subtract(new BigDecimal("0.02"));
        bResponseEMax = bResponseEMax.add(new BigDecimal("0.02"));

        convertEMin = Double.parseDouble(String.valueOf(bResponseEmin));
        convertEMax = Double.parseDouble(String.valueOf(bResponseEMax));
        double tempDouble = Double.parseDouble(String.valueOf(bResponseEmin));
        String tempString = String.valueOf(convertEMin);
        BigDecimal strEMin = new BigDecimal(df.format(Double.parseDouble(mMinResponse.get(eIndex).toString())));
        strEMin = strEMin.subtract(new BigDecimal("0.02"));
        while (tempDouble <= convertEMax) {
            AxisValue axisValue = new AxisValue(Float.parseFloat(df.format(tempDouble - convertEMin)));
            axisValue.setLabel(tempString);
            xAxisValues.add(axisValue);
            bResponseEmin = bResponseEmin.add(bSpace);
            strEMin = strEMin.add(bSpace);
            tempString = String.valueOf(strEMin);
            tempDouble = Double.parseDouble(String.valueOf(bResponseEmin));
        }

        BigDecimal bResponseNMin = new BigDecimal(df.format(Double.parseDouble(mMinResponse.get(nIndex).toString())));
        BigDecimal bResponseNMax = new BigDecimal(df.format(Double.parseDouble(mMaxResponse.get(nIndex).toString())));

        bResponseNMin = bResponseNMin.subtract(new BigDecimal("0.02"));
        bResponseNMax = bResponseNMax.add(new BigDecimal("0.02"));

        convertNMin = Double.parseDouble(String.valueOf(bResponseNMin));
        convertNMax = Double.parseDouble(String.valueOf(bResponseNMax));
        tempDouble = Double.parseDouble(String.valueOf(bResponseNMin));
        tempString = String.valueOf(convertNMin);
        BigDecimal strNMin = new BigDecimal(df.format(Double.parseDouble(mMinResponse.get(nIndex).toString())));
        strNMin = strNMin.subtract(new BigDecimal("0.02"));
        while (tempDouble <= convertNMax) {
            AxisValue axisValue = new AxisValue(Float.parseFloat(df.format(tempDouble - convertNMin)));
            axisValue.setLabel(tempString);
            yAxisValues.add(axisValue);
            bResponseNMin = bResponseNMin.add(bSpace);
            strNMin = strNMin.add(bSpace);
            tempString = String.valueOf(strNMin);
            tempDouble = Double.parseDouble(String.valueOf(bResponseNMin));
        }


        double tempE, tempN;
        BigDecimal minuendN1 = new BigDecimal(df.format(Double.parseDouble(mMinResponse.get(nIndex).toString())));
        minuendN1 = minuendN1.subtract(new BigDecimal("0.02"));
        BigDecimal minuendE1 = new BigDecimal(df.format(Double.parseDouble(mMinResponse.get(eIndex).toString())));
        minuendE1 = minuendE1.subtract(new BigDecimal("0.02"));
        for (List<Object> content : contentList) {
            tempN = Double.parseDouble(content.get(nIndex).toString()) - Double.parseDouble(minuendN1.toString());
            tempE = Double.parseDouble(content.get(eIndex).toString()) - Double.parseDouble(minuendE1.toString());
            pointValues.add(new PointValue(Float.parseFloat(String.valueOf(tempE)), Float.parseFloat(String.valueOf(tempN))));
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

//        LogUtil.e("setYLable valueYMax",valueYMax + "");
//        LogUtil.e("setYLable valueYMin",valueYMin + "");
//        LogUtil.e("setYLable 传入的最小值VS返回的最小值",yMin + "" + minResponse.get(1));

        String space = "0.01";     //每格大小为0.01m
        mConvertYMax = valueYMax + 0.1f;
        mConvertYMin = valueYMin - 0.1f;

        //保证convertYmin和yMin是真实值。这样即使转成只有两位小数，也是对应的。
        DecimalFormat df = new DecimalFormat("#.00");//只保留小数点后两位，厘米级精度
        BigDecimal b_ymin = new BigDecimal(df.format(mConvertYMin));
        BigDecimal tempYmin = new BigDecimal(df.format(Double.parseDouble(yMin)));
        BigDecimal b_space = new BigDecimal(space);
        chartValue = mConvertYMin;
        tempYmin = tempYmin.subtract(new BigDecimal("0.1"));
        BigDecimal s_yMin = new BigDecimal(df.format(tempYmin));  //传入格式化后的最小值

//        LogUtil.e("convertYMin + tempYmin",convertYMin + "  " + tempYmin);

        while (chartValue <= mConvertYMax) {
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
                xAxisValues = DateUtil.getHourXAxisValue(1);
                break;
            case "最近6小时":
                xAxisValues = DateUtil.getHourXAxisValue(6);
                break;
            case "最近12小时":
                xAxisValues = DateUtil.getHourXAxisValue(12);
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
            case "自定义时间":
                xAxisValues = DateUtil.getCustomNGT7DayAxisValue(mStartTime, mEndTime);
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
    public void setChart(LineChartView chartView, List<AxisValue> xAxisValues, List<AxisValue> yAxisValues, float convertAverage) {
        String selectedTime = mTimeSp.getSelectedItem().toString();
        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < mChartLines.size(); i++) {
            Line line = new Line(mChartLines.get(i)).setColor(Color.parseColor("#2196F3")).setCubic(false).setPointRadius(0).setStrokeWidth(2);
            lines.add(line);
        }

        //实现自定义竖向分割线
//        List<PointValue> leftValue = new ArrayList<>();
//        leftValue.add(new PointValue(100,-0.1f));
//        leftValue.add(new PointValue(100,0.1f));
//        Line leftLine = new Line(leftValue);
//        leftLine.setColor(R.color.main_red);
//        List<PointValue> rightValue = new ArrayList<>();
//        rightValue.add(new PointValue(200,-0.1f));
//        rightValue.add(new PointValue(200,0.1f));
//        Line rightLine = new Line(rightValue);
//        rightLine.setColor(R.color.main_red);
//        lines.add(leftLine);
//        lines.add(rightLine);
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

        if ("一周".equals(selectedTime) || "自定义时间".equals(selectedTime)) {
            axisX.setMaxLabelChars(8);
        } else if ("一月".equals(selectedTime)) {
            axisX.setMaxLabelChars(10);
        } else if ("一年".equals(selectedTime)) {
            axisX.setMaxLabelChars(12);
        }
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
        maxWindow.bottom = mConvertYMin;
        maxWindow.top = mConvertYMax;

        //设置当前窗口，将每格长度大约设置成物理上的1cm
        Viewport currentWindow = new Viewport(chartView.getMaximumViewport());
        //经过计算：1dp = 0.015875cm；600dp = 9.525cm，所以设置当前窗口显示9个刻度即可保证一格为1cm
        currentWindow.bottom = convertAverage - 0.05f;
        currentWindow.top = convertAverage + 0.04f;
//        LogUtil.e("currentWindow", "bottom: " + currentWindow.bottom + " , top: " + currentWindow.top);
        switch (mTimeSp.getSelectedItem().toString()) {
            case "最近1小时":
                maxWindow.right = 60 + (float) (60 / 24);
                maxWindow.left = 0f;
                break;
            case "最近6小时":
                maxWindow.right = 360 + (float) (360 / 24);
                maxWindow.left = 0f;
                break;
            case "最近12小时":
                maxWindow.right = 720 + (float) (720 / 24);
                maxWindow.left = 0f;
                break;
            case "本日":
                maxWindow.right = 1440 + (float) (1440 / 24);
                maxWindow.left = 0f;
                break;
            case "一周":
                maxWindow.right = 1440 * 7 + (float) (1440 * 7 / 24);
                maxWindow.left = 0f;
                break;
            case "一月":
                maxWindow.right = 1440 * 30 + (float) (1440 * 30 / 24);
                maxWindow.left = 0f;
                break;
            case "一年":
                maxWindow.right = 1440 * 365 + (float) (1440 * 365 / 24);
                maxWindow.left = 0f;
                break;
            case "自定义时间":
                int hourOffSet = DateUtil.calcHourOffset(mStartTime, DateUtil.getLabelEndTime());
                maxWindow.right = 60 * hourOffSet + (float)(60 * hourOffSet / 24);
                if(hourOffSet > 7 * 24){
                    maxWindow.left = DateUtil.getOffSet();
                }else{
                    maxWindow.left = 0f;
                }
        }

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

    public void requestChartDataSync(final String graphicType, String stationUUID, final String startTime, String endTime, String deltaTime) {
        FormBody getChartDataBody = new FormBody.Builder()
                .add("AccessToken", ApiConfig.getAccessToken())
                .add("SessionUUID", ApiConfig.getSessionUUID())
                .add("StationUUID", stationUUID)
                .add("GraphicType", graphicType)
                .add("StartTime", startTime)
                .add("EndTime", endTime)
                .add("DeltaTime", deltaTime)
                .build();
        final SimpleDateFormat sdfTwo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        LogUtil.e("请求点数据开始时间", sdfTwo.format(System.currentTimeMillis()));
        Api.config(ApiConfig.GET_GRAPHIC_DATA).postRequestFormBodySync(getActivity(), getChartDataBody, new ApiCallback() {
            @Override
            public void onSuccess(String res) {
                LogUtil.e("请求点数据结束时间", sdfTwo.format(System.currentTimeMillis()));
                LogUtil.e("请求数据的返回值",res);
                Gson gson = new Gson();
                GNSSFilterInfoResponse gnssFilterInfoResponse = gson.fromJson(res, GNSSFilterInfoResponse.class);
                mContentResponse = gnssFilterInfoResponse.getContent();
                if (mContentResponse.size() == 0) {
                    hasData = false;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mNChart.setVisibility(View.GONE);
                            mEChart.setVisibility(View.GONE);
                            mHChart.setVisibility(View.GONE);
                            mDeltaDChart.setVisibility(View.GONE);
                            mDeltaHChart.setVisibility(View.GONE);
                            mHeartChart.setVisibility(View.GONE);
                            showToast("监测点暂无数据");
                        }
                    });
                    return;
                }

                List<String> titleResponse = gnssFilterInfoResponse.getTitle();
                for (int i = 0; i < titleResponse.size(); i++) {
                    switch (titleResponse.get(i)) {
                        case "DataTimestamp":
                            mTitleIndex.put("DataTimestamp", i);
                            break;
                        case "GNSSFilterInfoN":
                            mTitleIndex.put("GNSSFilterInfoN", i);
                            break;
                        case "GNSSFilterInfoE":
                            mTitleIndex.put("GNSSFilterInfoE", i);
                            break;
                        case "GNSSFilterInfoH":
                            mTitleIndex.put("GNSSFilterInfoH", i);
                            break;
                        case "GNSSFilterInfoDeltaD":
                            mTitleIndex.put("GNSSFilterInfoDeltaD", i);
                            break;
                        case "GNSSFilterInfoDeltaH":
                            mTitleIndex.put("GNSSFilterInfoDeltaH", i);
                            break;
                    }
                }
                int timeStampIndex = mTitleIndex.get("DataTimestamp");
                int nIndex = mTitleIndex.get("GNSSFilterInfoN");
                int eIndex = mTitleIndex.get("GNSSFilterInfoE");
                int hIndex = mTitleIndex.get("GNSSFilterInfoH");
                int deltaDIndex = mTitleIndex.get("GNSSFilterInfoDeltaD");
                int deltaHIndex = mTitleIndex.get("GNSSFilterInfoDeltaH");
//                LogUtil.e("time N E H delta deltaH",timeStampIndex + " " + nIndex + " " + eIndex + " " + hIndex + " " + deltaDIndex + " " + deltaHIndex);
                mMaxResponse = gnssFilterInfoResponse.getMax();
                mMinResponse = gnssFilterInfoResponse.getMin();
                mAvgResponse = gnssFilterInfoResponse.getAverage();
                mInterval = gnssFilterInfoResponse.getData().getInterval();
                mMaxInterval = gnssFilterInfoResponse.getData().getMaxInterval();
                List<List<String>> xResponseData = new ArrayList<>();
                List<List<String>> yResponseData = new ArrayList<>();
                List<List<String>> hResponseData = new ArrayList<>();
                List<List<String>> deltaDResponseData = new ArrayList<>();
                List<List<String>> deltaHResponseData = new ArrayList<>();
                //数据提取
                LogUtil.e("返回数据的数量", String.valueOf(mContentResponse.size()));
                for (List<Object> responseData : mContentResponse) {
                    List<String> nValue = new ArrayList<>();
                    List<String> eValue = new ArrayList<>();
                    List<String> hValue = new ArrayList<>();
                    List<String> deltaDValue = new ArrayList<>();
                    List<String> deltaHValue = new ArrayList<>();
                    //时间
                    nValue.add(String.valueOf(Math.round((double) responseData.get(timeStampIndex))));
                    eValue.add(String.valueOf(Math.round((double) responseData.get(timeStampIndex))));
                    hValue.add(String.valueOf(Math.round((double) responseData.get(timeStampIndex))));
                    deltaDValue.add(String.valueOf(Math.round((double) responseData.get(timeStampIndex))));
                    deltaHValue.add(String.valueOf(Math.round((double) responseData.get(timeStampIndex))));
                    //数值
                    nValue.add(String.valueOf(responseData.get(nIndex)));
                    eValue.add(String.valueOf(responseData.get(eIndex)));
                    hValue.add(String.valueOf(responseData.get(hIndex)));
                    deltaDValue.add(String.valueOf(responseData.get(deltaDIndex)));
                    deltaHValue.add(String.valueOf(responseData.get(deltaHIndex)));
                    //添加到各自的数据列表中
                    xResponseData.add(nValue);
                    yResponseData.add(eValue);
                    hResponseData.add(hValue);
                    deltaDResponseData.add(deltaDValue);
                    deltaHResponseData.add(deltaHValue);
                }
                mNConvertData = convertData(xResponseData, mMinResponse.get(nIndex).toString(), startTime, mAvgResponse.get(nIndex).toString());
                mEConvertData = convertData(yResponseData, mMinResponse.get(eIndex).toString(), startTime, mAvgResponse.get(eIndex).toString());
                mHConvertData = convertData(hResponseData, mMinResponse.get(hIndex).toString(), startTime, mAvgResponse.get(hIndex).toString());
                mDeltaDConvertData = convertData(deltaDResponseData, mMinResponse.get(deltaDIndex).toString(), startTime, mAvgResponse.get(deltaDIndex).toString());
                mDeltaHConvertData = convertData(deltaHResponseData, mMinResponse.get(deltaHIndex).toString(), startTime, mAvgResponse.get(deltaHIndex).toString());
                mNConvertAvg = (float) (Double.parseDouble(mAvgResponse.get(nIndex).toString()) - Double.parseDouble(mMinResponse.get(nIndex).toString()));
                mEConvertAvg = (float) (Double.parseDouble(mAvgResponse.get(eIndex).toString()) - Double.parseDouble(mMinResponse.get(eIndex).toString()));
                mHConvertAvg = (float) (Double.parseDouble(mAvgResponse.get(hIndex).toString()) - Double.parseDouble(mMinResponse.get(hIndex).toString()));
                mDeltaDConvertAvg = (float) (Double.parseDouble(mAvgResponse.get(deltaDIndex).toString()) - Double.parseDouble(mMinResponse.get(deltaDIndex).toString()));
                mDeltaHConvertAvg = (float) (Double.parseDouble(mAvgResponse.get(deltaHIndex).toString()) - Double.parseDouble(mMinResponse.get(deltaHIndex).toString()));
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
     * @param values 转换后的数据
     */
    public void convertLines(List<PointValue> values) {
        if (values.size() == 1) {
            return;
        }
        float space = values.get(1).getX() - values.get(0).getX();
        float maxSpace = space * 2;
        float tempSpace;
        mChartLines.clear();
        List<PointValue> tempLines = new ArrayList<>();
        tempLines.add(new PointValue(values.get(0).getX(), values.get(0).getY()));   //加入第一个数据
        for (int i = 1; i < values.size(); i++) {
            tempSpace = mNConvertData.get(i).getX() - mNConvertData.get(i - 1).getX();
            if (tempSpace > maxSpace) {
                mChartLines.add(tempLines);
                tempLines = new ArrayList<>();  //如果用clear()方法清空，那么所有使用该列表的数据都空了。
            }
            space = tempSpace;
            maxSpace = space * 2;
            tempLines.add(new PointValue(values.get(i).getX(), values.get(i).getY()));
        }
        mChartLines.add(tempLines);   //将最后一段数据加入
    }

    /**
     * 同步获取数据
     *
     * @param graphicType 图表数据类型
     * @param stationUUID 设备ID
     * @param startTime   开始时间
     * @param endTime     结束时间
     */
    public void getData(final String graphicType, final String stationUUID, final String startTime, final String endTime, final String deltaTime) {
        Thread httpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                requestChartDataSync(graphicType, stationUUID, startTime, endTime, deltaTime);
                handler.sendEmptyMessageDelayed(200,0);
            }
        });
        httpThread.start();
    }

    /**
     * 根据选中的监测点和时间重新获取数据并重绘图表
     */
    public void refresh() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        switch (mTimeSp.getSelectedItemPosition()) {
            case 0:
                //最近1小时
//                startTime = sdf.format(DateUtil.getHourBegin(1));
//                endTime = sdf.format(DateUtil.getHourEnd());
                mStartTime = sdf.format(DateUtil.getCurrentTimeBegin(1));
                mEndTime = sdf.format(DateUtil.getCurrentTimeEnd(1));
                mDeltaTime = "60";
//                doLoadingDialog();
                break;
            case 1:
                //最近6小时
//                startTime = sdf.format(DateUtil.getHourBegin(6));
//                endTime = sdf.format(DateUtil.getHourEnd());
                mStartTime = sdf.format(DateUtil.getCurrentTimeBegin(6));
                mEndTime = sdf.format(DateUtil.getCurrentTimeEnd(6));
                mDeltaTime = "60";
//                doLoadingDialog();
                break;
            case 2:
//                startTime = sdf.format(DateUtil.getHourBegin(12));
//                endTime = sdf.format(DateUtil.getHourEnd());
                mStartTime = sdf.format(DateUtil.getCurrentTimeBegin(12));
                mEndTime = sdf.format(DateUtil.getCurrentTimeEnd(12));
                mDeltaTime = "60";
                break;
            case 3:
                //本日
                mStartTime = sdf.format(DateUtil.getDayBegin());
                mEndTime = sdf.format(DateUtil.getDayEnd());
                mDeltaTime = "60";
                break;
            case 4:
                //一周
                mStartTime = sdf.format(DateUtil.getBeginDayOfWeek());
                mEndTime = sdf.format(DateUtil.getEndDayOfWeek());
                mDeltaTime = "420";  //7min
                break;
            case 5:
                //一月
                mStartTime = sdf.format(DateUtil.getBeginDayOfMonth());
                mEndTime = sdf.format(DateUtil.getEndDayOfMonth());
                mDeltaTime = "1800";  //30min
                break;
            case 6:
                //一年
                mStartTime = sdf.format(DateUtil.getBeginDayOfYear());
                mEndTime = sdf.format(DateUtil.getEndDayOfYear());
                mDeltaTime = "21900";  //365min
                break;
            case 7:
                //自定义
                mDeltaTime = DateUtil.getCustomDeltaTime(mStartTime, mEndTime);
                break;
        }
        doLoadingDialog();
    }

    public void downLoadExcel() {
        //获取URL
        FormBody body = new FormBody.Builder()
                .add("AccessToken", ApiConfig.getAccessToken())
                .add("SessionUUID", ApiConfig.getSessionUUID())
                .add("StationUUID", mStationUUIDList.get(mDeviceSp.getSelectedItemPosition()))
                .add("StartTime", mStartTime)
                .add("EndTime", mEndTime)
                .build();
        final Api api = Api.config(ApiConfig.GET_STATION_REPORT);
        api.postRequestFormBody(getActivity(), body, new ApiCallback() {
            @Override
            public void onSuccess(String res) {
                String url = api.parseJSONObject(res, "ReportFilePath");
                LogUtil.e("获取的下载·地址为", url);
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null && !"".equals(url)) {
                    mainActivity.getDownloadBinder().startDownload(url);
                } else {
                    Toast.makeText(getActivity(), "获取URL有误，请稍后再试", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                LogUtil.e("downloadReport network failure", e.toString());
                Toast.makeText(getActivity(), "网络连接错误，请稍后再试", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void drawChart() {
        if (hasData) {
            drawXYHChart(mTimeSp.getSelectedItem().toString());
            drawDeltaChart(mTimeSp.getSelectedItem().toString());
            drawHeartChart(mTimeSp.getSelectedItem().toString());
        }
        if (mLoadingDlg != null)
        {
            mLoadingDlg.cancel();
        }
    }


    //时间选择器
    private void timePicker(final TextView textView) {
        //时间选择器
        TimePickerView pvTime = new TimePickerBuilder(getActivity(), new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
                textView.setText(format.format(date));
            }
//        }).setType(new boolean[]{true, true, true, true, true, true})// 默认全部显示
        }).setType(new boolean[]{true, true, true, true, false, false})// 年月日时
                .setCancelText("取消")//取消按钮文字
                .setSubmitText("确定")//确认按钮文字
                .setDate(Calendar.getInstance())//注：根据需求来决定是否使用该方法（一般是精确到秒的情况），此项可以在弹出选择器的时候重新设置当前时间，避免在初始化之后由于时间已经设定，导致选中时间与当前时间不匹配的问题。
                .setLabel("年", "月", "日", "时", "分", "秒")//默认设置为年月日时分秒
                .isDialog(true)
                .build();
        pvTime.show();
        //解决PickerView被Dialog覆盖问题
        Dialog dialog = pvTime.getDialog();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) pvTime.getDialogContainerLayout().getLayoutParams();
        layoutParams.leftMargin = 0;
        layoutParams.rightMargin = 0;

        pvTime.getDialogContainerLayout().setLayoutParams(layoutParams);
        if (dialog != null) {
            Window dialogWindow = dialog.getWindow();
            if (dialogWindow != null) {
                WindowManager.LayoutParams attributes = dialogWindow.getAttributes();
                attributes.dimAmount = 0.3f;
                attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
                attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
                attributes.gravity = Gravity.BOTTOM;
                dialogWindow.setAttributes(attributes);
                dialogWindow.setWindowAnimations(R.style.timePickerStyle);
            }
        }
    }
}