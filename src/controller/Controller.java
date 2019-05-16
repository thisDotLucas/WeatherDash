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
import model.DateAndTime;
import model.Json;
import model.OfficialDataHandler;
import model.SensorDataHandler;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    ObservableList<String> format = FXCollections.observableArrayList("Date", "Week", "Month", "Year");

    String date;
    Object[] json;

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

    }

    //Updates values when using combo box
    @FXML
    public void comboBoxPickerAction(ActionEvent event) {

        pickFormat();

    }



    //Choose date format
    public void pickFormat(){

        LocalDate localDate = datePicker.getValue();

        if (showByCombBox.getValue().equals(format.get(0))) { // 0 = Date

            //yyyy-MM-dd
            date = localDate.toString();
            headLabel.setText(date);

        } else if (showByCombBox.getValue().equals(format.get(1))){ // 1 = Week

            //Week number
            WeekFields weekFields = WeekFields.of(Locale.GERMAN);
            String week = Integer.toString(localDate.get(weekFields.weekOfWeekBasedYear()));
            headLabel.setText("Week: " + week);

        } else if (showByCombBox.getValue().equals(format.get(2))){ // 2 = Month

            //Month and year
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
            String monthYear = localDate.format(formatter);
            headLabel.setText(monthYear);

        } else { // Year

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
            String year = localDate.format(formatter);
            headLabel.setText(year);

        }

    }

    //Line chart
    @FXML
    public void initGraph(){

        chart.setAnimated(false);
        chart.setLegendVisible(false);

        OfficialDataHandler officialData = (OfficialDataHandler) json[0];
        SensorDataHandler sensorData = (SensorDataHandler) json[1];

        XYChart.Series series1 = new XYChart.Series();
        XYChart.Series series2 = new XYChart.Series();

        for (int i = 0; i < 24; i++) {

            series1.getData().add(new XYChart.Data<String, Number>(officialData.formatDate(i, 0), officialData.formatTemp(i)));
            series2.getData().add(new XYChart.Data<String, Number>(sensorData.formatDate(i, 0), sensorData.formatTemp(i)));

        }

        chart.getData().addAll(series1, series2);

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

        initGraph();

        headLabel.setText(currDate);
        datePicker.setValue(x.getLoacalDate());

        showByCombBox.setValue("Date");
        showByCombBox.setItems(format);

    }
}
