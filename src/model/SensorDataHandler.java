package model;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SensorDataHandler {

    SensorData[] sensorDataArray;
    int size;

    public SensorDataHandler(SensorData[] sensorDataArray, int size){

        this.sensorDataArray = sensorDataArray;
        this.size = size;

    }

    public SensorData[] getSensorDataArray() {
        return sensorDataArray;
    }

    public String formatDate(int index, int format) {

        String dateToBeFormatted = sensorDataArray[index].getTimeStamp();
        String toBeReturned = "Error";

        if (format == 0) { //Hours

            toBeReturned = dateToBeFormatted.substring(11, 16);

        } else if (format == 1) { //Days

            toBeReturned = dateToBeFormatted.substring(0, 10);

        } else if (format == 2 || format == 3) { //Weeks or months

            String input = dateToBeFormatted.substring(0, 10);
            String dateFormat = "yyyy-MM-dd";
            Date date;
            int week = 0;
            int month = 0;

            SimpleDateFormat df = new SimpleDateFormat(dateFormat);

            try {
                date = df.parse(input);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                if (format == 2) {
                    week = calendar.get(Calendar.WEEK_OF_YEAR);
                    return Integer.toString(week);
                } else {
                    month = calendar.get(Calendar.MONTH);
                    return Integer.toString(month);
                }
            } catch (Exception e) {

                System.out.println(e);

            }

        }

        return toBeReturned;

    }

    public Float formatTemp(int index){

        Float tempToBeFormatted = Float.parseFloat(sensorDataArray[index].getTemperature());

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        Float formatedTemp = new Float(formatter.format(tempToBeFormatted));

        return formatedTemp;

    }

    public int size(){

        return size;

    }
}
