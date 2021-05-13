package com.beyond.beidou;

import com.beyond.beidou.util.DateUtil;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author: 李垚
 * @date: 2021/5/12
 */
public class XLabelTest {

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Test
    public void NotBigger12HourXLabelTest()
    {
        List<String> xLabel = DateUtil.getCustomXLabel("2021-05-08 13:00:00", "2021-05-12 11:00:00");
        for (String s : xLabel) {
            System.out.println(s);
        }
    }

    @Test
    public void get1HourEnd()
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
        Calendar calendar = Calendar.getInstance();
        System.out.println("转换之前" + df.format(calendar.getTime()));
        while (calendar.get(Calendar.MINUTE) % 60 != 0)
            {
            calendar.add(Calendar.MINUTE,1);
        }
        System.out.println("转换之后" + df.format(calendar.getTime()));
    }

    @Test
    public void getCurrentBegin()
    {
        Date hourBegin = DateUtil.getCurrentTimeBegin(6);
        System.out.println("获取的开始时间" + df.format(hourBegin));
    }

    @Test
    public void getCurrentXLabel()
    {
        List<String> hourXLabel = DateUtil.getCurrentTimeXLabel(12);
        for (String s : hourXLabel) {
            System.out.println(s);
        }
    }



}
