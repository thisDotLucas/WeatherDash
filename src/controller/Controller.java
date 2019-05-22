package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import javafx.event.ActionEvent;
import javafx.scene.control.RadioButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import model.*;

import javax.activation.DataHandler;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.Time;
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
import java.util.concurrent.TimeUnit;

public class Controller implements Initializable {

    ObservableList<String> format = FXCollections.observableArrayList("Date", "Week", "Month (N/A)", "Year (N/A)");


    String startDate = "2019-05-07"; //sensor data start
    String weatherAvg;
    String sensorAvg;
    String diffAvg;
    String nowDate;
    String nowWeek;
    String date;
    String week;
    String month;
    String year;
    Object[] json;
    float [] firstWeekTemps = {(float)5.5, (float)12.1, (float)6.1, (float)15.4, (float)9.9, (float)17.7, (float)9.8, (float)17.0, (float)9.7, (float)16.2, (float)7.2, (float)14.6};
    int [] firstWeekHums = {72, 54, 68, 42, 49, 30, 69, 46, 85, 66, 84, 60};
    String [] weekDays = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    boolean radiotemp = true;
    boolean radioHum;
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
    @FXML
    public Label showingLabel;

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

    //Radio buttons
    @FXML
    public RadioButton radioTemperature;

    @FXML
    public RadioButton radioHumidity;



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

        OfficialDataHandler officialData = null;
        SensorDataHandler sensorData = null;

        XYChart.Series series1 = new XYChart.Series();
        XYChart.Series series2 = new XYChart.Series();

        float officialDataAverage = 0;
        float sensorDataAverage = 0;

        if (weekNumber(date).equals("19") && showByCombBox.getValue().equals("Week")){

            int dayCounter = 1;

            for (int i = 0; i < firstWeekTemps.length; i += 2) {

                if (radiotemp) {

                    officialDataAverage += firstWeekTemps[i];
                    sensorDataAverage += firstWeekTemps[i + 1];

                    XYChart.Data data1 = (new XYChart.Data<String, Number>(weekDays[dayCounter], firstWeekTemps[i]));
                    XYChart.Data data2 = (new XYChart.Data<String, Number>(weekDays[dayCounter], firstWeekTemps[i + 1]));

                    data1.setNode(new HoveredThresholdNode(firstWeekTemps[i + 1], firstWeekTemps[i], 0, weekDays[dayCounter]));
                    data2.setNode(new HoveredThresholdNode(firstWeekTemps[i], firstWeekTemps[i + 1], 1, weekDays[dayCounter]));

                    series1.getData().add(data1);
                    series2.getData().add(data2);

                    dayCounter++;

                } else {

                    officialDataAverage += firstWeekHums[i];
                    sensorDataAverage += firstWeekHums[i + 1];

                    XYChart.Data data1 = (new XYChart.Data<String, Number>(weekDays[dayCounter], firstWeekHums[i]));
                    XYChart.Data data2 = (new XYChart.Data<String, Number>(weekDays[dayCounter], firstWeekHums[i + 1]));

                    data1.setNode(new HoveredThresholdNode(firstWeekHums[i + 1], firstWeekHums[i], 0, weekDays[dayCounter]));
                    data2.setNode(new HoveredThresholdNode(firstWeekHums[i], firstWeekHums[i + 1], 1, weekDays[dayCounter]));

                    series1.getData().add(data1);
                    series2.getData().add(data2);

                    dayCounter++;

                }
            }

        } else {

            officialData = sort((OfficialDataHandler) json[0], formatIndex);
            sensorData = sort((SensorDataHandler) json[1], formatIndex);

        for (int i = 0; i < officialData.size(); i++) {

            if (radiotemp) {

                officialDataAverage += officialData.formatTemp(i);
                sensorDataAverage += sensorData.formatTemp(i);

                XYChart.Data data1 = (new XYChart.Data<String, Number>(officialData.formatDate(i, formatIndex), officialData.formatTemp(i)));
                XYChart.Data data2 = (new XYChart.Data<String, Number>(sensorData.formatDate(i, formatIndex), sensorData.formatTemp(i)));

                data1.setNode(new HoveredThresholdNode(sensorData.formatTemp(i), officialData.formatTemp(i), 0, officialData.formatDate(i, formatIndex)));
                data2.setNode(new HoveredThresholdNode(officialData.formatTemp(i), sensorData.formatTemp(i), 1, sensorData.formatDate(i, formatIndex)));

                series1.getData().add(data1);
                series2.getData().add(data2);

            } else {

                officialDataAverage += officialData.formatHumidity(i);
                sensorDataAverage += sensorData.formatHumidity(i);

                XYChart.Data data1 = (new XYChart.Data<String, Number>(officialData.formatDate(i, formatIndex), officialData.formatHumidity(i)));
                XYChart.Data data2 = (new XYChart.Data<String, Number>(sensorData.formatDate(i, formatIndex), sensorData.formatHumidity(i)));

                data1.setNode(new HoveredThresholdNode(sensorData.formatHumidity(i), officialData.formatHumidity(i), 0, officialData.formatDate(i, formatIndex)));
                data2.setNode(new HoveredThresholdNode(officialData.formatHumidity(i), sensorData.formatHumidity(i), 1, sensorData.formatDate(i, formatIndex)));

                series1.getData().add(data1);
                series2.getData().add(data2);

            }
        }




        }

        chart.getData().addAll(series1, series2);

        if (weekNumber(date).equals("19") && showByCombBox.getValue().equals("Week")){

            if (radiotemp) {

                weatherTempLabel.setText(Float.toString(officialDataAverage / (firstWeekTemps.length / 2)).substring(0, 4) + "°C");
                sensorTempLabel.setText(Float.toString(sensorDataAverage / (firstWeekTemps.length / 2)).substring(0, 4) + "°C");

                float difference = (sensorDataAverage / (firstWeekTemps.length / 2)) - (officialDataAverage / (firstWeekTemps.length / 2));

                if (difference < 0.0)
                    difference = difference * -1;

                difTempLabel.setText(Float.toString(difference).substring(0, 5) + "°C");

            } else {

                weatherTempLabel.setText(Math.round(officialDataAverage / (firstWeekTemps.length / 2)) + "%");
                sensorTempLabel.setText(Math.round(sensorDataAverage / (firstWeekTemps.length / 2)) + "%");

                float x = Math.abs(sensorDataAverage / (firstWeekTemps.length / 2) - officialDataAverage / (firstWeekTemps.length / 2));
                float y = Math.abs((sensorDataAverage / (firstWeekTemps.length / 2) + officialDataAverage / (firstWeekTemps.length / 2)) / 2);

                double difference = (x / y * 100);

                difTempLabel.setText(Double.toString(difference).substring(0, 4) + "%");

            }

        } else {

            if (radiotemp) {

                weatherTempLabel.setText(String.format("%.01f", officialDataAverage / officialData.size()) + "°C");
                sensorTempLabel.setText(String.format("%.01f", sensorDataAverage / sensorData.size()) + "°C");

                float difference = (sensorDataAverage / sensorData.size()) - (officialDataAverage / officialData.size());

                if (difference < 0.0)
                    difference = difference * -1;

                difTempLabel.setText(String.format("%.02f", difference) + "°C");

            } else {

                weatherTempLabel.setText(Math.round(officialDataAverage / officialData.size()) + "%");
                sensorTempLabel.setText(Math.round(sensorDataAverage / sensorData.size()) + "%");

                float x = Math.abs(sensorDataAverage / sensorData.size() - officialDataAverage / officialData.size());
                float y = Math.abs((sensorDataAverage / sensorData.size() + officialDataAverage / officialData.size()) / 2);

                double difference = (x / y * 100);

                difTempLabel.setText(String.format("%.1f", difference) + "%");

            }
        }

    }





    private OfficialDataHandler sort(OfficialDataHandler dataArray, int toDo) {

        if (toDo == 0) { //date

            return sortByDayOfficial(dataArray);
            

        } else if (toDo == 1) { //week

           return sortByWeekOfficial(dataArray);

        }
        return null;
    }

    private OfficialDataHandler sortByWeekOfficial(OfficialDataHandler dataArray) {

        OfficialData[] fullArray = dataArray.getOfficialDataArray();
        OfficialDataHandler returnable;

        OfficialData[] array = null;
        float averageTemp = 0;
        int averageHum = 0;

        for (int i = 0; i < fullArray.length; i++) {

            if (weekNumber(date).equals(weekNumber(fullArray[i].getTimeStamp().substring(0, 10)))) {

                int arrayLength = getDayNumber(toDayName(fullArray[i].getTimeStamp().substring(0, 10)));
                array = new OfficialData[arrayLength];

                int counter = array.length - 1;
                int index = i;
                int lastlap = 0;
                int hoursInADay = 24;
                boolean checkLastDay = false;

                if (weekNumber(date).equals(nowWeek)){

                    lastlap = Integer.parseInt(fullArray[0].getTimeStamp().substring(11, 13)) + 1;
                    checkLastDay = true;

                }

                while (counter >= 0) {

                    if (checkLastDay && counter - 1 == 0){

                        hoursInADay = lastlap;

                    }

                    for (int z = 0; z < hoursInADay; z++) {

                        averageTemp += Float.parseFloat(dataArray.getOfficialDataArray()[index + z].getTemperature());
                        averageHum += Math.round(Float.parseFloat(dataArray.getOfficialDataArray()[index + z].getHumidity()));

                    }

                    OfficialData x;
                    x = new OfficialData(toDayName(fullArray[index].getTimeStamp()), formatTemp(averageTemp / hoursInADay).toString(), dataArray.getOfficialDataArray()[index].getSourceName(), Integer.toString(averageHum / hoursInADay) );

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






    private SensorDataHandler sort(SensorDataHandler dataArray, int toDo) {

        if(toDo == 0){//date

            return sortByDaySensor(dataArray);

        } else if (toDo == 1) {//week

            return sortByWeekSensor(dataArray);

        }

        return null;
    }







    private SensorDataHandler sortByWeekSensor(SensorDataHandler dataArray) {

        SensorData[] fullArray = dataArray.getSensorDataArray();
        SensorDataHandler returnable;

        float averageTemp = 0;
        int averageHum = 0;
        SensorData[] array = null;

        for (int i = 0; i < fullArray.length; i++) {

            if (weekNumber(date).equals(weekNumber(fullArray[i].getTimeStamp().substring(0, 10)))){

                int arrayLength = getDayNumber(toDayName(fullArray[i].getTimeStamp().substring(0, 10)));
                array = new SensorData[arrayLength];

                int counter = array.length - 1;
                int index = i;
                int lastlap = 0;
                int hoursInADay = 24;
                boolean checkLastDay = false;

                if (weekNumber(date).equals(nowWeek)){

                    lastlap = Integer.parseInt(fullArray[0].getTimeStamp().substring(11, 13)) + 1;
                    checkLastDay = true;

                }


                while (counter >= 0) {

                    if (checkLastDay && counter - 1 == 0){

                        hoursInADay = lastlap;

                    }

                    for (int z = 0; z < hoursInADay; z++) {

                        averageTemp += Float.parseFloat(dataArray.getSensorDataArray()[index + z].getTemperature());
                        averageHum += Math.round(Float.parseFloat(dataArray.getSensorDataArray()[index + z].getHumidity()));

                    }

                    SensorData x;
                    x = new SensorData(toDayName(fullArray[index].getTimeStamp()), formatTemp(averageTemp / hoursInADay).toString(), dataArray.getSensorDataArray()[index].getSourceName(), Integer.toString(averageHum / hoursInADay));

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







    private SensorDataHandler sortByDaySensor(SensorDataHandler dataArray) {

        SensorData[] fullArray = dataArray.getSensorDataArray();
        SensorDataHandler returnable;

        SensorData[] array = new SensorData[24];
        SensorData x;

        for(int i = 0; i < i + 24; i++) {

            if (date.equals(fullArray[i].getTimeStamp().substring(0, 10)) && fullArray[i].getTimeStamp().substring(11, 16).equals("00:00")) {

                int counter = 0;
                int diff = 0;

                for(int j = i; j < i + 24; j++){

                    if (i - diff < 0){
                        x = new SensorData(fullArray[0].getTimeStamp(), dataArray.formatTemp(0).toString(), dataArray.getSensorDataArray()[0].getSourceName(), dataArray.getSensorDataArray()[0].getHumidity());
                    } else
                        x = new SensorData(fullArray[i - diff].getTimeStamp(), dataArray.formatTemp(i - diff).toString(), dataArray.getSensorDataArray()[i - diff].getSourceName(), dataArray.getSensorDataArray()[i - diff].getHumidity());

                    array[counter] = x;
                    counter++;
                    diff++;

                }
                break;
            }
        }
        returnable = new SensorDataHandler(array, array.length);
        return returnable;
    }






    
    private OfficialDataHandler sortByDayOfficial(OfficialDataHandler dataArray){

        OfficialData[] fullArray = dataArray.getOfficialDataArray();
        OfficialDataHandler returnable;
        
        OfficialData[] array = new OfficialData[24];
        OfficialData x;

        for (int i = 0; i < fullArray.length; i++) {

            if (date.equals(fullArray[i].getTimeStamp().substring(0, 10)) && fullArray[i].getTimeStamp().substring(11, 16).equals("00:00")) {

                int counter = 0;
                int diff = 0;

                for (int j = i; j < i + 24; j++) {

                    if (i - diff < 0){
                        x = new OfficialData(fullArray[0].getTimeStamp(), dataArray.formatTemp(0).toString(), dataArray.getOfficialDataArray()[0].getSourceName(), dataArray.getOfficialDataArray()[0].getHumidity());
                    } else
                        x = new OfficialData(fullArray[i - diff].getTimeStamp(), dataArray.formatTemp(i - diff).toString(), dataArray.getOfficialDataArray()[i - diff].getSourceName(), dataArray.getOfficialDataArray()[i - diff].getHumidity());
                    array[counter] = x;
                    counter++;
                    diff++;

                }
                break;
            }
        }

        returnable = new OfficialDataHandler(array, array.length);
        return returnable;
    }



    @FXML
    public void radioTempAction(){
       radioHumidity.setSelected(false);
       radiotemp = true;
       radioHum = false;
       chart.getData().clear();
       initGraph();
       saveAvg();
    }




    @FXML
    public void radioHumidityAction(){
        radioTemperature.setSelected(false);
        radioHum = true;
        radiotemp = false;
        chart.getData().clear();
        initGraph();
        saveAvg();
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
        formatter.setMaximumFractionDigits(1);
        formatter.setMinimumFractionDigits(1);
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
            saveAvg();

        } catch (NullPointerException e){

            weatherTempLabel.setText("N/A");
            sensorTempLabel.setText("N/A");
            difTempLabel.setText("N/A");
            showingLabel.setText("");
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
        String currTime = x.getTime();

        headLabel.setText(currDate);
        datePicker.setValue(x.getLoacalDate());

        showByCombBox.setValue("Date");
        showByCombBox.setItems(format);

        LocalDate localDate = datePicker.getValue();
        date = localDate.toString();

        if(Integer.parseInt(currTime.substring(0, 2)) > 6)
            nowDate = date;
        else
            nowDate = x.getYesterday();

        nowWeek = weekNumber(nowDate);

        radioTemperature.setSelected(true);

        initGraph();
        saveAvg();

    }




    private void saveAvg() {
        weatherAvg = weatherTempLabel.getText();
        sensorAvg = sensorTempLabel.getText();
        diffAvg = difTempLabel.getText();
        if (radiotemp)
        showingLabel.setText("Showing: Average Temperature");
        else
            showingLabel.setText("Showing: Average Humidity");
    }





    class HoveredThresholdNode extends StackPane {

        HoveredThresholdNode(float otherValue, float value, int id, String time) {

            setPrefSize(10, 10);

            if(time.length() == 3)
                time = toFullDayName(time);

            String finalTime = time;

            setOnMouseEntered(mouseEvent -> {

                setCursor(Cursor.HAND);

                if (id == 0) {

                    weatherTempLabel.setText((formatTemp(value)) + "°C");
                    sensorTempLabel.setText((formatTemp(otherValue)) + "°C");
                    float difference = (value - otherValue);
                    if (difference < 0)
                        difference = difference * -1;
                    difTempLabel.setText(String.format("%.02f", difference) + "°C");
                    showingLabel.setText("Showing: Temperature for " + finalTime);

                } else {

                    weatherTempLabel.setText((formatTemp(otherValue)) + "°C");
                    sensorTempLabel.setText((formatTemp(value)) + "°C");
                    float difference = (value - otherValue);
                    if (difference < 0)
                        difference = difference * -1;
                    difTempLabel.setText(String.format("%.02f", difference) + "°C");
                    showingLabel.setText("Showing: Temperature for " + finalTime);

                }

            });

            setOnMouseExited(mouseEvent -> {
                getChildren().clear();
                setCursor(Cursor.CROSSHAIR);
                try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (Exception e){
                    System.out.println(e);
                }

                weatherTempLabel.setText(weatherAvg);
                sensorTempLabel.setText(sensorAvg);
                difTempLabel.setText(diffAvg);
                if(radiotemp)
                showingLabel.setText("Showing: Average Temperature");
                else
                    showingLabel.setText("Showing: Average Humidity");

            });
        }






        HoveredThresholdNode(int otherValue, int value, int id, String time) {

            setPrefSize(10, 10);

            if(time.length() == 3)
                time = toFullDayName(time);

            String finalTime = time;
            setOnMouseEntered(mouseEvent -> {

                setCursor(Cursor.HAND);
                if (id == 0) {
                    weatherTempLabel.setText((Math.round(value)) + "%");
                    sensorTempLabel.setText((Math.round(otherValue)) + "%");
                    float x = Math.abs(value - otherValue);
                    float y = Math.abs(value + otherValue) / 2;
                    float difference = (x / y) * 100;

                    difTempLabel.setText(String.format("%.1f", difference) + "%");
                    showingLabel.setText("Showing: " + "Humidity for " + finalTime);
                } else {
                    weatherTempLabel.setText((Math.round(otherValue)) + "%");
                    sensorTempLabel.setText((Math.round(value)) + "%");
                    float x = Math.abs(value - otherValue);
                    float y = Math.abs(value + otherValue) / 2;
                    float difference = (x / y) * 100;

                    difTempLabel.setText(String.format("%.1f", difference) + "%");
                    showingLabel.setText("Showing: " + "Humidity for " + finalTime);
                }

            });

            setOnMouseExited(mouseEvent -> {
                getChildren().clear();
                setCursor(Cursor.CROSSHAIR);
                try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (Exception e){
                    System.out.println(e);
                }

                weatherTempLabel.setText(weatherAvg);
                sensorTempLabel.setText(sensorAvg);
                difTempLabel.setText(diffAvg);
                showingLabel.setText("Showing: Average Humidity");

            });
        }


        private String toFullDayName(String time) {

            switch (time){

                case "Mon":
                    return "Monday";

                case "Tue":
                    return "Tuesday";

                case "Wed":
                    return "Wednesday";

                case "Thu":
                    return "Thursday";

                case "Fri":
                    return "Friday";

                case "Sat":
                    return "Saturday";

                case "Sun":
                    return "Sunday";

            }

            return "";

        }
    }
}
