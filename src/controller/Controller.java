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

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    ObservableList<String> format = FXCollections.observableArrayList("Date", "Week", "Month", "Year");


    String date;
    String week;
    String month;
    String year;
    Object[] json;
    int formatIndex;

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

        pickFormat();
        initGraph();

    }

    //Updates values when using combo box
    @FXML
    public void comboBoxPickerAction(ActionEvent event) {

        pickFormat();
        initGraph();

    }



    //Choose date format
    public void pickFormat(){

        LocalDate localDate = datePicker.getValue();

        if (showByCombBox.getValue().equals(format.get(0))) { // 0 = Date

            //yyyy-MM-dd
            date = localDate.toString();
            headLabel.setText(date);
            formatIndex = 0;

        } else if (showByCombBox.getValue().equals(format.get(1))){ // 1 = Week

            //Week number
            WeekFields weekFields = WeekFields.of(Locale.GERMAN);
            week = Integer.toString(localDate.get(weekFields.weekOfWeekBasedYear()));
            headLabel.setText("Week: " + week);
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

        int laps;
        int jump;

        chart.getStylesheets().add(getClass().getResource("linechart.css").toString());
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);

        OfficialDataHandler officialData = sort((OfficialDataHandler) json[0], formatIndex);
        SensorDataHandler sensorData = sort((SensorDataHandler) json[1], formatIndex);

        XYChart.Series series1 = new XYChart.Series();
        XYChart.Series series2 = new XYChart.Series();

        for (int i = 0; i < 24; i++) {

            series1.getData().add(new XYChart.Data<String, Number>(officialData.formatDate(i, 0), officialData.formatTemp(i)));
            series2.getData().add(new XYChart.Data<String, Number>(sensorData.formatDate(i, 0), sensorData.formatTemp(i)));


        }

        chart.getData().addAll(series1, series2);

    }

    private OfficialDataHandler sort(OfficialDataHandler dataArray, int toDo) {

        if(toDo == 0){

            OfficialData[] array = new OfficialData[24];
            OfficialData[] fullArray = dataArray.getOfficialDataArray();

            for(int i = 0; i < fullArray.length; i++) {

                if (date.equals(fullArray[i].getTimeStamp().substring(0, 10))) {

                    int counter = 0;

                    for(int j = i; j < array.length; j++){

                        OfficialData x = new OfficialData(dataArray.formatDate(j, 0), dataArray.formatTemp(j).toString(), dataArray.getOfficialDataArray()[j].getSourceName());
                        array[counter] = x;
                        counter++;

                    }
                    break;
                }
            }
            OfficialDataHandler returnable = new OfficialDataHandler(array, array.length);
            return returnable;
        }


        return null;
    }

    private SensorDataHandler sort(SensorDataHandler dataArray, int toDo) {


        return null;
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

        initGraph();

    }
}
