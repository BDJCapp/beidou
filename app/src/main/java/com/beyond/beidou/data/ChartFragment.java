package com.beyond.beidou.data;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.entites.GetGraphicDataResponse;
import com.beyond.beidou.my.FileManageFragment;
import com.beyond.beidou.util.FileUtil;
import com.beyond.beidou.views.MyDialog;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.views.MyDownloadDialog;
import com.beyond.beidou.views.MySpinner;
import com.beyond.beidou.R;
import com.beyond.beidou.api.Api;
import com.beyond.beidou.api.ApiCallback;
import com.beyond.beidou.api.ApiConfig;
import com.beyond.beidou.util.DateUtil;
import com.beyond.beidou.util.LogUtil;
import com.beyond.beidou.util.LoginUtil;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private Toolbar toolbar;
    private Spinner mDeviceSp;
    private MySpinner mTimeSp;
    private LineChartView mNChart, mEChart, mHChart,  mDeltaDChart, mDeltaHChart, mHeartChart;
    private TextView mNChartCooTv, mEChartCooTv, mHChartCooTv, mDeltaDChartCooTv, mDeltaHChartCooTv, mHeartChartCooTv;
    private TextView mNChartTimeTv, mEChartTimeTv, mHChartTimeTv,  mDeltaDChartTimeTv, mDeltaHChartTimeTv, mHeartChartTimeTv;
    private TextView mTitleTv;
    private TextView mDownLoadExcelTv;
    private float mConvertYMax;
    private float mConvertYMin;
    private float mFILNConvertAvg, mFILEConvertAvg, mFILHConvertAvg, mDeltaDConvertAvg, mDeltaHConvertAvg;
    private List<Object> mMaxResponse = new ArrayList<>();
    private List<Object> mMinResponse = new ArrayList<>();
    private List<Object> mAvgResponse = new ArrayList<>();
    private ScrollView mChartsSv;
    private ImageView mBackIv;
    private List<PointValue> mFILNConvertData = new ArrayList<>();
    private List<PointValue> mFILEConvertData = new ArrayList<>();
    private List<PointValue> mFILHConvertData = new ArrayList<>();

    private List<PointValue> mESTNConvertData = new ArrayList<>();
    private List<PointValue> mESTEConvertData = new ArrayList<>();
    private List<PointValue> mESTHConvertData = new ArrayList<>();
    private List<PointValue> mESTDeltaDConvertData = new ArrayList<>();
    private List<PointValue> mESTDeltaHConvertData = new ArrayList<>();
    private List<List<Object>> mFILContentResponse = new ArrayList<>();
    private List<List<Object>> mESTContentResponse = new ArrayList<>();
    private List<List<PointValue>> mFilChartLines = new ArrayList<>();
    private List<List<PointValue>> mEstChartLines = new ArrayList<>();

    private int mInterval, mMaxInterval;
    private boolean isFirstTimeSelectTime = true;
    private ArrayList<String> mStationUUIDList = new ArrayList<>();
    private String mStartTime = null;
    private String mEndTime = null;
    private String mDeltaTime = null;
    private boolean hasData = true;
    private Map<String, Integer> mTitleIndex = new HashMap<>();
    private MyDialog mTimePickerDlg;
    private MyDownloadDialog mDownloadDlg;
    private int mLastSelectedTimePosition;
    private static final int LOADING = 1;
    private static final int GET_DATA_SUCCESS = 200;
    private static final int N_CHART = 10;
    private static final int E_CHART = 11;
    private static final int H_CHART = 12;
    private static final int DELTAD_CHART = 13;
    private static final int DELTAH_CHART = 14;
    private ZLoadingDialog mLoadingDlg;
    public Handler pHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case LOADING:
                    getData(mStationUUIDList.get(mDeviceSp.getSelectedItemPosition()), mStartTime, mEndTime, mDeltaTime);
                    break;
                case GET_DATA_SUCCESS:
                    drawChart();
                    break;
            }
        }
    };
    boolean existException = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mLoadingDlg = new ZLoadingDialog(context);
    }

    private void doLoadingDialog() {
        mLoadingDlg.setLoadingBuilder(Z_TYPE.ROTATE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK)//颜色
                .setHintText("加载中")
                .setCanceledOnTouchOutside(false)
                .show();
        pHandler.sendEmptyMessageDelayed(LOADING, 0);
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
        setHasOptionsMenu(true);
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

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.mu_download, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mu_download:
                if (LoginUtil.isNetworkUsable(getActivity())) {
                    mDownloadDlg.show();
                    mDownloadDlg.setStartTime(mStartTime);
                    mDownloadDlg.setEndTime(mEndTime);
                    final MainActivity activity = (MainActivity) getActivity();
                    mDownloadDlg.setOnClickBottomListener(new MyDownloadDialog.OnClickBottomListener() {
                        @Override
                        public void onPositiveClick() {
                            if (activity != null) {
                                activity.displayToast("正在导出...");
                            }
                            downLoadExcel(mDownloadDlg.getStartTime(),mDownloadDlg.getEndTime(),mDownloadDlg.getCheckedButton());
                            mDownloadDlg.dismiss();
                        }

                        @Override
                        public void onNegativeClick() {
                            mDownloadDlg.dismiss();
                        }
                    });
                    mDownloadDlg.setOnClickTextViewListener(new MyDownloadDialog.OnClickTextViewListener() {
                        @Override
                        public void onStartTimeClick(View v) {
                            timePicker((TextView) v);
                        }

                        @Override
                        public void onEndTimeClick(View v) {
                            timePicker((TextView) v);
                        }
                    });
                }
                return true;
            case R.id.mu_manageFile:
                MainActivity activity = (MainActivity) getActivity();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment fileManageFragment = new FileManageFragment();
                activity.setFileManageFragment(fileManageFragment);
                activity.setNowFragment(fileManageFragment);
                activity.setExit(false);
                ft.add(R.id.layout_home, fileManageFragment).hide(this);
                ft.commit();
                activity.getNavigationView().setSelectedItemId(activity.getNavigationView().getMenu().getItem(3).getItemId());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void initView(final View view) {
        mTitleTv = view.findViewById(R.id.tv_title);
        mDeviceSp = view.findViewById(R.id.sp_device);
        mTimeSp = view.findViewById(R.id.sp_chart_time);
        mDownLoadExcelTv = view.findViewById(R.id.tv_load_excel);
        mTimePickerDlg = new MyDialog(getActivity());
        mDownloadDlg = new MyDownloadDialog(getActivity());
        if (getArguments() != null) {
            mTitleTv.setText(getArguments().getString("projectName"));
            ArrayAdapter<String> deviceAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getArguments().getStringArrayList("stationNameList"));
            deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            mDeviceSp.setAdapter(deviceAdapter);
            int selectedDevicePosition = getArguments().getInt("position");
            mDeviceSp.setSelection(selectedDevicePosition, true);
            mStationUUIDList = getArguments().getStringArrayList("stationUUIDList");
        }
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

        mNChartCooTv = view.findViewById(R.id.tv_coordinateSystemX);
        mNChartTimeTv = view.findViewById(R.id.tv_chartTimeX);

        mEChartCooTv = view.findViewById(R.id.tv_coordinateSystemY);
        mEChartTimeTv = view.findViewById(R.id.tv_chartTimeY);

        mHChartCooTv = view.findViewById(R.id.tv_coordinateSystemH);
        mHChartTimeTv = view.findViewById(R.id.tv_chartTimeH);

        mDeltaDChartCooTv = view.findViewById(R.id.tv_coordinateSystemDeltaD);
        mDeltaDChartTimeTv = view.findViewById(R.id.tv_chartTimeDeltaD);

        mDeltaHChartCooTv = view.findViewById(R.id.tv_coordinateSystemDeltaH);
        mDeltaHChartTimeTv = view.findViewById(R.id.tv_chartTimeDeltaH);

        mHeartChartCooTv = view.findViewById(R.id.tv_coordinateSystemHeart);
        mHeartChartTimeTv = view.findViewById(R.id.tv_chartTimeHeart);

        mBackIv = view.findViewById(R.id.img_chart_back);
        mBackIv.setOnClickListener(this);

        mChartsSv = view.findViewById(R.id.sv_charts);

//        mChartsSv.setOnTouchListener(new View.OnTouchListener() {
//                    private int lastY = 0;
//                    private int touchEventId = -9983761;
//
//                    Handler handler = new Handler() {
//                        @Override
//                        public void handleMessage(Message msg) {
//                            super.handleMessage(msg);
//                            View scroller = (View)msg.obj;
//                            if(msg.what==touchEventId) {
//                                if(lastY ==scroller.getScrollY()) {
//                                    handleStop(scroller);
//                                }else {
//                                    LogUtil.e("++++","****");
//                                    handler.sendMessageDelayed(handler.obtainMessage(touchEventId,scroller), 5);
//                                    lastY = scroller.getScrollY();
//                                }
//                            }
//                        }
//                    };
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        if(event.getAction() == MotionEvent.ACTION_UP) {
//                            handler.sendMessageDelayed(handler.obtainMessage(touchEventId,v), 5);
//                        }
//                        return false;
//                    }
//                    //这里写真正的事件
//                    private void handleStop(Object view) {
//                        LogUtil.e("CHartFra","ScrollView滑动停止");
//                    }
//                });


        //        //解决Menu不显示，因为主题为NoActio

        toolbar = view.findViewById(R.id.tb_chart);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);

        mTimeSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                if (LoginUtil.isNetworkUsable(getActivity())) {
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
                                if (mStartTime.equals("") | mEndTime.equals("") | DateUtil.calcHourOffset(mStartTime, mEndTime) <= 0) {
                                    showToast("请选择正确的时间区间！");
                                } else if (DateUtil.calcHourOffset(mStartTime, mEndTime) >= 8784) {
                                    showToast("最大查询间隔为两年，请重新选择");
                                } else {
                                    mTimePickerDlg.dismiss();
                                    refresh();
                                    mLastSelectedTimePosition = position;
                                }
                            }

                            @Override
                            public void onNegativeClick() {
                                LogUtil.e("取消按钮","1111111");
                                mTimePickerDlg.dismiss();
                                if (mLastSelectedTimePosition != 7) {
                                    LogUtil.e("位置7","++++++++");
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
                    } else {
                        refresh();
                        mLastSelectedTimePosition = position;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mDeviceSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (LoginUtil.isNetworkUsable(getActivity())) {
                    refresh();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * 绘制XYH图表
     */
    public void drawXYHChart(String selectedTime) {
        String time = mStartTime + "~" + mEndTime;
        mNChartTimeTv.setText(time);
        mNChartCooTv.setText(R.string.CoordinateSystem);

        mEChartTimeTv.setText(time);
        mEChartCooTv.setText(R.string.CoordinateSystem);

        mHChartTimeTv.setText(time);
        mHChartCooTv.setText(R.string.CoordinateSystem);
        List<AxisValue> xAxisValues = setXAxisValues(selectedTime);
        List<AxisValue> yLabel = setAxisYLabel(mMinResponse.get(mTitleIndex.get("GNSSPJKFIRInfoN")).toString(), mFILNConvertData,N_CHART);
        convertLines(mFILNConvertData, mFilChartLines);
        convertLines(mESTNConvertData, mEstChartLines);
        setLineChart(mNChart, xAxisValues, yLabel, mFILNConvertAvg);

        xAxisValues = setXAxisValues(selectedTime);
        yLabel = setAxisYLabel(mMinResponse.get(mTitleIndex.get("GNSSPJKFIRInfoE")).toString(), mFILEConvertData,E_CHART);
        convertLines(mFILEConvertData, mFilChartLines);
        convertLines(mESTEConvertData, mEstChartLines);
        setLineChart(mEChart, xAxisValues, yLabel, mFILEConvertAvg);

        xAxisValues = setXAxisValues(selectedTime);
        yLabel = setAxisYLabel(mMinResponse.get(mTitleIndex.get("GNSSPJKFIRInfoH")).toString(), mFILHConvertData,H_CHART);
        convertLines(mFILHConvertData, mFilChartLines);
        convertLines(mESTHConvertData, mEstChartLines);
        setLineChart(mHChart, xAxisValues, yLabel, mFILHConvertAvg);
    }

    /**
     * 绘制位移图表
     */
    public void drawDeltaChart(String selectedTime) {
        String time = mStartTime + "~" + mEndTime;
        mDeltaDChartTimeTv.setText(time);
        mDeltaDChartCooTv.setText(R.string.CoordinateSystem);
        mDeltaHChartTimeTv.setText(time);
        mDeltaHChartCooTv.setText(R.string.CoordinateSystem);
        List<AxisValue> xAxisValues = setXAxisValues(selectedTime);
        List<AxisValue> yLabel = setAxisYLabel(mMinResponse.get(mTitleIndex.get("GNSSPJKFIRInfoDeltaD")).toString(), mESTDeltaDConvertData, DELTAD_CHART);
        convertLines(mESTDeltaDConvertData, mEstChartLines);
        setLineChart(mDeltaDChart, xAxisValues, yLabel, mDeltaDConvertAvg);

        xAxisValues = setXAxisValues(selectedTime);
        yLabel = setAxisYLabel(mMinResponse.get(mTitleIndex.get("GNSSPJKFIRInfoDeltaH")).toString(), mESTDeltaHConvertData, DELTAH_CHART);
        convertLines(mESTDeltaHConvertData, mEstChartLines);
        setLineChart(mDeltaHChart, xAxisValues, yLabel, mDeltaHConvertAvg);
    }

    public void drawHeartChart() {
        String time = mStartTime + "~" + mEndTime;
        ArrayList edgeList = new ArrayList();
        mHeartChartTimeTv.setText(time);
        mHeartChartCooTv.setText(R.string.CoordinateSystem);
        List<AxisValue> nAxisValues = new ArrayList<>();
        List<AxisValue> eAxisValues = new ArrayList<>();
        List<PointValue> mEstPointValues = new ArrayList<>();
        convertHeartChartData( mESTContentResponse, nAxisValues, eAxisValues,  mEstPointValues, edgeList);
        setHeartChart(mHeartChart, mEstPointValues, nAxisValues, eAxisValues, edgeList);
    }

    public void setHeartChart(LineChartView chartView,  List<PointValue> estValues, List<AxisValue> xAxisValues, List<AxisValue> yAxisValues, ArrayList<Double> edgeList) {
        Line estLine = new Line(estValues).setColor(Color.parseColor("#0000ff")).setCubic(false).setPointRadius(0).setStrokeWidth(2);

        List<Line> lines = new ArrayList<>();
        lines.add(estLine);

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
        if(existException){
            viewport.top = edgeList.get(3).floatValue();
            viewport.bottom = edgeList.get(2).floatValue();
            viewport.left = edgeList.get(0).floatValue();
            viewport.right = edgeList.get(1).floatValue();
            existException = false;
        }else{
            viewport.top += 0.02f;
            viewport.bottom -= 0.02f;
            viewport.left -= 0.02f;
            viewport.right += 0.02f;
        }
        chartView.setMaximumViewport(viewport);
        chartView.setCurrentViewport(viewport);

    }

    public void convertHeartChartData(List<List<Object>> estContentList, List<AxisValue> xAxisValues, List<AxisValue> yAxisValues,  List<PointValue> estPointValues, ArrayList<Double> edgeList) {
        int nIndex, eIndex;
        nIndex = mTitleIndex.get("GNSSPJKFIRInfoN");
        eIndex = mTitleIndex.get("GNSSPJKFIRInfoE");

        double mEMin, mEMax, mNMin, mNMax;
        if(!TextUtils.isEmpty(estContentList.get(0).get(eIndex).toString()) && !TextUtils.isEmpty(estContentList.get(0).get(nIndex).toString())){
            mEMin = mEMax = Double.parseDouble(estContentList.get(0).get(eIndex).toString());
            mNMin = mNMax = Double.parseDouble(estContentList.get(0).get(nIndex).toString());
        }else{
            mEMin = mEMax = mNMax = mNMin = 0;
        }
        for (List<Object> list : estContentList) {
            double mETmp = Double.parseDouble(list.get(eIndex).toString());
            double mNTmp = Double.parseDouble(list.get(nIndex).toString());
            if(mEMin > mETmp){
                mEMin = mETmp;
            }
            if(mEMax < mETmp){
                mEMax = mETmp;
            }

            if(mNMin > mNTmp){
                mNMin = mNTmp;
            }
            if(mNMax < mNTmp){
                mNMax = mNTmp;
            }
        }

        double convertEMin, convertEMax, convertNMin, convertNMax;
        DecimalFormat df = new DecimalFormat("#.00");
        String space = "0.01";
        BigDecimal bSpace = new BigDecimal(space);

        double bResponseEminTmp = mEMin;
        double bResponseEmaxTmp = mEMax;

        if(bResponseEmaxTmp - bResponseEminTmp > 100){
            bResponseEmaxTmp = bResponseEminTmp + 0.2;
            existException = true;
        }

        BigDecimal bResponseEmin = new BigDecimal(df.format(bResponseEminTmp));
        BigDecimal bResponseEMax = new BigDecimal(df.format(bResponseEmaxTmp));

        bResponseEmin = bResponseEmin.subtract(new BigDecimal("0.02"));
        bResponseEMax = bResponseEMax.add(new BigDecimal("0.02"));


        convertEMin = Double.parseDouble(String.valueOf(bResponseEmin));
        convertEMax = Double.parseDouble(String.valueOf(bResponseEMax));
        double tempDouble = Double.parseDouble(String.valueOf(bResponseEmin));
        String tempString = String.valueOf(convertEMin);
        BigDecimal strEMin = new BigDecimal(df.format(bResponseEminTmp));
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

        double bResponseNMinTmp = mNMin;
        double bResponseNMaxTmp = mNMax;

        if(bResponseNMaxTmp - bResponseNMinTmp > 100){
            bResponseNMaxTmp = bResponseNMinTmp + 0.2;
            existException = true;
        }


        BigDecimal bResponseNMin = new BigDecimal(df.format(bResponseNMinTmp));
        BigDecimal bResponseNMax = new BigDecimal(df.format(bResponseNMaxTmp));

        bResponseNMin = bResponseNMin.subtract(new BigDecimal("0.02"));
        bResponseNMax = bResponseNMax.add(new BigDecimal("0.02"));

        convertNMin = Double.parseDouble(String.valueOf(bResponseNMin));
        convertNMax = Double.parseDouble(String.valueOf(bResponseNMax));
        tempDouble = Double.parseDouble(String.valueOf(bResponseNMin));
        tempString = String.valueOf(convertNMin);
        BigDecimal strNMin = new BigDecimal(df.format(bResponseNMinTmp));
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
        BigDecimal minuendN1 = new BigDecimal(df.format(mNMin));
        minuendN1 = minuendN1.subtract(new BigDecimal("0.02"));
        BigDecimal minuendE1 = new BigDecimal(df.format(mEMin));
        minuendE1 = minuendE1.subtract(new BigDecimal("0.02"));

        for (List<Object> content : estContentList) {
            tempN = Double.parseDouble(content.get(nIndex).toString()) - Double.parseDouble(minuendN1.toString());
            tempE = Double.parseDouble(content.get(eIndex).toString()) - Double.parseDouble(minuendE1.toString());
            estPointValues.add(new PointValue(Float.parseFloat(String.valueOf(tempE)), Float.parseFloat(String.valueOf(tempN))));
        }

        edgeList.add(-0.02);
        edgeList.add(bResponseEmaxTmp - bResponseEminTmp + 0.02);
        edgeList.add(-0.02);
        edgeList.add(bResponseNMaxTmp - bResponseNMinTmp + 0.02);
    }


    public List<AxisValue> setAxisYLabel(String yMin, List<PointValue> values, int chartType) {
        float chartValue;
        String tempString;
        String minSubNum;     //最小值的减数
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

        //结果出现是0.005时，在浮点运算时会四舍五入为0.01，影响运算结果
        if (Math.abs(valueYMin - 0.005) < 0.0001) {
            valueYMin = 0;
        }

        //防止数据波动过大导致卡死
        if (valueYMax - valueYMin > 100) {
            mConvertYMax = valueYMin + 0.2f;
            mConvertYMin = valueYMin - 0.01f;
            if (chartType == DELTAD_CHART) {
                mDeltaDConvertAvg = (mConvertYMax + mConvertYMin) / 2;
            } else if (chartType == DELTAH_CHART) {
                mDeltaHConvertAvg = (mConvertYMax + mConvertYMin) / 2;
            }else if (chartType == N_CHART) {
                mFILNConvertAvg = (mConvertYMax + mConvertYMin) / 2;
            }else if (chartType == E_CHART) {
                mFILEConvertAvg = (mConvertYMax + mConvertYMin) / 2;
            }else if (chartType == H_CHART) {
                mFILHConvertAvg = (mConvertYMax + mConvertYMin) / 2;
            }
            minSubNum = "0.01";
        } else {
            mConvertYMax = valueYMax + 0.1f;
            mConvertYMin = valueYMin - 0.1f;
            minSubNum = "0.1";
        }

        String space = "0.01";     //每格大小为0.01m

        //保证convertYmin和yMin是真实值。这样即使转成只有两位小数，也是对应的。
        DecimalFormat df = new DecimalFormat("#.00");//只保留小数点后两位，厘米级精度
        BigDecimal b_ymin = new BigDecimal(df.format(mConvertYMin));
        BigDecimal tempYmin = new BigDecimal(df.format(Double.parseDouble(yMin)));
        BigDecimal b_space = new BigDecimal(space);
        chartValue = mConvertYMin;
        tempYmin = tempYmin.subtract(new BigDecimal(minSubNum));
        BigDecimal s_yMin = new BigDecimal(df.format(tempYmin));  //传入格式化后的最小值

        while (chartValue <= mConvertYMax) {
            b_ymin = b_ymin.add(b_space);
            s_yMin = s_yMin.add(b_space);
            tempString = String.valueOf(s_yMin);
            chartValue = Float.parseFloat(String.valueOf(b_ymin));
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
    public void setLineChart(LineChartView chartView, List<AxisValue> xAxisValues, List<AxisValue> yAxisValues, float convertAverage) {
        String selectedTime = mTimeSp.getSelectedItem().toString();
        List<Line> lines = new ArrayList<>();
        if (chartView != mDeltaDChart && chartView != mDeltaHChart){
            for (int i = 0; i < mFilChartLines.size(); i++) {
                Line line = new Line(mFilChartLines.get(i)).setColor(getResources().getColor(R.color.filter_line)).setCubic(false).setPointRadius(0).setStrokeWidth(2);
                lines.add(line);
            }
        }
        for (int i = 0; i < mEstChartLines.size(); i++) {
            Line line = new Line(mEstChartLines.get(i)).setColor(getResources().getColor(R.color.estimate_line)).setCubic(false).setPointRadius(0).setStrokeWidth(2);
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
        LogUtil.e("window convertAverage bottom top", convertAverage + "**" + currentWindow.bottom + "**" + currentWindow.top);
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
                maxWindow.right = 60 * hourOffSet + (float) (60 * hourOffSet / 24);
                if (hourOffSet > 7 * 24) {
                    maxWindow.left = DateUtil.getOffSet();
                } else {
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
                MainActivity activity = (MainActivity) getActivity();
                if (activity.getDataFragment() == null) {
                    fm.beginTransaction().remove(activity.getChartFragment()).commit();
                    activity.setChartFragment(null);
                    activity.getNavigationView().setSelectedItemId(activity.getNavigationView().getMenu().getItem(1).getItemId());
                } else {
                    fm.beginTransaction().hide(activity.getChartFragment()).show(activity.getDataFragment()).remove(activity.getChartFragment()).commit();
                    activity.setChartFragment(null);
                }
                activity.setNowFragment(activity.getDataFragment());
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
//                LogUtil.e("请求点数据结束时间", sdfTwo.format(System.currentTimeMillis()));
                LogUtil.e("请求数据的返回值", res);
                Gson gson = new Gson();
                final GetGraphicDataResponse graphicDataResponse = gson.fromJson(res, GetGraphicDataResponse.class);

                if ("GNSSPJKFIRInfo".equals(graphicType)) {
                    mFILContentResponse = graphicDataResponse.getContent();
                } else {
                    mESTContentResponse = graphicDataResponse.getContent();
                }

                //其他错误
                if (!graphicDataResponse.getResponseCode().equals("200")) {
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
                            showToast(graphicDataResponse.getResponseMsg());
                        }
                    });
                    return;
                }
                //基准站没有数据
                if (mFILContentResponse.size() == 0) {
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
                            showToast("该监测点暂无数据");
                        }
                    });
                    return;
                }

                //设置返回Json各字段的下标，防止因返回顺序变化导致获取错误数据
                List<String> titleResponse = graphicDataResponse.getTitle();
                for (int i = 0; i < titleResponse.size(); i++) {
                    switch (titleResponse.get(i)) {
                        case "DataTimestamp":
                            mTitleIndex.put("DataTimestamp", i);
                            break;
                        case "GNSSPJKFIRInfoN":
                            mTitleIndex.put("GNSSPJKFIRInfoN", i);
                            break;
                        case "GNSSPJKFIRInfoE":
                            mTitleIndex.put("GNSSPJKFIRInfoE", i);
                            break;
                        case "GNSSPJKFIRInfoH":
                            mTitleIndex.put("GNSSPJKFIRInfoH", i);
                            break;
                        case "GNSSPJKFIRInfoDeltaD":
                            mTitleIndex.put("GNSSPJKFIRInfoDeltaD", i);
                            break;
                        case "GNSSPJKFIRInfoDeltaH":
                            mTitleIndex.put("GNSSPJKFIRInfoDeltaH", i);
                            break;
                    }
                }

                initChartData(graphicDataResponse, startTime, graphicType);
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }

    public void initChartData(GetGraphicDataResponse graphicDataResponse, String startTime, String graphicType) {
        int timeStampIndex = mTitleIndex.get("DataTimestamp");
        int nIndex = mTitleIndex.get("GNSSPJKFIRInfoN");
        int eIndex = mTitleIndex.get("GNSSPJKFIRInfoE");
        int hIndex = mTitleIndex.get("GNSSPJKFIRInfoH");
        int deltaDIndex = mTitleIndex.get("GNSSPJKFIRInfoDeltaD");
        int deltaHIndex = mTitleIndex.get("GNSSPJKFIRInfoDeltaH");
        List<List<String>> xResponseData = new ArrayList<>();
        List<List<String>> yResponseData = new ArrayList<>();
        List<List<String>> hResponseData = new ArrayList<>();
        List<List<String>> deltaDResponseData = new ArrayList<>();
        List<List<String>> deltaHResponseData = new ArrayList<>();
        //数据提取
        LogUtil.e("返回数据的数量", String.valueOf(graphicDataResponse.getContent().size()));
        for (List<Object> responseData : graphicDataResponse.getContent()) {
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
        if ("GNSSPJKFIRInfo".equals(graphicType)) {
            mMaxResponse = graphicDataResponse.getMax();
            mMinResponse = graphicDataResponse.getMin();
            mAvgResponse = graphicDataResponse.getAverage();
            mInterval = graphicDataResponse.getData().getInterval();
            mMaxInterval = graphicDataResponse.getData().getMaxInterval();
            mFILNConvertData = convertData(xResponseData, mMinResponse.get(nIndex).toString(), startTime, mAvgResponse.get(nIndex).toString());
            mFILEConvertData = convertData(yResponseData, mMinResponse.get(eIndex).toString(), startTime, mAvgResponse.get(eIndex).toString());
            mFILHConvertData = convertData(hResponseData, mMinResponse.get(hIndex).toString(), startTime, mAvgResponse.get(hIndex).toString());
//            mFILDeltaDConvertData = convertData(deltaDResponseData, mMinResponse.get(deltaDIndex).toString(), startTime, mAvgResponse.get(deltaDIndex).toString());
//            mFILDeltaHConvertData = convertData(deltaHResponseData, mMinResponse.get(deltaHIndex).toString(), startTime, mAvgResponse.get(deltaHIndex).toString());

            mFILNConvertAvg = (float) (Double.parseDouble(mAvgResponse.get(nIndex).toString()) - Double.parseDouble(mMinResponse.get(nIndex).toString()));
            mFILEConvertAvg = (float) (Double.parseDouble(mAvgResponse.get(eIndex).toString()) - Double.parseDouble(mMinResponse.get(eIndex).toString()));
            mFILHConvertAvg = (float) (Double.parseDouble(mAvgResponse.get(hIndex).toString()) - Double.parseDouble(mMinResponse.get(hIndex).toString()));
//            mDeltaDConvertAvg = (float) (Double.parseDouble(mAvgResponse.get(deltaDIndex).toString()) - Double.parseDouble(mMinResponse.get(deltaDIndex).toString()));
//            mDeltaHConvertAvg = (float) (Double.parseDouble(mAvgResponse.get(deltaHIndex).toString()) - Double.parseDouble(mMinResponse.get(deltaHIndex).toString()));
            LogUtil.e("getData执行结束", "获取滤波数据++++++");
            hasData = true;
        } else if ("GNSSPJKESTInfo".equals(graphicType)) {
            mMaxResponse.set(deltaDIndex,graphicDataResponse.getMax().get(deltaDIndex));
            mMaxResponse.set(deltaHIndex,graphicDataResponse.getMax().get(deltaHIndex));
            mMinResponse.set(deltaDIndex,graphicDataResponse.getMin().get(deltaDIndex));
            mMinResponse.set(deltaHIndex,graphicDataResponse.getMin().get(deltaHIndex));
            mAvgResponse.set(deltaDIndex,graphicDataResponse.getAverage().get(deltaDIndex));
            mAvgResponse.set(deltaHIndex,graphicDataResponse.getAverage().get(deltaHIndex));
            mESTNConvertData = convertData(xResponseData, mMinResponse.get(nIndex).toString(), startTime, mAvgResponse.get(nIndex).toString());
            mESTEConvertData = convertData(yResponseData, mMinResponse.get(eIndex).toString(), startTime, mAvgResponse.get(eIndex).toString());
            mESTHConvertData = convertData(hResponseData, mMinResponse.get(hIndex).toString(), startTime, mAvgResponse.get(hIndex).toString());
            mESTDeltaDConvertData = convertData(deltaDResponseData, mMinResponse.get(deltaDIndex).toString(), startTime, mAvgResponse.get(deltaDIndex).toString());
            mESTDeltaHConvertData = convertData(deltaHResponseData, mMinResponse.get(deltaHIndex).toString(), startTime, mAvgResponse.get(deltaHIndex).toString());
            mDeltaDConvertAvg = (float) (Double.parseDouble(mAvgResponse.get(deltaDIndex).toString()) - Double.parseDouble(mMinResponse.get(deltaDIndex).toString()));
            mDeltaHConvertAvg = (float) (Double.parseDouble(mAvgResponse.get(deltaHIndex).toString()) - Double.parseDouble(mMinResponse.get(deltaHIndex).toString()));
            LogUtil.e("getData执行结束", "获取估计数据*******");
            hasData = true;
        }

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
     * 返回的数据可能是不连续的
     * 判断转换后的数据有几段，并转换成线数据。
     *
     * @param values 转换后的数据
     */
    public void convertLines(List<PointValue> values, List<List<PointValue>> chartLines) {
        if (values.size() == 1) {
            return;
        }
        float space = values.get(1).getX() - values.get(0).getX();
        float maxSpace = space * 2;
        float tempSpace;
        chartLines.clear();
        List<PointValue> tempLines = new ArrayList<>();
        tempLines.add(new PointValue(values.get(0).getX(), values.get(0).getY()));   //加入第一个数据
        for (int i = 1; i < values.size(); i++) {
            tempSpace = mFILNConvertData.get(i).getX() - mFILNConvertData.get(i - 1).getX();
            if (tempSpace > maxSpace) {
                chartLines.add(tempLines);
                tempLines = new ArrayList<>();  //注意此处清空数据的方法。如果用clear()方法清空，那么所有使用该列表的数据都空了。
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
     * @param stationUUID 设备ID
     * @param startTime   开始时间
     * @param endTime     结束时间
     */
    public void getData(final String stationUUID, final String startTime, final String endTime, final String deltaTime) {
        Thread httpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                requestChartDataSync("GNSSPJKFIRInfo", stationUUID, startTime, endTime, deltaTime);
                requestChartDataSync("GNSSPJKESTInfo", stationUUID, startTime, endTime, deltaTime);
                pHandler.sendEmptyMessageDelayed(GET_DATA_SUCCESS, 0);
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
                mStartTime = sdf.format(DateUtil.getCurrentTimeBegin(1));
                mEndTime = sdf.format(DateUtil.getCurrentTimeEnd(1));
                mDeltaTime = "60";
                break;
            case 1:
                //最近6小时
                mStartTime = sdf.format(DateUtil.getCurrentTimeBegin(6));
                mEndTime = sdf.format(DateUtil.getCurrentTimeEnd(6));
                mDeltaTime = "60";
                break;
            case 2:
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
//                mDeltaTime = "420";  //7min
                mDeltaTime = "300";  //5min
                break;
            case 5:
                //一月
                mStartTime = sdf.format(DateUtil.getBeginDayOfMonth());
                mEndTime = sdf.format(DateUtil.getEndDayOfMonth());
//                mDeltaTime = "1800";  //30min
                mDeltaTime = "7200";  //2h
                break;
            case 6:
                //一年
                mStartTime = sdf.format(DateUtil.getBeginDayOfYear());
                mEndTime = sdf.format(DateUtil.getEndDayOfYear());
//                mDeltaTime = "21900";  //365min
                mDeltaTime = "86400";  //16h
                break;
            case 7:
                //自定义
                mDeltaTime = DateUtil.getCustomDeltaTime(mStartTime, mEndTime);
                break;
        }
        doLoadingDialog();
    }

    public void downLoadExcel(String startTime,String endTime,String downloadType) {
        MainActivity mainActivity = (MainActivity) getActivity();
        String projectUUID= FileUtil.getProjectUUIDByName(mainActivity, mainActivity.getPresentProject());
        JSONObject jsonData = new JSONObject();
        JSONArray stationUUIDArray = new JSONArray();
        LogUtil.e("+++++++++downloadType",downloadType);
        try {
            jsonData.put("AccessToken", ApiConfig.getAccessToken());
            jsonData.put("SessionUUID", ApiConfig.getSessionUUID());
            jsonData.put("ProjectUUID", projectUUID);
            jsonData.put("StartTime", startTime);
            jsonData.put("EndTime", endTime);
            if ("currentPoint".equals(downloadType)){
                stationUUIDArray.put(0,mStationUUIDList.get(mDeviceSp.getSelectedItemPosition()));
                jsonData.put("StationUUID", stationUUIDArray);
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

        final Api api = Api.config(ApiConfig.GET_PROJECT_REPORT);
        api.postJsonString(getActivity(), jsonData.toString(), new ApiCallback() {
            @Override
            public void onSuccess(String res) {
                String responseCode = api.parseSimpleJson(res, "ResponseCode");
                String responseMsg = api.parseSimpleJson(res, "ResponseMsg");
                if ("200".equals(responseCode)) {
                    String url = api.parseNestedJson(res, "ReportFileInfo","ReportExcel");
                    if (url != null){
                        MainActivity mainActivity = (MainActivity) getActivity();
                        if (mainActivity != null) {
                            mainActivity.getDownloadBinder().startDownload(url,getStringFromSP("userName"));
                        } else {
                            showToastSync("导出失败," + responseMsg);
                        }
                    }else {
                        showToastSync("当前文件路径为空，请稍后再试");
                    }
                } else {
                    showToastSync(responseMsg);
                }
            }

            @Override
            public void onFailure(Exception e) {
                showToastSync("请求失败，请检查网络连接，稍后再试");
            }
        });
    }

    public void drawChart() {
        if (hasData) {
            drawXYHChart(mTimeSp.getSelectedItem().toString());
            drawDeltaChart(mTimeSp.getSelectedItem().toString());
            drawHeartChart();
        }
        if (mLoadingDlg != null) {
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