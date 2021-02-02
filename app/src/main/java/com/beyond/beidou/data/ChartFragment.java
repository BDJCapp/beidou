package com.beyond.beidou.data;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.my.MyFragment;
import com.beyond.beidou.project.ProjectFragment;
import com.beyond.beidou.utils.LogUtil;
import com.beyond.beidou.utils.MyPointValue;
import com.beyond.beidou.warning.WarningFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;


public class ChartFragment extends BaseFragment implements View.OnClickListener{


    private Spinner spDevice,spChart,spTime;
    private LineChartView chartX,chartY,chartH;
    private List<PointValue> xValues = new ArrayList<>();   //X图表数据
    private List<PointValue> yValues = new ArrayList<>();   //Y图表数据
    private List<PointValue> hValues = new ArrayList<>();   //H图表数据
    private List<MyPointValue> preValues = new ArrayList<>();
    private TextView xChartName,yChartName,hChartName;
    private TextView xChartCoo,yChartCoo,hChartCoo;
    private TextView xChartTime,yChartTime,hChartTime;
    private float valueymax, valueymin,ymax,ymin;
    private String cutNum;

    private ImageView imgBack;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        initView(view);
        initChart();
        View view1 = view.findViewById(R.id.header);
        initAfterSetContentView(getActivity(), view1);
        return view;
    }

    public void initView(View view)
    {
        spChart = view.findViewById(R.id.sp_chart_type);
        spDevice = view.findViewById(R.id.sp_device);
        spTime = view.findViewById(R.id.sp_chart_time);

        String[] devices = new String[]{"监测点1","监测点2","监测点3"};
        String[] charts = new String[]{"XYH","位移图","心型图"};
        String[] times = new String[]{"最近1小时","最近6小时","最近12小时","本日","本周","本月","本年"};
        ArrayAdapter<String> deviceAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,devices);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        ArrayAdapter<String> chartAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,charts);
        chartAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,times);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spTime.setAdapter(timeAdapter);
        spDevice.setAdapter(deviceAdapter);
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

        imgBack = view.findViewById(R.id.img_chart_back);
        imgBack.setOnClickListener(this);
    }

    public void initChart()
    {
        initChartValue();
        xChartName.setText("X");
        xChartTime.setText("2021-02-02 10:00:00~2021-02-02 11:00:00");
        xChartCoo.setText("WGS84坐标系|");

        yChartName.setText("Y");
        yChartTime.setText("2021-02-02 10:00:00~2021-02-02 11:00:00");
        yChartCoo.setText("WGS84坐标系|");

        hChartName.setText("H");
        hChartTime.setText("2021-02-02 10:00:00~2021-02-02 11:00:00");
        hChartCoo.setText("WGS84坐标系|");
        List<AxisValue> yAxisValues = setAxisYLabel(cutNum,xValues);
        List<AxisValue> xAxisValues = set1HourAxisXLabel(xValues);
        setChart(chartX,xValues,xAxisValues,yAxisValues);
        setChart(chartY,yValues,xAxisValues,yAxisValues);
        setChart(chartH,hValues,xAxisValues,yAxisValues);
    }

    public void initChartValue()
    {
        preValues.add(new MyPointValue(1,3550716.442));
        preValues.add(new MyPointValue(2,3550716.439));
        preValues.add(new MyPointValue(3,3550716.440));
        preValues.add(new MyPointValue(4,3550716.443));
        preValues.add(new MyPointValue(5,3550716.442));
        preValues.add(new MyPointValue(6,3550716.442));
        preValues.add(new MyPointValue(7,3550716.442));
        preValues.add(new MyPointValue(8,3550716.443));
        preValues.add(new MyPointValue(9,3550716.443));
        preValues.add(new MyPointValue(10,3550716.443));
        preValues.add(new MyPointValue(11,3550716.444));
        preValues.add(new MyPointValue(12,3550716.444));
        preValues.add(new MyPointValue(13,3550716.444));

        //裁剪
        String tempstring;
        float tempfloat;
        cutNum = String.valueOf(preValues.get(0).getY()).substring(0, 3);
        for(MyPointValue point:preValues)
        {
            //数据预处理，将去掉公共前两位
            tempstring = String.valueOf(point.getY()).substring(3);
            tempfloat = Float.valueOf(tempstring);
            xValues.add(new PointValue((float)point.getX(),tempfloat));
            yValues.add(new PointValue((float)point.getX(),tempfloat));
            hValues.add(new PointValue((float)point.getX(),tempfloat));
        }

    }

    /**
     * 设置Y轴的标签
     * @param cutNum 裁剪的整数
     * @param values 图表数据
     * @return  Y轴标签列表
     */
    public List<AxisValue> setAxisYLabel(String cutNum,List<PointValue> values)
    {
        String tempstring;
        float tempfloat;
        List<AxisValue> axisValues = new ArrayList<>();
        valueymax = values.get(0).getY();
        valueymin = values.get(0).getY();
        for(lecho.lib.hellocharts.model.PointValue pointValue:values)
        {
            //确定最大最小值
            if(pointValue.getY() >= valueymax)
            {
                valueymax = pointValue.getY();
            }
            if(pointValue.getY() <= valueymin)
            {
                valueymin = pointValue.getY();
            }
        }

        String sp = "0.01";     //每格大小为0.01m
        ymax = valueymax + 0.1f;
        ymin = valueymin - 0.1f;

        DecimalFormat df = new DecimalFormat("#.00");//只保留小数点后两位，厘米级精度

        BigDecimal b_ymin = new BigDecimal(df.format(ymin));//解决浮点型数据加减运算精度问题
        //BigDecimal b_ymin = new BigDecimal(String.valueOf(ymin));
        BigDecimal b_space = new BigDecimal(sp);

        tempfloat = ymin;

        while (tempfloat <= ymax)
        {
            b_ymin = b_ymin.add(b_space);
            tempstring = cutNum + String.valueOf(b_ymin);
            tempfloat = Float.parseFloat(String.valueOf(b_ymin));
            //Log.d("tempflaot",String.valueOf(tempfloat));
            AxisValue axisValue = new AxisValue(tempfloat);
            axisValue.setLabel(tempstring);
            axisValues.add(axisValue);
        }
        return axisValues;
    }

    public List<AxisValue> set1HourAxisXLabel(List<PointValue> values)
    {
        String[] labels = new String[]{"10:00","10:05","10:10","10:15","10:20","10:25","10:30","10:35","10:40","10:45","10:50","10:55","11:00"};
        List<AxisValue> axisValues = new ArrayList<>();
        for (int i = 0; i < 13; i++)
        {
            AxisValue axisValue = new AxisValue(values.get(i).getX());
            axisValue.setLabel(labels[i]);
            axisValues.add(axisValue);
        }
        return axisValues;
    }

    public void setChart(LineChartView chartView,List<PointValue> values,List<AxisValue> xAxisValues,List<AxisValue> yAxisValues)
    {
        Line line = new Line(values).setColor(Color.parseColor("#2196F3")).setCubic(false).setPointRadius(0).setStrokeWidth(2);
        //画散点图
        //Line line = new Line(values).setHasLines(false).setPointColor(Color.BLUE).setPointRadius(4);
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
        axisY.setMaxLabelChars(10);
        //为两个坐标系设定名称
        axisY.setName("单位(米)");
        //设置图标所在位置
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);
        //将数据添加到View中
        chartView.setLineChartData(data);
        chartView.setInteractive(true);
        chartView.setZoomType(ZoomType.HORIZONTAL);
        chartView.setMaxZoom(4f);
        chartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        chartView.setVisibility(View.VISIBLE);

        Viewport v = new Viewport(chartView.getMaximumViewport());

        v.bottom = ymin;
        v.top = ymax;
        chartView.setMaximumViewport(v);
        chartView.setCurrentViewport(v);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_chart_back:
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
                MainActivity activity = (MainActivity) getActivity();
                activity.setChartFragment(null);
                activity.setNowFragment(activity.getDataFragment());
                break;
        }
    }

}