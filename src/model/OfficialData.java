package model;

public class OfficialData {

    String timeStamp;
    String temperature;
    String sourceName;

    public OfficialData(String timeStamp, String temperature, String sourceName){

        this.timeStamp = timeStamp;
        this.temperature = temperature;
        this.sourceName = sourceName;

    }

    public String getSourceName() {
        return sourceName;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
