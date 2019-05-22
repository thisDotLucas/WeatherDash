package model;

public class OfficialData {

    String timeStamp;
    String temperature;
    String sourceName;
    String humidity;

    public OfficialData(String timeStamp, String temperature, String sourceName, String humidity){

        this.timeStamp = timeStamp;
        this.temperature = temperature;
        this.sourceName = sourceName;
        this.humidity = humidity;

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

    public String getHumidity() {return humidity; }

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
