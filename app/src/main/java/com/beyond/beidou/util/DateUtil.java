package com.beyond.beidou.util;

import android.animation.FloatEvaluator;
import android.util.Log;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.PointValue;


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

    //获取本日的结束时间
    public static Date getDayEnd() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    //获取本周的开始时间
    public static Date getBeginDayOfWeek() {
        Date date = new Date();
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayofweek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayofweek == 1) {
            dayofweek += 7;
        }
        cal.add(Calendar.DATE, 2 - dayofweek);
        return getDayStartTime(cal.getTime());
    }

    //获取本周的结束时间
    public static Date getEndDayOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getBeginDayOfWeek());
        cal.add(Calendar.DAY_OF_WEEK, 6);
        Date weekEndSta = cal.getTime();
        return getDayEndTime(weekEndSta);
    }

    //获取本月的开始时间
    public static Date getBeginDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth() - 1, 1);
        return getDayStartTime(calendar.getTime());
    }

    //获取本月的结束时间
    public static Date getEndDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth() - 1, 1);
        int day = calendar.getActualMaximum(5);
        calendar.set(getNowYear(), getNowMonth() - 1, day);
        return getDayEndTime(calendar.getTime());
    }


    //获取本年的开始时间
    public static Date getBeginDayOfYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, getNowYear());
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        return getDayStartTime(cal.getTime());
    }

    //获取本年的结束时间
    public static Date getEndDayOfYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, getNowYear());
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DATE, 31);
        return getDayEndTime(cal.getTime());
    }

    //获取某个日期的开始时间
    public static Timestamp getDayStartTime(Date d) {
        Calendar calendar = Calendar.getInstance();
        if (null != d) calendar.setTime(d);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Timestamp(calendar.getTimeInMillis());
    }

    //获取某个日期的结束时间
    public static Timestamp getDayEndTime(Date d) {
        Calendar calendar = Calendar.getInstance();
        if (null != d) calendar.setTime(d);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return new Timestamp(calendar.getTimeInMillis());
    }

    //获取当前是几点
    public static int getNowHour() {
        Calendar calendar = Calendar.getInstance();
        int curHour24 = calendar.get(Calendar.HOUR_OF_DAY);  //24小时制
        return curHour24;
    }

    //获取今年是哪一年
    public static int getNowYear() {
        Date date = new Date();
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        return gc.get(1);
    }

    //获取本月是哪一月
    public static int getNowMonth() {
        Date date = new Date();
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        return gc.get(2) + 1;
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
        SimpleDateFormat secondFormat = new SimpleDateFormat("MM-dd HH:mm");
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

    public static List<AxisValue> getDayXAxisLabel() {
//        String[] labels = new String[]{"00:00", "02:00", "04:00", "06:00", "08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00", "22:00", "24:00"};
        String[] labels = new String[]{"00", "02", "04", "06", "08", "10", "12", "14", "16", "18", "20", "22", "24"};
        List<AxisValue> axisValues = new ArrayList<>();
        for (int i = 0; i <= 1440; i += 120) {
            AxisValue axisValue = new AxisValue(i);
            axisValue.setLabel(labels[i / 120]);
            axisValues.add(axisValue);
        }
        return axisValues;
    }


    public static List<AxisValue> getHourXAxisLabel(int hour) {
        int totalPoint = 60 * hour;
        int spaceTIme = 5 * hour;
        List<AxisValue> axisValues = new ArrayList<>();
        List<String> labelList = DateUtil.getHourXLabel(hour);
        for (int i = 0; i <= totalPoint; i += spaceTIme) {
            AxisValue axisValue = new AxisValue(i);
            axisValue.setLabel(labelList.get(i / spaceTIme));
            axisValues.add(axisValue);
        }
        return axisValues;
    }

    public static String getTimeInterval(String selectedTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        String time = null;
        String startTime = null;
        String endTime = null;
        switch (selectedTime) {
            case "最近1小时":
                //暂时只有3.9号的数据，之后直接把"2021-03-09 "去掉即可。
                startTime = sdf.format(DateUtil.getHourBegin(1));
                endTime = sdf.format(DateUtil.getHourEnd());
                time = startTime + "~" + endTime;
                break;
            case "最近6小时":
                startTime = sdf.format(DateUtil.getHourBegin(6));
                endTime = sdf.format(DateUtil.getHourEnd());
                time = startTime + "~" + endTime;
                break;
            case "最近12小时":
                startTime = sdf.format(DateUtil.getHourBegin(12));
                endTime = sdf.format(DateUtil.getHourEnd());
                time = startTime + "~" + endTime;
                break;
            case "本日":
                startTime = sdf.format(DateUtil.getDayBegin());
                endTime = sdf.format(DateUtil.getDayEnd());
                time = startTime + "~" + endTime;
                break;
            case "本周":
                startTime = sdf.format(DateUtil.getBeginDayOfWeek());
                endTime = sdf.format(DateUtil.getEndDayOfWeek());
                time = startTime + "~" + endTime;
                break;
            case "本月":
                startTime = sdf.format(DateUtil.getBeginDayOfMonth());
                endTime = sdf.format(DateUtil.getEndDayOfMonth());
                time = startTime + "~" + endTime;
                break;
            case "本年":
                startTime = sdf.format(DateUtil.getBeginDayOfYear());
                endTime = sdf.format(DateUtil.getEndDayOfYear());
                time = startTime + "~" + endTime;
                break;
        }
        return time;
    }

/////////////////////////////////////////////////

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
        labels.add(sdf.format(date) + " 24:00");
        for (int i = 0; i < 14; i++) {
            if(i % 2 == 0){
                labels.add(sdf.format(date) + " 12:00");
            }else{
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
        calendar.add(Calendar.DATE, -1);
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
        calendar.add(Calendar.DATE, 1);
        Date present = calendar.getTime();
        calendar.add(Calendar.DATE, -1);
        for (int i = 0; i <= 12; i++) {
            labels.add(sdf.format(present));
            calendar.add(Calendar.MONTH, -1);
            intervals.add(calcDayOffset(calendar.getTime(), present));
            present = calendar.getTime();
        }

        int totalXCount = isLeapYear(getYear() - 1) ? 366 * 1440 : 365 * 1440;
        for (int i = 0, j = 12; i <= totalXCount; i += 1440 * interval) {
            if (j >= 0) {
                interval = intervals.get(12 - j);
                AxisValue axisValue = new AxisValue(i);
                axisValue.setLabel(labels.get(j--));
                xAxis.add(axisValue);
            }
        }
        return xAxis;
    }

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
                if (isLeapYear(getYear() - 1)) {  //闰年
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
