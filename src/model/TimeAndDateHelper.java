package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This object is used to format date strings and getting current date and time.
 */

public class TimeAndDateHelper {

    private DateTimeFormatter date;
    private DateTimeFormatter time;

    public TimeAndDateHelper() {
        date = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        time = DateTimeFormatter.ofPattern("hh:MM:ss");
    }

    public LocalDateTime getDate() {
        return LocalDateTime.now();
    }



    public String getTime() {
        return time.format(LocalDateTime.now());
    }

}
