package model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TemperatureData {

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm");
    private String timeStamp;
    private String temperature;
    private String sourceName;
    private String humidity;
    private Date date;
    private Date time;
    private String strTime;
    private String weekDay;
    private int week;
    private int month;
    private int year;

    public TemperatureData(String timeStamp, String temperature, String sourceName, String humidity){

        SimpleDateFormat dayFormatter = new SimpleDateFormat("EEEE");
        SimpleDateFormat weekFormatter = new SimpleDateFormat("w");
        SimpleDateFormat monthFormatter = new SimpleDateFormat("MM");
        SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy");

        this.timeStamp = timeStamp;
        this.temperature = String.format("%.1f", Float.parseFloat(temperature));
        this.sourceName = sourceName;
        this.humidity = String.format("%.1f", Float.parseFloat(humidity));

        String[] split = timeStamp.split(",");

        try {
            this.date = dateFormatter.parse(split[0]);
            this.time = timeFormatter.parse(split[1]);
        } catch (ParseException e){
            e.printStackTrace();
        }
        this.strTime = split[1].substring(0, 5);
        this.weekDay = dayFormatter.format(this.date);
        this.week = Integer.parseInt(weekFormatter.format(this.date));
        this.month = Integer.parseInt(monthFormatter.format(this.date));
        this.year = Integer.parseInt(yearFormatter.format(this.date));

    }

    public String getValue(boolean getTemp){
        if(getTemp)
            return temperature;
        else
            return humidity;
    }

    public String getSourceName() { return sourceName; }

    public String  getTemperature() { return temperature; }

    public String getTimeStamp() { return timeStamp; }

    public String getHumidity() { return humidity; }

    public Date getDate() { return date; }

    public String getDateAsString() { return dateFormatter.format(date); }

    public Date getTime() { return time; }

    public int getWeek() { return week; }

    public int getMonth() { return month; }

    public int getYear() { return year; }

    public String getTimeAsString() { return strTime;}
}
