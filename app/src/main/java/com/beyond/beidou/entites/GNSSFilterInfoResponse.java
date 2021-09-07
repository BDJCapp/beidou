package com.beyond.beidou.entites;

import com.beyond.beidou.util.LogUtil;

import java.util.List;


public class GNSSFilterInfoResponse {


    /**
     * ResponseCode : 200
     * ResponseMsg : 操作成功
     * Data : {"Interval":48,"MaxInterval":95}
     * Title : ["DataTimestamp","GNSSFilterInfoN","GNSSFilterInfoE","GNSSFilterInfoH","GNSSFilterInfoDeltaD","GNSSFilterInfoDeltaH"]
     * Initial : [1616377403,"","",""]
     * Content : [[1615219435,"+4430971.9449","+193476.6726","+49.9973","0.010962207519873","-0.0167"],[1615219495,"+4430971.9462","+193476.6738","+49.9983","0.012731064504953","-0.015700000000002"]]
     * Min : [1616377403,"+4430971.9319","+193476.6618","+49.9699","0.00050000021001326","-0.0441"]
     * Max : [1616377403,"+4430971.9522","+193476.6785","+50.0142","0.017130382591823","0.00019999999999953"]
     * Average : [1616377403,"4430971.9430","193476.6704","49.9944","0.0088","-0.0196"]
     */

    private String ResponseCode;
    private String ResponseMsg;
    private DataBean Data;
    private List<String> Title;
    private List<Object> Initial;
    private List<List<Object>> Content;
    private List<Object> Min;
    private List<Object> Max;
    private List<Object> Average;

    public String getResponseCode() {
        return ResponseCode;
    }

    public void setResponseCode(String responseCode) {
        ResponseCode = responseCode;
    }

    public String getResponseMsg() {
        return ResponseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        ResponseMsg = responseMsg;
    }

    public DataBean getData() {
        return Data;
    }

    public void setData(DataBean data) {
        Data = data;
    }

    public List<String> getTitle() {
        return Title;
    }

    public void setTitle(List<String> title) {
        Title = title;
    }

    public List<Object> getInitial() {
        return Initial;
    }

    public void setInitial(List<Object> initial) {
        Initial = initial;
    }

    public List<List<Object>> getContent() {
        return Content;
    }

    public void setContent(List<List<Object>> content) {
        Content = content;
    }

    public List<Object> getMin() {
        return Min;
    }

    public void setMin(List<Object> min) {
        Min = min;
    }

    public List<Object> getMax() {
        return Max;
    }

    public void setMax(List<Object> max) {
        Max = max;
    }

    public List<Object> getAverage() {
        return Average;
    }

    public void setAverage(List<Object> average) {
        Average = average;
    }

    public static class DataBean {
        /**
         * Interval : 48
         * MaxInterval : 95
         */

        private int Interval;
        private int MaxInterval;

        public int getInterval() {
            return Interval;
        }

        public void setInterval(int Interval) {
            this.Interval = Interval;
        }

        public int getMaxInterval() {
            return MaxInterval;
        }

        public void setMaxInterval(int MaxInterval) {
            this.MaxInterval = MaxInterval;
        }
    }
}
