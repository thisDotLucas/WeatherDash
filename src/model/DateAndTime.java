package model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;


public class DateAndTime {

    LocalDate orderDate;
    LocalTime orderTime;
    String date;
    String time;

    public DateAndTime(){

        orderDate = LocalDate.now();
        orderTime = LocalTime.now();

        date = orderDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        time = orderTime.format(DateTimeFormatter.ISO_LOCAL_TIME);

    }

    public String getDate(){

        return date;

    }

    public LocalDate getLoacalDate(){

        return orderDate;

    }

    public String getTime() {
        return time;
    }

    public String getYesterday(){


        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        return formatter.format(new Date(System.currentTimeMillis() - 24*60*60*1000));

    }
}
