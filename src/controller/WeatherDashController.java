package controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import model.Json;
import model.LineChartHandler;
import model.TemperatureData;
import model.TimeAndDateHelper;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;

public class WeatherDashController implements Initializable {

    private ArrayList<TemperatureData> data;
    private boolean isTemp;

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
    @FXML
    public Label difLabel;
    @FXML
    public Label monthLabel;
    @FXML
    public Label dayLabel;
    @FXML
    public Label jumpOfLabel;
    @FXML
    public Label theLastLabel;

    @FXML
    public CheckBox nodeCheckBox;

    @FXML
    public ComboBox<Integer> monthComboBox;

    @FXML
    public TextField jumpTextField;

    //Datepicker
    @FXML
    public DatePicker datePicker;

    //Combo box
    @FXML
    public ComboBox showByComboBox;

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
    public void datePickerAction() {
        if(showByComboBox.getValue() != null) {
            setMonthComboBoxItems();
            monthComboBox.setValue(1);
            updateChart();
        }
    }


    //Updates values when using combo box
    @FXML
    public void comboBoxPickerAction() {
       updateChart();
    }

    @FXML
    public void onMonthComboBox(){
        try {
            if (monthComboBox.getValue() > 1)
                monthLabel.setText("months.");
            else
                monthLabel.setText("month.");

            if (jumpTextField.getText().equals(""))
                jumpTextField.setText("3");
            updateChart();
        } catch (NullPointerException e){
            monthLabel.setText("month.");
        }

    }

    @FXML
    public void onNodeCheckBox(){ updateChart(); }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        setLoadingState();

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                data = Json.getTemperatureDataArrayList();
                Platform.runLater(() -> {
                   setLoadedState();
                });
                return null;
            }
        };
        new Thread(task).start();

        initLineChart();
        showByComboBox.setItems(
                FXCollections.observableArrayList("Day", "Week", "Months")
        );

        initDatePicker();
        setCharLimit(jumpTextField, 3);
        onlyNumbers(jumpTextField);
        showMonthSettings(false);
        setMonthComboBoxItems();
        radioTempAction();
        radioTemperature.setSelected(true);
    }

    private void setMonthComboBoxItems() {
        monthComboBox.getItems().clear();

        Calendar startCalendar = new GregorianCalendar();
        startCalendar.setTime(new GregorianCalendar(2019, Calendar.JUNE + 1, 1).getTime());
        Calendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH) + 2;

        for(int i = 0; i < diffMonth; i++)
            monthComboBox.getItems().add(i + 1);

    }


    private void setLoadingState(){
        showByComboBox.setDisable(true);
        datePicker.setDisable(true);
        radioTemperature.setDisable(true);
        radioHumidity.setDisable(true);
        headLabel.setText("Loading Data ...");
        showingLabel.setText("");
    }

    private void setLoadedState(){
        showByComboBox.setDisable(false);
        datePicker.setDisable(false);
        radioTemperature.setDisable(false);
        radioHumidity.setDisable(false);
        headLabel.setText("Showing:");
        showingLabel.setText("");
    }

    private void initLineChart(){

        chart.getStylesheets().add("view/linechart.css");
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);

    }

    private void updateChart() {
        if(showByComboBox.getValue() != null) {
            chart.getData().clear();
            LineChartHandler handler = new LineChartHandler(this);
            if (showByComboBox.getValue().equals("Day")) {
                handler.graphDay();
                showingLabel.setText(datePicker.getValue().toString());
                showMonthSettings(false);
            } else if (showByComboBox.getValue().equals("Week")) {
                handler.graphWeek();
                TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
                showingLabel.setText("Week " + datePicker.getValue().get(woy));
                showMonthSettings(false);
            } else {
                handler.graphMonth(monthComboBox.getValue(), Integer.parseInt(jumpTextField.getText())*2);
                showMonthSettings(true);
            }
        }
    }


    private void showMonthSettings(boolean x) {
        monthLabel.setVisible(x);
        dayLabel.setVisible(x);
        jumpOfLabel.setVisible(x);
        theLastLabel.setVisible(x);
        jumpTextField.setVisible(x);
        monthComboBox.setVisible(x);
        if(!x){
            monthComboBox.setValue(1);
            jumpTextField.setText("3");
        }
    }


    private void initDatePicker() {

        datePicker.setEditable(false);
        datePicker.setValue(new TimeAndDateHelper().getDate().toLocalDate());
        datePicker.setConverter(new StringConverter<LocalDate>() {

            private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate localDate)
            {
                if(localDate == null)
                    return "";
                return dateTimeFormatter.format(localDate);
            }

            @Override
            public LocalDate fromString(String dateString)
            {
                if(dateString == null || dateString.trim().isEmpty()) {
                    return null;
                }
                return LocalDate.parse(dateString, dateTimeFormatter);
            }
        });

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isBefore(LocalDate.of(2019, 6, 1)) || date.compareTo(LocalDate.now()) > 0);
            }
        });

    }

    /**
     * This listener sets a limit for the amount of characters in a text field.
     */
    public void setCharLimit(TextField textField, int limit){
        textField.textProperty().addListener(l -> {
            if (textField.getText().length() > limit) {
                String s = textField.getText().substring(0, limit);
                textField.setText(s);
            }

        });
    }

    /**
     * This listener makes the text field only accept numbers.
     */
    public void onlyNumbers(TextField textField){
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    textField.setText(newValue.replaceAll("[^\\d]", ""));
                }
                if(!textField.getText().equals("")) {
                    updateChart();
                    if (Integer.parseInt(textField.getText()) > 1)
                        dayLabel.setText("days.");
                    else
                        dayLabel.setText("day.");
                }
            }
        });
    }


    public ArrayList<TemperatureData> getData(){ return data; }

    public DatePicker getDatePicker(){ return  datePicker; }

    public LineChart<String, Number> getChart(){ return chart; }

    public ComboBox<Integer> getMonthComboBox() { return monthComboBox; }

    public CheckBox getNodeCheckBox(){ return nodeCheckBox; }

    public TextField getJumpTextField() {return jumpTextField; }

    public Label getWeatherTempLabel(){ return weatherTempLabel; }

    public Label getSensorTempLabel() { return sensorTempLabel; }

    public Label getDifTempLabel() { return difTempLabel; }

    public Label getShowingLabel(){ return showingLabel; }

    public boolean isTemp() { return isTemp; }

    @FXML
    public void radioTempAction(){
        radioHumidity.setSelected(false);
        isTemp = true;
        updateChart();
        chart.getYAxis().setLabel("Temperature °C");
    }




    @FXML
    public void radioHumidityAction(){
        radioTemperature.setSelected(false);
        isTemp = false;
        updateChart();
        chart.getYAxis().setLabel("Humidity %");

    }

}


