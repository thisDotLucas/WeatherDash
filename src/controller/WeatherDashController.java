package controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;
import model.Json;
import model.LineChartHandler;
import model.TemperatureData;
import model.TimeAndDateHelper;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;

public class WeatherDashController implements Initializable {

    private ArrayList<TemperatureData> data; //Temperature and humidity data from both the api and the sensor. api data on uneven indexes and node data on even indexes.
    private boolean isTemp; //If true temperature is displayed else humidity.
    private boolean isLoading; //If true json data is not loaded.
    private int monthValue; //Default value is 1
    private int jumpValue; //Default value is 3

    @FXML
    private Label headLabel;

    @FXML
    private Label difTempLabel;

    @FXML
    private Label weatherTempLabel;

    @FXML
    private Label sensorTempLabel;

    @FXML
    private Label showingLabel;

    @FXML
    private Label monthLabel;

    @FXML
    private Label dayLabel;

    @FXML
    private CheckBox nodeCheckBox;

    @FXML
    private ComboBox<Integer> monthComboBox;

    @FXML
    private ComboBox showByComboBox;

    @FXML
    private TextField jumpTextField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private LineChart<String, Number> chart;

    @FXML
    private RadioButton radioTemperature;

    @FXML
    private RadioButton radioHumidity;


    /**
     * This method fires when the user switches the date in the date picker. The chart and month
     * combobox will get updated accordingly.
     */
    @FXML
    private void datePickerAction() {
        if (showByComboBox.getValue() != null) {
            setMonthComboBoxItems();
            monthComboBox.setValue(1);
            updateChart();
        }
    }


    /**
     * This method fires when the show by combobox value is changed and updates the chart.
     */
    @FXML
    private void showByComboBoxAction() {
        updateChart();
    }

    /**
     * This method fires when the value in the month combobox is changed. The month label is updated to "label" if one
     * month and if more "months", the monthValue variable and the chart is updated.
     */
    @FXML
    private void onMonthComboBox() {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (monthComboBox.getValue() > 1)
                        monthLabel.setText("months.");
                    else
                        monthLabel.setText("month.");

                    if (jumpTextField.getText().equals(""))
                        jumpTextField.setText("3");

                    monthValue = monthComboBox.getValue();

                    updateChart();

                } catch (NullPointerException e) {
                    monthLabel.setText("month.");
                }
            }
        });
    }

    /**
     * This method fires when the show nodes checkbox is checked or unchecked. If checked hoverable nodes will
     * be shown else nodes will not be shown.
     */
    @FXML
    private void onNodeCheckBox() {
        updateChart();
    }

    /**
     * On initialization the json data is fetched in a separate thread while the thread is running the isLoading variable is true.
     * All variables are set to the default values and the javafx elements are given listeners and set to starting states.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        isLoading = true;
        isTemp = true;
        monthValue = 1;
        jumpValue = 3;
        setState();

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                data = Json.getTemperatureDataArrayList();
                isLoading = false;
                Platform.runLater(() -> {
                    setState();
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
        setCharAndJumpLimit(jumpTextField, 2, 31);
        onlyNumbersMoreThanZero(jumpTextField);
        disableMonthSettings(true);
        showByComboBox.setPromptText("Select a option.");
        setMonthComboBoxItems();

        ToggleGroup radioButtons = new ToggleGroup();
        radioTemperature.setUserData(true);
        radioHumidity.setUserData(false);
        radioTemperature.setToggleGroup(radioButtons);
        radioHumidity.setToggleGroup(radioButtons);
        addRadioButtonListener(radioButtons);

        radioTemperature.setSelected(true);
        jumpTextField.addEventFilter(KeyEvent.ANY, handler);
        jumpTextField.setPromptText("1-31");
        dayLabel.setText("days.");
        monthLabel.setText("months.");
    }

    /**
     * In this method the difference in months from where the data was started to be collected from the current
     * chosen month. The month combo box is then updated so you cannot get months where no data exist.
     */
    private void setMonthComboBoxItems() {
        monthComboBox.getItems().clear();

        Calendar startCalendar = new GregorianCalendar();
        startCalendar.setTime(new GregorianCalendar(2019, Calendar.JUNE + 1, 1).getTime());
        Calendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH) + 2;

        for (int i = 0; i < diffMonth; i++)
            monthComboBox.getItems().add(i + 1);

    }

    /**
     * This method disables all input options when the data is still loading.
     */
    private void setState() {
        showByComboBox.setDisable(isLoading);
        datePicker.setDisable(isLoading);
        radioHumidity.setDisable(isLoading);
        nodeCheckBox.setDisable(isLoading);
        if (isLoading) {
            headLabel.setText("Loading Data ...");
            showingLabel.setText("");

        } else {
            headLabel.setText("Showing:");
            showingLabel.setText("");
            jumpTextField.setText("3");
        }
    }


    /**
     * Initializes the line chart.
     */
    private void initLineChart() {
        chart.getStylesheets().add("view/linechart.css");
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);
    }

    /**
     * This updates the chart according to the user inputted parameters and enables/disables
     * JavaFx elements.
     */
    private void updateChart() {
        if (showByComboBox.getValue() != null) {
            chart.getData().clear();
            LineChartHandler handler = new LineChartHandler(this);
            if (showByComboBox.getValue().equals("Day")) {
                handler.graphDay();
                showingLabel.setText(datePicker.getValue().toString());
                chart.getXAxis().setLabel("Time");
                disableMonthSettings(true);
                resetMonthValues();
            } else if (showByComboBox.getValue().equals("Week")) {
                handler.graphWeek();
                TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
                showingLabel.setText("Week " + datePicker.getValue().get(woy));
                chart.getXAxis().setLabel("Days");
                disableMonthSettings(true);
                resetMonthValues();
            } else {
                disableMonthSettings(false);
                handler.graphMonth(monthValue, jumpValue * 2);
                monthComboBox.setValue(monthValue);
                showingLabel.setText(getMonthLabelText());
                chart.getXAxis().setLabel("Dates");
            }
        }
    }

    /**
     * Gets the text for the label to be shown when choosing many months. For example if december is chosen with a jump of 2 months
     * the string that is returned is October-November.
     */
    private String getMonthLabelText() {
        Month month = datePicker.getValue().getMonth();
        Month prevMonth = month.minus(monthValue - 1);

        String strMonth = month.toString();
        String strPrevMonth = prevMonth.toString();

        if (strMonth.equals(strPrevMonth))
            return strMonth.substring(0, 1) + strMonth.substring(1, strMonth.length()).toLowerCase();
        else
            return strPrevMonth.substring(0, 1) + strPrevMonth.substring(1, strPrevMonth.length()).toLowerCase() + " - " + strMonth.substring(0, 1) + strMonth.substring(1, strMonth.length()).toLowerCase();
    }

    /**
     * Disables the jump textfield and month combobox when not in show by
     * months mode.
     */
    private void disableMonthSettings(boolean disable) {
        jumpTextField.setDisable(disable);
        monthComboBox.setDisable(disable);
    }

    /**
     * Sets the month and jump variables to their default values.
     */
    private void resetMonthValues() {
        monthValue = 1;
        jumpValue = 3;
        monthComboBox.setValue(monthValue);
        jumpTextField.setText(Integer.toString(jumpValue));
    }

    /**
     * sets the date format and disables dates where no data is available.
     */
    private void initDatePicker() {

        datePicker.setEditable(false);
        datePicker.setValue(LocalDate.of(2020, 1, 9));
        datePicker.setConverter(new StringConverter<LocalDate>() {

            private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate localDate) {
                if (localDate == null)
                    return "";
                return dateTimeFormatter.format(localDate);
            }

            @Override
            public LocalDate fromString(String dateString) {
                if (dateString == null || dateString.trim().isEmpty()) {
                    return null;
                }
                return LocalDate.parse(dateString, dateTimeFormatter);
            }
        });

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isBefore(LocalDate.of(2019, 6, 1)) || date.compareTo(LocalDate.now()) > 0 || date.isAfter(LocalDate.of(2020, 1, 9)));
            }
        });

    }

    /**
     * Listener for the group of radio buttons, disable the radio button that is already chosen and
     * sets the y axis label in the chart accordingly.
     */
    private void addRadioButtonListener(ToggleGroup radioButtons) {
        radioButtons.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {

                if (isLoading) { //Radio buttons disabled while data is loading.
                    for (Toggle toggle : radioButtons.getToggles()) {
                        RadioButton radioButton = (RadioButton) toggle;
                        radioButton.setDisable(true);
                    }

                } else if (radioButtons.getSelectedToggle() != null) {

                    RadioButton rb = (RadioButton) radioButtons.getSelectedToggle();

                    for (Toggle toggle : radioButtons.getToggles()) {
                        RadioButton radioButton = (RadioButton) toggle;
                        if (!radioButton.isSelected())
                            radioButton.setDisable(false);
                    }
                    isTemp = (boolean) rb.getUserData();
                    rb.setSelected(true);
                    rb.setDisable(true);

                    if (isTemp)
                        chart.getYAxis().setLabel("Temperature °C");
                    else
                        chart.getYAxis().setLabel("Humidity %");

                    updateChart();
                }
            }
        });

    }

    /**
     * This listener sets a limit for the amount of characters in a text field.
     */
    private void setCharAndJumpLimit(TextField textField, int charLimit, int jumpLimit) {
        textField.textProperty().addListener(l -> {

            if (textField.getText().length() > charLimit) {
                String s = textField.getText().substring(0, charLimit);
                textField.setText(s);
            }

            if (!textField.getText().equals("") && Integer.parseInt(textField.getText()) > jumpLimit) {
                textField.setText(Integer.toString(jumpLimit));
            }

            if (textField.getText().equals(""))
                textField.setPromptText("1-31");
            else
                jumpValue = Integer.parseInt(textField.getText());

        });
    }

    /**
     * This listener makes the text field only accept numbers and updates the day label to "day" if its a 1 day jump else to "days.
     */
    private void onlyNumbersMoreThanZero(TextField textField) {
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*") || newValue.matches("0")) {
                    textField.setText(newValue.replaceAll("[^\\d]", ""));
                    textField.setText(newValue.replaceAll("0", ""));
                }
                if (!textField.getText().equals("")) {
                    updateChart();
                    if (Integer.parseInt(textField.getText()) > 1)
                        dayLabel.setText("days.");
                    else
                        dayLabel.setText("day.");
                }
            }
        });
    }


    /**
     * This handler prevents whitespaces from the user and key text fields.
     */
    private EventHandler<KeyEvent> handler = new EventHandler<KeyEvent>() {

        private boolean willConsume = false;

        @Override
        public void handle(KeyEvent event) {

            if(willConsume)
                event.consume();


            if(event.getCode().isWhitespaceKey())
                willConsume = true;
            else
                willConsume = false;
        }

    };


    public ArrayList<TemperatureData> getData() {
        return data;
    }

    public DatePicker getDatePicker() {
        return datePicker;
    }

    public LineChart<String, Number> getChart() {
        return chart;
    }

    public CheckBox getNodeCheckBox() {
        return nodeCheckBox;
    }

    public Label getWeatherTempLabel() {
        return weatherTempLabel;
    }

    public Label getSensorTempLabel() {
        return sensorTempLabel;
    }

    public Label getDifTempLabel() {
        return difTempLabel;
    }

    public Label getShowingLabel() {
        return showingLabel;
    }

    public boolean isTemp() {
        return isTemp;
    }

}


