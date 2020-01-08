package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This object is used to format date strings and getting current date.
 */

public class TimeAndDateHelper {

    private DateTimeFormatter date;

    public TimeAndDateHelper() {
        date = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    public LocalDateTime getDate() {
        return LocalDateTime.now();
    }

}
