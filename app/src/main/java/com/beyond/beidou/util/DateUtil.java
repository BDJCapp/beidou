package com.beyond.beidou.util;

import android.util.Log;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import lecho.lib.hellocharts.model.AxisValue;


public class DateUtil {

    public static final int TYPE_28 = 28;
    public static final int TYPE_29 = 29;
    public static final int TYPE_30 = 30;
    public static final int TYPE_31 = 31;

    private static Integer[] month_30 = {4, 6, 9, 11};
    private static Integer[] month_31 = {1, 3, 5, 7, 8, 10, 12};
    private static List<Integer> monthList_30 = new ArrayList<>(Arrays.asList(month_30));
    private static List<Integer> monthList_31 = new ArrayList<>(Arrays.asList(month_31));

    private static Calendar cal = Calendar.getInstance();
    private static String labelEndTime;
    private static long offSet = 0;

    public static String getLabelEndTime() {
        return labelEndTime;
    }

    public static long getOffSet() {
        return offSet;
    }

    //****************************************************************************************
    //修改后最近1，6，12小时
    public static Date getCurrentTimeEnd(int hour) {
        int timeInterval = 0;
        switch (hour) {
            case 1:
                timeInterval = 5;
                break;
            case 6:
                timeInterval = 30;
                break;
            case 12:
                timeInterval = 60;
                break;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        while (calendar.get(Calendar.MINUTE) % timeInterval != 0) {
            calendar.add(Calendar.MINUTE, 1);
        }
        return calendar.getTime();
    }

    public static Date getCurrentTimeBegin(int hour) {
        Date hourEnd = getCurrentTimeEnd(hour);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(hourEnd);
        calendar.add(Calendar.HOUR, -hour);
        return calendar.getTime();
    }


    public static List<String> getCurrentTimeXLabel(int hour) {
        int timeInterval = 0;
        switch (hour) {
            case 1:
                timeInterval = 5;
                break;
            case 6:
                timeInterval = 30;
                break;
            case 12:
                timeInterval = 60;
                break;
        }
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        List<String> timeLabel = new ArrayList<>();
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(getCurrentTimeBegin(hour));
        timeLabel.add(df.format(startCalendar.getTime()));
        for (int i = 0; i < 12; i++) {
            startCalendar.add(Calendar.MINUTE, timeInterval);
            timeLabel.add(df.format(startCalendar.getTime()));
        }
        return timeLabel;
    }
    //****************************************************************************************

    //获取hour小时的开始时间
    public static Date getHourBegin(int hour) {
        Date date = getHourTime(getHourEnd(), hour, "-");
        return date;
    }

    //获取hour的结束时间
    public static Date getHourEnd() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        if ("00:00".equals(sdf.format(date))) {
            date = getHourTime(new Date(), 0, "=");
        } else {
            date = getHourTime(new Date(), 1, "+");
        }
        return date;
    }

    //获取本日的开始时间
    public static Date getDayBegin() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    //获取本日的结束时间.认为是明日的00:00:00
    public static Date getDayEnd() {
        Calendar cal = new GregorianCalendar();
//        cal.set(Calendar.HOUR_OF_DAY, 23);
//        cal.set(Calendar.MINUTE, 59);
//        cal.set(Calendar.SECOND, 59);
        cal.setTime(getDayBegin());
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    //获取一周的开始时间
    public static Date getBeginDayOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -6);
        return getDayTime(cal.getTime());
    }

    //获取一周的结束时间
    public static Date getEndDayOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        return getDayTime(cal.getTime());
    }

    //获取一月的开始时间
    public static Date getBeginDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        cal.add(Calendar.MONTH, -1);
        return getDayTime(cal.getTime());
    }

    //获取一月的结束时间
    public static Date getEndDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        return getDayTime(cal.getTime());
    }


    //获取一年的开始时间
    public static Date getBeginDayOfYear() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.YEAR, -1);
        return getDayTime(cal.getTime());
    }

    //获取一年的结束时间
    public static Date getEndDayOfYear() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return getDayTime(cal.getTime());
    }

    public static Timestamp getDayTime(Date d) {
        Calendar calendar = Calendar.getInstance();
        if (null != d) calendar.setTime(d);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Timestamp(calendar.getTimeInMillis());
    }

    //获取当前是几点
    public static int getNowHour() {
        Calendar calendar = Calendar.getInstance();
        int curHour24 = calendar.get(Calendar.HOUR_OF_DAY);  //24小时制
        return curHour24;
    }

    //获取指定时间n个小时整点时间
    public static Date getHourTime(Date date, int hour, String direction) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.set(Calendar.MINUTE, 0);
        ca.set(Calendar.SECOND, 0);
        switch (direction) {
            case "+":
                ca.set(Calendar.HOUR_OF_DAY, ca.get(Calendar.HOUR_OF_DAY) + hour);
                break;
            case "-":
                ca.set(Calendar.HOUR_OF_DAY, ca.get(Calendar.HOUR_OF_DAY) - hour);
                break;
            case "=":
                ca.set(Calendar.HOUR_OF_DAY, ca.get(Calendar.HOUR_OF_DAY));
                break;
            default:
                ca.set(Calendar.HOUR_OF_DAY, ca.get(Calendar.HOUR_OF_DAY));
        }

        date = ca.getTime();
        return date;
    }

    //获取1，6，12h的X轴标签
    public static List<String> getHourXLabel(int hour) {
        int timeSpace = 5 * hour;
        SimpleDateFormat secondFormat = new SimpleDateFormat("HH:mm");
        List<String> timeLabel = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtil.getHourBegin(hour));
        timeLabel.add(secondFormat.format(DateUtil.getHourBegin(hour)));
        for (int i = 0; i < 12; i++) {
            calendar.add(Calendar.MINUTE, timeSpace);
            timeLabel.add(secondFormat.format(calendar.getTime()));
        }
        return timeLabel;
    }

    //获取不大于7天的X轴标签
    public static List<String> getCustomNGT7DayXLabel(Date startTime, Date endTime, int timeSpace) {
        SimpleDateFormat xLabelFormat = new SimpleDateFormat("MM-dd HH");
        SimpleDateFormat paramsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<String> timeLabels = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        while (calendar.getTimeInMillis() < endTime.getTime()) {
            timeLabels.add(xLabelFormat.format(calendar.getTime()));
            calendar.add(Calendar.HOUR, timeSpace);
        }
        timeLabels.add(xLabelFormat.format(calendar.getTime()));
        labelEndTime = paramsFormat.format(calendar.getTime());
        return timeLabels;
    }

    public static List<AxisValue> getCustomNGT7DayAxisValue(String startTime, String endTime) {
        SimpleDateFormat paramsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = null;
        Date endDate = null;
        int timeSpace = 0;
        int totalPoint = 0;
        int spaceTime = 0;
        try {
            startDate = paramsFormat.parse(startTime);
            endDate = paramsFormat.parse(endTime);
            long interval = endDate.getTime() - startDate.getTime();
            if (interval >= 0 && interval <= 12 * 60 * 60 * 1000) {
                timeSpace = 1;
            } else if (interval <= 24 * 60 * 60 * 1000) {
                timeSpace = 2;
            } else if (interval <= 3 * 24 * 60 * 60 * 1000) {
                timeSpace = 6;
            } else if (interval <= 7 * 24 * 60 * 60 * 1000) {
                timeSpace = 12;
            } else if (interval / (60 * 60 * 1000) <= 30 * 24) {
                return getCustomGTWeekXAxis(startDate, endDate);
            } else if (interval / (60 * 60 * 1000) <= 180 * 24) {
                return getCustomGTMonthXAxis(startDate, endDate);
            } else if (interval / (60 * 60 * 1000) <= 730 * 24) {
                return getCustomGT6MonthXAxis(startDate, endDate);
            }
            spaceTime = timeSpace * 60;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<String> xLabel = getCustomNGT7DayXLabel(startDate, endDate, timeSpace);
        try {
            totalPoint = (int) (paramsFormat.parse(labelEndTime).getTime() - startDate.getTime()) / 60 / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<AxisValue> axisValues = new ArrayList<>();
        for (int i = 0; i <= totalPoint; i += spaceTime) {
            AxisValue axisValue = new AxisValue(i);
            axisValue.setLabel(xLabel.get(i / spaceTime));
            axisValues.add(axisValue);
        }
        return axisValues;
    }

    public static List<AxisValue> getDayXAxisLabel() {
        String[] labels = new String[]{"00:00", "02:00", "04:00", "06:00", "08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00", "22:00", "24:00"};
//        String[] labels = new String[]{"00", "02", "04", "06", "08", "10", "12", "14", "16", "18", "20", "22", "24"};
        List<AxisValue> axisValues = new ArrayList<>();
        for (int i = 0; i <= 1440; i += 120) {
            AxisValue axisValue = new AxisValue(i);
            axisValue.setLabel(labels[i / 120]);
            axisValues.add(axisValue);
        }
        return axisValues;
    }

    public static List<AxisValue> getHourXAxisValue(int hour) {
        int totalPoint = 60 * hour;
        int spaceTIme = 5 * hour;
        List<AxisValue> axisValues = new ArrayList<>();
//        List<String> labelList = DateUtil.getHourXLabel(hour);
        List<String> labelList = DateUtil.getCurrentTimeXLabel(hour);  //修改后的标签
        for (int i = 0; i <= totalPoint; i += spaceTIme) {
            AxisValue axisValue = new AxisValue(i);
            axisValue.setLabel(labelList.get(i / spaceTIme));
            axisValues.add(axisValue);
        }
        return axisValues;
    }

    public static String getCustomDeltaTime(String startTime, String endTime) {
        SimpleDateFormat paramsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = null;
        Date endDate = null;
        String deltaTime = null;
        try {
            startDate = paramsFormat.parse(startTime);
            endDate = paramsFormat.parse(endTime);
            long interval = (endDate.getTime() - startDate.getTime()) / (60 * 60 * 1000);
            if (interval <= 24) {
                deltaTime = "60";   //1min
            } else if (interval <= 3 * 24) {
                deltaTime = "180";  //3min
            } else if (interval <= 7 * 24) {
                deltaTime = "420";  //7min
            } else if (interval <= 30 * 24) {
                deltaTime = "1800";
            } else if (interval <= 180 * 24) {
                deltaTime = "10800";
            } else if (interval <= 365 * 24) {
                deltaTime = "21900";
            } else if (interval <= 730 * 24) {
                deltaTime = "43800";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return deltaTime;
    }

    public static String getTimeInterval(String selectedTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = null;
        String startTime = null;
        String endTime = null;
        switch (selectedTime) {
            case "最近1小时":
//                startTime = sdf.format(DateUtil.getHourBegin(1));
//                endTime = sdf.format(DateUtil.getHourEnd());
                startTime = sdf.format(DateUtil.getCurrentTimeBegin(1));
                endTime = sdf.format(DateUtil.getCurrentTimeEnd(1));
                time = startTime + "~" + endTime;
                break;
            case "最近6小时":
//                startTime = sdf.format(DateUtil.getHourBegin(6));
//                endTime = sdf.format(DateUtil.getHourEnd());
                startTime = sdf.format(DateUtil.getCurrentTimeBegin(6));
                endTime = sdf.format(DateUtil.getCurrentTimeEnd(6));
                time = startTime + "~" + endTime;
                break;
            case "最近12小时":
//                startTime = sdf.format(DateUtil.getHourBegin(12));
//                endTime = sdf.format(DateUtil.getHourEnd());
                startTime = sdf.format(DateUtil.getCurrentTimeBegin(12));
                endTime = sdf.format(DateUtil.getCurrentTimeEnd(12));
                time = startTime + "~" + endTime;
                break;
            case "本日":
                startTime = sdf.format(DateUtil.getDayBegin());
                endTime = sdf.format(DateUtil.getDayEnd());
                time = startTime + "~" + endTime;
                break;
            case "一周":
                startTime = sdf.format(DateUtil.getBeginDayOfWeek());
                endTime = sdf.format(DateUtil.getEndDayOfWeek());
                time = startTime + "~" + endTime;
                break;
            case "一月":
                startTime = sdf.format(DateUtil.getBeginDayOfMonth());
                endTime = sdf.format(DateUtil.getEndDayOfMonth());
                time = startTime + "~" + endTime;
                break;
            case "一年":
                startTime = sdf.format(DateUtil.getBeginDayOfYear());
                endTime = sdf.format(DateUtil.getEndDayOfYear());
                time = startTime + "~" + endTime;
                break;
        }
        return time;
    }

    //获取间隔小时数
    public static int calcHourOffset(String startTime, String endTime) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        LogUtil.e("calOffset", "startTime ：" + startTime + ", endTime: " + endTime);
        try {
            startCal.setTime(df.parse(startTime));
            endCal.setTime(df.parse(endTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (int) ((endCal.getTimeInMillis() - startCal.getTimeInMillis()) / 60 / 1000 / 60);
    }

/////////////////////////////////////////////////////////////////

    public static int getYear() {
        return cal.get(Calendar.YEAR);
    }

    public static int getMonth() {
        return cal.get(Calendar.MONTH) + 1;
    }

    public static int getDay() {
        return cal.get(Calendar.DATE);
    }

    public static boolean isLeapYear(int year) {
        return ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0);
    }

    public static List<AxisValue> getWeekXAxis() {
        List<AxisValue> xAxis = new ArrayList<>();
        String[] labels = new String[]{"周一 00:00", "周一 12:00", "周二 00:00", "周二 12:00", "周三 00:00", "周三 12:00", "周四 00:00", "周四 12:00", "周五 00:00", "周五 12:00", "周六 00:00", "周六 12:00", "周日 00:00", "周日 12:00", "周日 24:00"};
        for (int i = 0, j = 0; i <= 10080; i += 720) {
            AxisValue axisValue = new AxisValue(i);
            axisValue.setLabel(labels[j++]);
            xAxis.add(axisValue);
        }
        return xAxis;
    }

    public static List<AxisValue> getMonthXAxis() {
        List<AxisValue> xAxis = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        String normalSuffix = "号 00:00";
        String lastSuffix = "号 24:00";
        switch (getMonthType()) {
            case TYPE_28:
                for (int i = 0; i < TYPE_28; i++) {
                    labels.add((i + 1) + normalSuffix);
                }
                labels.add(TYPE_28 + lastSuffix);
                for (int i = 0, j = 0; i <= (TYPE_28 * 1440); i += 1440) {
                    AxisValue axisValue = new AxisValue(i);
                    axisValue.setLabel(labels.get(j++));
                    xAxis.add(axisValue);
                }
                break;
            case TYPE_29:
                for (int i = 0; i < TYPE_29; i++) {
                    labels.add((i + 1) + normalSuffix);
                }
                labels.add(TYPE_29 + lastSuffix);
                for (int i = 0, j = 0; i <= (TYPE_29 * 1440); i += 1440) {
                    AxisValue axisValue = new AxisValue(i);
                    axisValue.setLabel(labels.get(j++));
                    xAxis.add(axisValue);
                }
                break;
            case TYPE_30:
                for (int i = 0; i < TYPE_30; i++) {
                    labels.add((i + 1) + normalSuffix);
                }
                labels.add(TYPE_30 + lastSuffix);
                for (int i = 0, j = 0; i <= (TYPE_30 * 1440); i += 1440) {
                    AxisValue axisValue = new AxisValue(i);
                    Log.e("Month", "  " + i);
                    Log.e("Month", "  " + labels.get(j));
                    axisValue.setLabel(labels.get(j++));
                    xAxis.add(axisValue);
                }
                break;
            case TYPE_31:
                for (int i = 0; i < TYPE_31; i++) {
                    labels.add((i + 1) + normalSuffix);
                }
                labels.add(TYPE_31 + lastSuffix);
                for (int i = 0, j = 0; i <= (TYPE_31 * 1440); i += 1440) {
                    AxisValue axisValue = new AxisValue(i);
                    axisValue.setLabel(labels.get(j++));
                    xAxis.add(axisValue);
                }
                break;
        }
        return xAxis;
    }

    public static List<AxisValue> getYearXAxis() {
        List<AxisValue> xAxis = new ArrayList<>();
        String[] labels = new String[]{"1月1日 00:00", "2月1日 00:00", "3月1日 00:00", "4月1日 00:00", "5月1日 00:00", "6月1日 00:00", "7月1日 00:00", "8月1日 00:00", "9月1日 00:00", "10月1日 00:00", "11月1日 00:00", "12月1日 00:00", "12月31日 24:00"};
        int interval;
        if (isLeapYear(getYear())) {
            for (int i = 0, j = 0; i <= (1440 * 366); i += interval) {
                AxisValue axisValue = new AxisValue(i);
                axisValue.setLabel(labels[j++]);
                xAxis.add(axisValue);
                if (monthList_30.contains(j)) {
                    interval = 30 * 1440;
                } else if (monthList_31.contains(j)) {
                    interval = 31 * 1440;
                } else {
                    interval = 29 * 1440;
                }
            }
        } else {
            for (int i = 0, j = 0; i <= (1440 * 365); i += interval) {
//                Log.e("Year", "  " + i );
//                Log.e("Year", "  " + labels[j]);
                AxisValue axisValue = new AxisValue(i);
                axisValue.setLabel(labels[j++]);
                xAxis.add(axisValue);
                if (monthList_30.contains(j)) {
                    interval = 30 * 1440;
                } else if (monthList_31.contains(j)) {
                    interval = 31 * 1440;
                } else {
                    interval = 28 * 1440;
                }
            }
        }
        return xAxis;
    }

    /**
     * @return int
     * @description 返回月份天数类型, 28天为 TYPE_28,29天为TYPE_29,30天为TYPE_30,31天为TYPE_31
     */
    private static int getMonthType() {
        //平年
        if (!isLeapYear(getYear())) {
            if (monthList_30.contains(getMonth())) {
                return TYPE_30;
            } else if (monthList_31.contains(getMonth())) {
                return TYPE_31;
            } else {
                return TYPE_28;
            }
        } else {
            if (monthList_30.contains(getMonth())) {
                return TYPE_30;
            } else if (monthList_31.contains(getMonth())) {
                return TYPE_31;
            } else {
                return TYPE_29;
            }
        }
    }

///////////////////////////////////////////////////////////////////////////////////////

    public static List<AxisValue> getRecentWeekXAxis() {
        List<AxisValue> xAxis = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.CHINA);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        Date date = calendar.getTime();
        labels.add(sdf.format(date) + " 00:00");
        calendar.add(Calendar.DATE, -1);
        date = calendar.getTime();
        for (int i = 0; i < 14; i++) {
            if (i % 2 == 0) {
                labels.add(sdf.format(date) + " 12:00");
            } else {
                labels.add(sdf.format(date) + " 00:00");
                calendar.add(Calendar.DATE, -1);
            }
            date = calendar.getTime();
        }
        for (int i = 0, j = 14; i <= 10080; i += 720) {
            AxisValue axisValue = new AxisValue(i);
            axisValue.setLabel(labels.get(j--));
            xAxis.add(axisValue);
        }
        return xAxis;
    }

    public static List<AxisValue> getRecentMonthXAxis() {
        List<AxisValue> xAxis = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.CHINA);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        Date date = calendar.getTime();
        calendar.add(Calendar.MONTH, -1);

        Date dateBefore = calendar.getTime();
        int days = calcDayOffset(dateBefore, date);
        for (int i = 0; i <= days; i++) {
            labels.add(sdf.format(date));
            calendar.setTime(date);
            calendar.add(Calendar.DATE, -1);
            date = calendar.getTime();
        }
        for (int i = 0, j = days; i <= days * 1440; i += 1440) {
            AxisValue axisValue = new AxisValue(i);
            axisValue.setLabel(labels.get(j--));
            xAxis.add(axisValue);
        }
        return xAxis;
    }

    public static List<AxisValue> getRecentYearXAxis() {
        int interval = 0;
        List<AxisValue> xAxis = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> intervals = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date present = calendar.getTime();
        for (int i = 0; i <= 12; i++) {
            labels.add(sdf.format(present));
//            LogUtil.e("生成的标签", labels.get(i));
            calendar.add(Calendar.MONTH, -1);
            intervals.add(calcDayOffset(calendar.getTime(), present));
            present = calendar.getTime();
        }
        for (Integer integer : intervals) {
//            LogUtil.e("生成的间隔值", "=========" + integer + "============");
        }
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.MONTH, 1);
        cal2.set(Calendar.DAY_OF_MONTH, 1);
        Date date2 = cal2.getTime();
        cal2.add(Calendar.YEAR, -1);
        Date date1 = cal2.getTime();
        int totalXCount = calcDayOffset(date1, date2) * 1440;
        int tick = 12;
        for (long i = 0; i <= totalXCount; i += 1440 * interval) {
//            LogUtil.e("值和标签", "value : " + i + ", labels: " + labels.get(tick) + " , tick=" + tick);
            interval = (tick == 0) ? 1 : intervals.get(tick - 1);
//            LogUtil.e("interval", "" + interval);
            AxisValue axisValue = new AxisValue(i);
            axisValue.setLabel(labels.get(tick--));
            xAxis.add(axisValue);
        }
        return xAxis;
    }

    public static List<AxisValue> getCustomGTWeekXAxis(Date start, Date end) {
        List<AxisValue> xAxis = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH", Locale.CHINA);
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(start);
        Date date1 = cal1.getTime();
        cal2.setTime(end);
        if (cal1.get(Calendar.HOUR_OF_DAY) != cal2.get(Calendar.HOUR_OF_DAY)) {
            end.setTime(end.getTime() + 1000 * 60 * 60 * 24);
            cal2.setTime(end);
            cal2.set(Calendar.HOUR_OF_DAY, cal1.get(Calendar.HOUR_OF_DAY));
        }
        Date date2 = cal2.getTime();
        int dayGap = calcDayOffset(date1, date2);
        for (int i = 0; i <= dayGap; i++) {
            labels.add(sdf.format(date2));
            cal2.add(Calendar.DAY_OF_YEAR, -1);
            date2 = cal2.getTime();
        }
        cal2.setTime(end);
        cal2.set(Calendar.HOUR_OF_DAY, cal1.get(Calendar.HOUR_OF_DAY));
        int totalXCount = calcDayOffset(date1, cal2.getTime()) * 1440;
        for (int i = 0, j = dayGap; i <= totalXCount; i += 1440) {
//            LogUtil.e("值和标签", "value : " + i + ", labels: " + labels.get(j) + " , j=" + j);
            AxisValue axisValue = new AxisValue(i);
            axisValue.setLabel(labels.get(j--));
            xAxis.add(axisValue);
        }

        offSet = 0;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        labelEndTime = df.format(cal2.getTime());
        return xAxis;
    }

    public static List<AxisValue> getCustomGTMonthXAxis(Date start, Date end) {
        int interval = 0;
        List<AxisValue> xAxis = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> intervals = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(start);
        cal1.set(Calendar.DAY_OF_MONTH, 1);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        Date date1 = cal1.getTime();
        cal2.setTime(end);
        cal2.add(Calendar.MONTH, 1);
        cal2.set(Calendar.DAY_OF_MONTH, 1);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        Date date2 = cal2.getTime();

        int monthGap = (cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR)) * 12 + (cal2.get(Calendar.MONTH) - cal1.get(Calendar.MONTH));

        for (int i = 0; i <= monthGap * 2; i++) {
            labels.add(sdf.format(date2));
//            LogUtil.e("生成的标签", labels.get(i));
            if (i % 2 == 0) {
                cal2.add(Calendar.MONTH, -1);
                cal2.set(Calendar.DAY_OF_MONTH, 15);
            } else {
                cal2.set(Calendar.DAY_OF_MONTH, 1);
            }
            intervals.add(calcDayOffset(cal2.getTime(), date2));
            date2 = cal2.getTime();
        }
        cal2.setTime(end);
        cal2.add(Calendar.MONTH, 1);
        cal2.set(Calendar.DAY_OF_MONTH, 1);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        int totalXCount = calcDayOffset(date1, cal2.getTime()) * 1440;
//        LogUtil.e("总点数", totalXCount + "");
//        for (Integer integer : intervals) {
//            LogUtil.e("生成的间隔值", "========="+integer+"============");
//        }
        LogUtil.e("Time", "startTime" + start + ", cal1Time: " + cal1.getTime());
        offSet = -(start.getTime() - cal1.getTimeInMillis()) / (1000 * 60);
        int tick = monthGap * 2;
        for (long i = offSet; i <= totalXCount + offSet; i += 1440 * interval) {
//            LogUtil.e("值和标签", "value : " + i + ", labels: " + labels.get(tick) + " , tick=" + tick);
            interval = (tick == 0) ? 1 : intervals.get(tick - 1);
//            LogUtil.e("interval","" + interval);
            AxisValue axisValue = new AxisValue(i);
            axisValue.setLabel(labels.get(tick--));
            xAxis.add(axisValue);
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
//        cal2.add(Calendar.DAY_OF_YEAR, 15);
        labelEndTime = df.format(cal2.getTime());
        return xAxis;
    }

    public static List<AxisValue> getCustomGT6MonthXAxis(Date start, Date end) {
        int interval = 0;
        List<AxisValue> xAxis = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> intervals = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(start);
        cal1.set(Calendar.DAY_OF_MONTH, 1);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        Date date1 = cal1.getTime();
        cal2.setTime(end);
        cal2.add(Calendar.MONTH, 1);
        cal2.set(Calendar.DAY_OF_MONTH, 1);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        Date date2 = cal2.getTime();

        int monthGap = (cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR)) * 12 + (cal2.get(Calendar.MONTH) - cal1.get(Calendar.MONTH));
        offSet = -(start.getTime() - cal1.getTimeInMillis()) / (1000 * 60);
        for (int i = 0; i <= monthGap; i++) {
            labels.add(sdf.format(date2));
            cal2.add(Calendar.MONTH, -1);
            intervals.add(calcDayOffset(cal2.getTime(), date2));
            date2 = cal2.getTime();
        }
        cal2.setTime(end);
        cal2.add(Calendar.MONTH, 1);
        cal2.set(Calendar.DAY_OF_MONTH, 1);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        int totalXCount = calcDayOffset(date1, cal2.getTime()) * 1440;
        int tick = monthGap;
        for (long i = offSet; i <= totalXCount + offSet; i += 1440 * interval) {
            interval = (tick == 0) ? 1 : intervals.get(tick - 1);
            AxisValue axisValue = new AxisValue(i);
            axisValue.setLabel(labels.get(tick--));
            xAxis.add(axisValue);
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        labelEndTime = df.format(cal2.getTime());
        return xAxis;
    }

    /**
     * @param date1 起始日期
     * @param date2 结束日期
     * @return 天数
     */
    public static int calcDayOffset(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        int day1 = cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);
        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        if (year1 != year2) {
            int timeDistance = 0;
            for (int i = year1; i < year2; i++) {
                if (isLeapYear(i)) {  //闰年
                    timeDistance += 366;
                } else {
                    timeDistance += 365;
                }
            }
            return timeDistance + (day2 - day1);
        } else {
            return day2 - day1;
        }
    }
}
