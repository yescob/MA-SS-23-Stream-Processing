package at.fhv.streamprocessing.bfn.aggregator.model;

public class NOAARecord {

    private static final Integer START_POSITION_STATION_ID = 5;
    private static final Integer END_POSITION_STATION_ID = 15;
    private static final Integer START_POSITION_YEAR = 16;
    private static final Integer END_POSITION_YEAR = 19;
    private static final Integer START_POSITION_MONTH = 20;
    private static final Integer END_POSITION_MONTH = 22;
    private static final Integer START_POSITION_DAY = 23;
    private static final Integer END_POSITION_DAY = 25;
    private static final Integer START_POSITION_TEMPERATURE = 88;
    private static final Integer END_POSITION_TEMPERATURE = 92;
    private static final Integer POSITION_QUALITY_CODE = 93;
    private static final Integer MISSING_VALUE = 9999;
    private static final String ALLOWED_QUALITY_NUMBERS_REGEX = "[01459]";

    
    private String stationID;
    private String year;
    private String month;
    private String day;
    private Integer temperature;
    private String qualityRecord;

    public NOAARecord(){}

    public NOAARecord(String record){

        stationID = record.substring(START_POSITION_STATION_ID-1, END_POSITION_STATION_ID);
        year = record.substring(START_POSITION_YEAR-1,END_POSITION_YEAR);
        month = record.substring(START_POSITION_MONTH-1,END_POSITION_MONTH);
        day = record.substring(START_POSITION_DAY-1,END_POSITION_DAY);
        temperature = Integer.parseInt(record.substring(START_POSITION_TEMPERATURE-1,END_POSITION_TEMPERATURE));
        qualityRecord = record.substring(POSITION_QUALITY_CODE-1, POSITION_QUALITY_CODE);
    }


    public String getStationID() {
        return this.stationID;
    }

    public void setStationID(String stationID) {
        this.stationID = stationID;
    }

    public String getYear() {
        return this.year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return this.month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return this.day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Integer getTemperature() {
        return this.temperature;
    }

    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }

    public String getQualityRecord() {
        return this.qualityRecord;
    }

    public void setQualityRecord(String qualityRecord) {
        this.qualityRecord = qualityRecord;
    }

}
