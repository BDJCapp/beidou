package com.beyond.beidou;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void listTest(){
        List<TestBean> list = new ArrayList<>();
        TestBean testBean4 = new TestBean("ty312-ASasd","4946848-asd","7998-asd","A01","3");
        TestBean testBean5 = new TestBean("23312-ASasd","4946848-asd","7898-asd","a01","1");
        TestBean testBean1 = new TestBean("21312-ASasd","ty46848-asd","4898-asd","a01","1");
        TestBean testBean2 = new TestBean("89312-ASasd","8546848-asd","u898-asd","59","2");
        TestBean testBean3 = new TestBean("ht312-ASasd","45ty6848-asd","h898-asd","sdaw","2");

        list.add(testBean1);
        list.add(testBean2);
        list.add(testBean3);
        list.add(testBean4);
        list.add(testBean5);


//        Collections.sort(list, new Comparator<TestBean>(){
//            public int compare(TestBean arg0, TestBean arg1) {
//                return arg0.getDeviceUUID().compareTo(arg1.getDeviceUUID());
//            }
//        });

        ListUtil.sort(list,true,"StationName","StationUUID");


        for (TestBean bean : list) {
            System.out.println(bean);
        }
    }


}