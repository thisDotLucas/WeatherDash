package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import javafx.event.ActionEvent;
import model.*;

import java.math.RoundingMode;
import java.net.URL;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    ObservableList<String> format = FXCollections.observableArrayList("Date", "Week", "Month (N/A)", "Year (N/A)");


    String startDate = "2019-05-07"; //sensor data start
    String nowDate;
    String date;
    String week;
    String month;
    String year;
    Object[] json;
    int formatIndex = 0;

    //Labels
    @FXML
    public Label headLabel;
    @FXML
    public Label difTempLabel;
    @FXML
    public Label weatherTempLabel;
    @FXML
    public Label sensorTempLabel;

    //Datepicker
    @FXML
    public DatePicker datePicker;

    //Combo box
    @FXML
    public ComboBox showByCombBox;

    //Line chart
    @FXML
    public LineChart<String, Number> chart;

    @FXML
    public CategoryAxis xAxis;

    @FXML
    public NumberAxis yAxis;



    //Sets header label
    @FXML
    public void datePickerAction(ActionEvent event) {

        changeAction();

    }

    //Updates values when using combo box
    @FXML
    public void comboBoxPickerAction(ActionEvent event) {

       changeAction();

    }



    //Choose date format
    public void pickFormat(){

        LocalDate localDate = datePicker.getValue();

        if (showByCombBox.getValue().equals(format.get(0))) { // 0 = Date

            //yyyy-MM-dd
            date = localDate.toString();
            headLabel.setText(date);
            xAxis.setLabel("Time");
            formatIndex = 0;

        } else if (showByCombBox.getValue().equals(format.get(1))){ // 1 = Week

            //Week number
            WeekFields weekFields = WeekFields.of(Locale.GERMAN);
            week = Integer.toString(localDate.get(weekFields.weekOfWeekBasedYear()));
            headLabel.setText("Week: " + week);
            xAxis.setLabel("Day");
            formatIndex = 1;

        } else if (showByCombBox.getValue().equals(format.get(2))){ // 2 = Month

            //Month and year
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
            String monthYear = localDate.format(formatter);
            month = monthYear.substring(0, 4);
            headLabel.setText(monthYear);
            formatIndex = 2;

        } else { // Year

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
            year = localDate.format(formatter);
            headLabel.setText(year);
            formatIndex = 3;

        }

    }

    //Line chart
    @FXML
    public void initGraph(){

        chart.getStylesheets().add(getClass().getResource("linechart.css").toString());
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);

        OfficialDataHandler officialData = sort((OfficialDataHandler) json[0], formatIndex);
        SensorDataHandler sensorData = sort((SensorDataHandler) json[1], formatIndex);

        XYChart.Series series1 = new XYChart.Series();
        XYChart.Series series2 = new XYChart.Series();

        float officialDataAverage = 0;
        float sensorDataAverage = 0;

        for (int i = 0; i < officialData.size(); i++) {

            officialDataAverage += officialData.formatTemp(i);
            sensorDataAverage += sensorData.formatTemp(i);

            series1.getData().add(new XYChart.Data<String, Number>(officialData.formatDate(i, formatIndex), officialData.formatTemp(i)));
            series2.getData().add(new XYChart.Data<String, Number>(sensorData.formatDate(i, formatIndex), sensorData.formatTemp(i)));

        }

        chart.getData().addAll(series1, series2);

        weatherTempLabel.setText(String.format("%.01f", officialDataAverage / officialData.size()) + "°C");
        sensorTempLabel.setText(String.format("%.01f",sensorDataAverage / sensorData.size()) + "°C");

        float difference = (sensorDataAverage / sensorData.size()) - (officialDataAverage / officialData.size());

        if (difference < 0.0)
            difference = difference * -1;

        difTempLabel.setText(String.format("%.02f", difference) + "°C");

    }

    private OfficialDataHandler sort(OfficialDataHandler dataArray, int toDo) {

        OfficialData[] fullArray = dataArray.getOfficialDataArray();
        OfficialDataHandler returnable;

        if (toDo == 0) { //date

            OfficialData[] array = new OfficialData[24];
            OfficialData x;

            for (int i = 0; i < fullArray.length; i++) {

                if (date.equals(fullArray[i].getTimeStamp().substring(0, 10)) && fullArray[i].getTimeStamp().substring(11, 16).equals("00:00")) {

                    int counter = 0;
                    int diff = 0;

                    for (int j = i; j < i + 24; j++) {

                        if (i - diff < 0){
                            x = new OfficialData(fullArray[0].getTimeStamp(), dataArray.formatTemp(0).toString(), dataArray.getOfficialDataArray()[0].getSourceName());
                        } else
                            x = new OfficialData(fullArray[i - diff].getTimeStamp(), dataArray.formatTemp(i - diff).toString(), dataArray.getOfficialDataArray()[i - diff].getSourceName());
                        array[counter] = x;
                        counter++;
                        diff++;

                    }
                    break;
                }
            }

            returnable = new OfficialDataHandler(array, array.length);

            return returnable;

        } else if (toDo == 1) { //week

            OfficialData[] array = null;
            float averageTemp = 0;

            for (int i = 0; i < fullArray.length; i++) {

                //if (weekNumber(date).equals(weekNumber(fullArray[i].getTimeStamp().substring(0, 10))) && toDayName(fullArray[i].getTimeStamp().substring(0, 10)).equals("Sun") && fullArray[i].getTimeStamp().substring(11, 16).equals("00:00")) {
                if (weekNumber(date).equals(weekNumber(fullArray[i].getTimeStamp().substring(0, 10)))) {

                    int arrayLength = getDayNumber(toDayName(fullArray[i].getTimeStamp().substring(0, 10)));
                    array = new OfficialData[arrayLength];

                    int counter = array.length - 1;
                    int index = i;

                    while (counter >= 0) {


                        for (int z = 0; z < 24; z++) {

                            averageTemp += Float.parseFloat(dataArray.getOfficialDataArray()[index + z].getTemperature());

                        }

                        OfficialData x = new OfficialData(toDayName(fullArray[index].getTimeStamp()), formatTemp(averageTemp / 24).toString(), dataArray.getOfficialDataArray()[index].getSourceName());
                        array[counter] = x;
                        averageTemp = 0;
                        counter--;
                        index += 24;

                    }
                    break;
                }


            }
            returnable = new OfficialDataHandler(array, array.length);
            return returnable;
        }
        return null;
    }

    private SensorDataHandler sort(SensorDataHandler dataArray, int toDo) {

        SensorData[] fullArray = dataArray.getSensorDataArray();
        SensorDataHandler returnable;

        if(toDo == 0){

            SensorData[] array = new SensorData[24];
            SensorData x;

            for(int i = 0; i < i + 24; i++) {

                if (date.equals(fullArray[i].getTimeStamp().substring(0, 10)) && fullArray[i].getTimeStamp().substring(11, 16).equals("00:00")) {

                    int counter = 0;
                    int diff = 0;

                    for(int j = i; j < i + 24; j++){

                        if (i - diff < 0){
                            x = new SensorData(fullArray[0].getTimeStamp(), dataArray.formatTemp(0).toString(), dataArray.getSensorDataArray()[0].getSourceName());
                        } else
                            x = new SensorData(fullArray[i - diff].getTimeStamp(), dataArray.formatTemp(i - diff).toString(), dataArray.getSensorDataArray()[i - diff].getSourceName());

                        array[counter] = x;
                        counter++;
                        diff++;

                    }
                    break;
                }
            }
            returnable = new SensorDataHandler(array, array.length);
            return returnable;

        } else if (toDo == 1) {


            float averageTemp = 0;
            SensorData[] array = null;

            for (int i = 0; i < fullArray.length; i++) {

                //if (weekNumber(date).equals(weekNumber(fullArray[i].getTimeStamp().substring(0, 10))) && toDayName(fullArray[i].getTimeStamp().substring(0, 10)).equals("Sun") && fullArray[i].getTimeStamp().substring(11, 16).equals("00:00")) {
                if (weekNumber(date).equals(weekNumber(fullArray[i].getTimeStamp().substring(0, 10)))){

                    int arrayLength = getDayNumber(toDayName(fullArray[i].getTimeStamp().substring(0, 10)));
                    array = new SensorData[arrayLength];

                    int counter = array.length - 1;
                    int index = i;


                    while (counter >= 0) {

                        for (int z = 0; z < 24; z++) {

                            averageTemp += Float.parseFloat(dataArray.getSensorDataArray()[index + z].getTemperature());

                        }


                        SensorData x = new SensorData(toDayName(fullArray[index].getTimeStamp()), formatTemp(averageTemp / 24).toString(), dataArray.getSensorDataArray()[index].getSourceName());
                        array[counter] = x;
                        averageTemp = 0;
                        counter--;
                        index += 24;

                    }
                    break;
                }
            }
            returnable = new SensorDataHandler(array, array.length);
            return returnable;
        }

        return null;
    }



    private int getDayNumber(String dayName) {

        switch (dayName){

            case "Mon":
                return 1;

            case "Tue":
                return 2;

            case "Wed":
                return 3;

            case "Thu":
                return 4;

            case "Fri":
                return 5;

            case "Sat":
                return 6;

            case "Sun":
                return 7;
        }

        return 0;

    }

    private String toDayName(String timeStamp) {

        Date date = null;

        try {
            date = new SimpleDateFormat("yyyy-M-d").parse(timeStamp.substring(0, 10));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new SimpleDateFormat("EEE", Locale.ENGLISH).format(date);

    }

    private String weekNumber(String input){


        String dateFormat = "yyyy-MM-dd";
        Date date;
        int week = 0;

        SimpleDateFormat df = new SimpleDateFormat(dateFormat);

        try {
            date = df.parse(input);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            week = calendar.get(Calendar.WEEK_OF_YEAR);
            return Integer.toString(week);
        } catch (Exception e){
            System.out.println(e);
        }
        return "";
    }


    public Float formatTemp(float x){

        Float tempToBeFormatted = x;

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        Float formatedTemp = new Float(formatter.format(tempToBeFormatted));

        return formatedTemp;

    }

    private void changeAction(){

        date = datePicker.getValue().toString();
        chart.getData().clear();
        pickFormat();
        
        try {

            initGraph();

        } catch (NullPointerException e){

            weatherTempLabel.setText("N/A");
            sensorTempLabel.setText("N/A");
            difTempLabel.setText("N/A");
            if(Integer.parseInt(date.replaceAll("-", "")) > Integer.parseInt(nowDate.replaceAll("-", ""))) {
                headLabel.setText("Data not available yet. Todays date: " + nowDate);
            } else if (Integer.parseInt(date.replaceAll("-", "")) < Integer.parseInt(startDate.replaceAll("-", ""))){
                headLabel.setText("Data available from 07.05.2019 - Current date.");
            } else if (weekNumber(date).equals("19")){
                headLabel.setText("Full data for week not available");
            } else {
                headLabel.setText("Not enough data available yet.");
            }


        }

    }


    //At start
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {

            json = Json.getJson();

        } catch (Exception e){

            System.out.println(e);
        }

        model.DateAndTime x = new DateAndTime();
        String currDate = x.getDate();

        headLabel.setText(currDate);
        datePicker.setValue(x.getLoacalDate());

        showByCombBox.setValue("Date");
        showByCombBox.setItems(format);

        LocalDate localDate = datePicker.getValue();
        date = localDate.toString();
        nowDate = date;

        initGraph();

    }
}
