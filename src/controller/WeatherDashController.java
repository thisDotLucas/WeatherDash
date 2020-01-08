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

    private ArrayList<TemperatureData> data;
    private boolean isTemp;
    private boolean isLoading;
    private int monthValue;
    private int jumpValue;

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
    private TextField jumpTextField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox showByComboBox;

    @FXML
    private LineChart<String, Number> chart;

    @FXML
    private RadioButton radioTemperature;

    @FXML
    private RadioButton radioHumidity;


    @FXML
    private void datePickerAction() {
        if (showByComboBox.getValue() != null) {
            setMonthComboBoxItems();
            monthComboBox.setValue(1);
            updateChart();
        }
    }


    //Updates values when using combo box
    @FXML
    private void comboBoxPickerAction() {
        updateChart();
    }


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


    @FXML
    private void onNodeCheckBox() {
        updateChart();
    }


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


    private void initLineChart() {
        chart.getStylesheets().add("view/linechart.css");
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);
    }


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


    private void disableMonthSettings(boolean disable) {
        jumpTextField.setDisable(disable);
        monthComboBox.setDisable(disable);
    }


    private void resetMonthValues() {
        monthValue = 1;
        jumpValue = 3;
        monthComboBox.setValue(monthValue);
        jumpTextField.setText(Integer.toString(jumpValue));
    }


    private void initDatePicker() {

        datePicker.setEditable(false);
        datePicker.setValue(new TimeAndDateHelper().getDate().toLocalDate());
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
                setDisable(date.isBefore(LocalDate.of(2019, 6, 1)) || date.compareTo(LocalDate.now()) > 0);
            }
        });

    }


    private void addRadioButtonListener(ToggleGroup radioButtons) {
        radioButtons.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {

                if (isLoading) {
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
     * This listener makes the text field only accept numbers.
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


