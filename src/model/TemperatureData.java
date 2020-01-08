package model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TemperatureData {

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm");
    private String temperature;
    private String sourceName;
    private String humidity;
    private Date date;
    private Date time;
    private String strTime;
    private int week;
    private int month;


    TemperatureData(String timeStamp, String temperature, String sourceName, String humidity) {

        SimpleDateFormat weekFormatter = new SimpleDateFormat("w");
        SimpleDateFormat monthFormatter = new SimpleDateFormat("MM");

        this.temperature = String.format("%.1f", Float.parseFloat(temperature));
        this.sourceName = sourceName;
        this.humidity = String.format("%.1f", Float.parseFloat(humidity));

        String[] split = timeStamp.split(",");

        try {
            this.date = dateFormatter.parse(split[0]);
            this.time = timeFormatter.parse(split[1]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.strTime = split[1].substring(0, 5);
        this.week = Integer.parseInt(weekFormatter.format(this.date));
        this.month = Integer.parseInt(monthFormatter.format(this.date));
    }


    String getValue(boolean getTemp) {
        if (getTemp)
            return temperature;
        else
            return humidity;
    }


    Date getDate() {
        return date;
    }

    String getDateAsString() {
        return dateFormatter.format(date);
    }

    int getWeek() {
        return week;
    }

    int getMonth() {
        return month;
    }

    String getTimeAsString() {
        return strTime;
    }
}
