package model;

import sun.util.calendar.LocalGregorianCalendar;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class OfficialDataHandler {

    OfficialData[] officialDataArray;
    int size;

    public OfficialDataHandler(OfficialData[] officialDataArray, int size){

        this.officialDataArray = officialDataArray;
        this.size = size;

    }

    public OfficialData[] getOfficialDataArray() {

        return officialDataArray;

    }

    public String formatDate(int index, int format) {

        //System.out.println(officialDataArray[index]);
        String dateToBeFormatted = officialDataArray[index].getTimeStamp();
        String toBeReturned = "Error";

        if (format == 0) { //Hours

            toBeReturned = dateToBeFormatted.substring(11, 16);

        } else if (format == 1) { //Days

            if (dateToBeFormatted.length() < 5)
                return dateToBeFormatted;

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

        Float tempToBeFormatted = Float.parseFloat(officialDataArray[index].getTemperature());

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        Float formatedTemp = new Float(formatter.format(tempToBeFormatted));

        return formatedTemp;

        }

        public int formatHumidity(int index){

        if (officialDataArray[index].getHumidity().length() < 4)
            return Math.round(Float.parseFloat(officialDataArray[index].getHumidity()));
        else
            return Math.round(Float.parseFloat(officialDataArray[index].getHumidity().substring(0, 4)));
        }

        public int size(){

            return size;

        }

    }

