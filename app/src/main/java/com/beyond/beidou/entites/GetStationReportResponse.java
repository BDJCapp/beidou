package com.beyond.beidou.entites;

import java.util.List;


public class GetStationReportResponse {

    /**
     * ResponseCode : 200
     * ResponseMsg : 操作成功
     * StationGNSSReport : {"ProjectName":"系统测试工程","Supervisor":"平台系统","StationName":"监测点名称","StationAddress":"北京","Title":["Date","Time","StationRelativeCoordinatesWGS84['N','E','H']","HorizontalDisplacement['Period','Cumulative']","VerticalDisplacement['Period','Cumulative']","DirectionAngle","BaseStationPositionWUSONG['N','E','H']","MobileStationPositionWUSONG['N','E','H']","DeviceID","DataFileName","FileSize","CumulativeDisplacementN['M','MM']","CumulativeDisplacementE['M','MM']","CumulativeDirectionAngle","RelativeCoordinatesWGS84['N0','E0','H0']","BaseStationPositionWGS84['N','E','H']","MobileStationPositionWGS84['N','E','H']"],"Daily":[["20210309","02:59:55",[880509.958,-211492.1539,-35.7944],[0,0],[0,0],0,[350033.1181,349855.4802,81.4878],[1230543.0761,138363.3263,45.6934],"设备序列号","","",[0,0],[0,0],0,[880509.958,-211492.1539,-35.7944],[3550461.99,404968.825,85.784],[4430971.948,193476.6711,49.9896]]]}
     * ReportFilePath : http://172.18.7.86/dist/API/../files/reports/station/6159529a-6bc3-4c73-84d1-e59f6f60ece6/6159529a-6bc3-4c73-84d1-e59f6f60ece6_1615219200_1615305599.xls
     */

    private String ResponseCode;
    private String ResponseMsg;
    private StationGNSSReportBean StationGNSSReport;
    private String ReportFilePath;

    public String getResponseCode() {
        return ResponseCode;
    }

    public void setResponseCode(String ResponseCode) {
        this.ResponseCode = ResponseCode;
    }

    public String getResponseMsg() {
        return ResponseMsg;
    }

    public void setResponseMsg(String ResponseMsg) {
        this.ResponseMsg = ResponseMsg;
    }

    public StationGNSSReportBean getStationGNSSReport() {
        return StationGNSSReport;
    }

    public void setStationGNSSReport(StationGNSSReportBean StationGNSSReport) {
        this.StationGNSSReport = StationGNSSReport;
    }

    public String getReportFilePath() {
        return ReportFilePath;
    }

    public void setReportFilePath(String ReportFilePath) {
        this.ReportFilePath = ReportFilePath;
    }

    public static class StationGNSSReportBean {
        /**
         * ProjectName : 系统测试工程
         * Supervisor : 平台系统
         * StationName : 监测点名称
         * StationAddress : 北京
         * Title : ["Date","Time","StationRelativeCoordinatesWGS84['N','E','H']","HorizontalDisplacement['Period','Cumulative']","VerticalDisplacement['Period','Cumulative']","DirectionAngle","BaseStationPositionWUSONG['N','E','H']","MobileStationPositionWUSONG['N','E','H']","DeviceID","DataFileName","FileSize","CumulativeDisplacementN['M','MM']","CumulativeDisplacementE['M','MM']","CumulativeDirectionAngle","RelativeCoordinatesWGS84['N0','E0','H0']","BaseStationPositionWGS84['N','E','H']","MobileStationPositionWGS84['N','E','H']"]
         * Daily : [["20210309","02:59:55",[880509.958,-211492.1539,-35.7944],[0,0],[0,0],0,[350033.1181,349855.4802,81.4878],[1230543.0761,138363.3263,45.6934],"设备序列号","","",[0,0],[0,0],0,[880509.958,-211492.1539,-35.7944],[3550461.99,404968.825,85.784],[4430971.948,193476.6711,49.9896]]]
         */

        private String ProjectName;
        private String Supervisor;
        private String StationName;
        private String StationAddress;
        private List<String> Title;
        private List<List<String>> Daily;

        public String getProjectName() {
            return ProjectName;
        }

        public void setProjectName(String ProjectName) {
            this.ProjectName = ProjectName;
        }

        public String getSupervisor() {
            return Supervisor;
        }

        public void setSupervisor(String Supervisor) {
            this.Supervisor = Supervisor;
        }

        public String getStationName() {
            return StationName;
        }

        public void setStationName(String StationName) {
            this.StationName = StationName;
        }

        public String getStationAddress() {
            return StationAddress;
        }

        public void setStationAddress(String StationAddress) {
            this.StationAddress = StationAddress;
        }

        public List<String> getTitle() {
            return Title;
        }

        public void setTitle(List<String> Title) {
            this.Title = Title;
        }

        public List<List<String>> getDaily() {
            return Daily;
        }

        public void setDaily(List<List<String>> Daily) {
            this.Daily = Daily;
        }
    }
}
