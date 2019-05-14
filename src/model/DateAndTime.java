package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class DateAndTime {

    LocalDate orderDate;
    String date;

    public DateAndTime(){

        orderDate = LocalDate.now();
        date = orderDate.format(DateTimeFormatter.ISO_LOCAL_DATE);


    }

    public String getDate(){

        return date;

    }

    public LocalDate getLoacalDate(){

        return orderDate;

    }
}
