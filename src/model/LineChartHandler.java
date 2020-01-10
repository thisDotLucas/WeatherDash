package model;

import controller.WeatherDashController;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;


public class LineChartHandler {

    private WeatherDashController controller;
    private ArrayList<TemperatureData> data;
    private String weatherApiAverage;
    private String sensorAverage;
    private String diffAverage;
    private String unit;

    /**
     * We get all the data from the controller which is given as an argument. The unit is set according
     * to what we want to display.
     */
    public LineChartHandler(WeatherDashController controller){
        this.controller = controller;
        this.data = controller.getData();
        if(controller.isTemp()) {
            unit = "°C";
        } else {
            unit = "%";
        }
    }

    /**
     * We go through the 24 hours in the day or less if current day is chosen. All of these values are put into ArrayLists
     * and graphed. When all values are graphed we calculate the average temperature for both the api and sensor values + the
     * average difference. Individual values are also available via the hoverable nodes which are also created if the node check
     * box is checked.
     */
    public void graphDay() {

        boolean getTemp = controller.isTemp();

        XYChart.Series officialSeries = new XYChart.Series();
        XYChart.Series sensorSeries = new XYChart.Series();

        ArrayList<Float> officialValues = new ArrayList<>();
        ArrayList<Float> sensorValues = new ArrayList<>();

        int i = getDayStartIndex();
        int count = 0;

        while(i >= 0 && data.get(i).getDateAsString().equals(controller.getDatePicker().getValue().toString())){

            TemperatureData officialTempData = data.get(i - 1);
            TemperatureData sensorTempData = data.get(i);

            XYChart.Data<String, Number> officialData = new XYChart.Data<>(officialTempData.getTimeAsString(), Float.parseFloat(officialTempData.getValue(getTemp)));
            XYChart.Data<String, Number> sensorData = new XYChart.Data<>(sensorTempData.getTimeAsString(), Float.parseFloat(sensorTempData.getValue(getTemp)));


            if(controller.getNodeCheckBox().isSelected()) {
                officialData.setNode(new HoveredThresholdNode((i == 0) ? 0 : Float.parseFloat(data.get(i - 1).getValue(getTemp)), Float.parseFloat(data.get(i).getValue(getTemp)), officialTempData.getTimeAsString(), true));
                sensorData.setNode(new HoveredThresholdNode((i == 0) ? 0 : Float.parseFloat(data.get(i).getValue(getTemp)), Float.parseFloat(data.get(i - 1).getValue(getTemp)), sensorTempData.getTimeAsString(), false));
            }

            officialValues.add(Float.parseFloat(officialTempData.getValue(getTemp)));
            sensorValues.add(Float.parseFloat(sensorTempData.getValue(getTemp)));

            officialSeries.getData().add(officialData);
            sensorSeries.getData().add(sensorData);

            i -= 2;
            count++;
        }

        float officialTempSum = 0;
        float sensorTempSum = 0;

        for(float x : officialValues)
            officialTempSum += x;

        for(float y : sensorValues)
            sensorTempSum += y;

        controller.getChart().getData().addAll(officialSeries, sensorSeries);
        controller.getWeatherTempLabel().setText(String.format("%.1f", officialTempSum / count) + unit);
        controller.getSensorTempLabel().setText(String.format("%.1f", sensorTempSum / count) + unit);
        controller.getDifTempLabel().setText(String.format("%.1f", Math.abs((officialTempSum - sensorTempSum)/count)) + unit);
        weatherApiAverage = controller.getWeatherTempLabel().getText();
        sensorAverage = controller.getSensorTempLabel().getText();
        diffAverage = controller.getDifTempLabel().getText();
    }

    /**
     * We go through the days in the week and their 24 hours. All days average temperatures or humidities are calculated and
     * put into another ArrayList. These values are then graphed and their average values and difference calculated. Individual
     * values are also available via the hoverable nodes which are also created if the node check box is checked.
     */
    public void graphWeek() {

        boolean getTemp = controller.isTemp();

        XYChart.Series officialSeries = new XYChart.Series();
        XYChart.Series sensorSeries = new XYChart.Series();

        ArrayList<Float> officialDayTemps = new ArrayList<>();
        ArrayList<Float> sensorDayTemps = new ArrayList<>();

        ArrayList<Float> officialTemps = new ArrayList<>();
        ArrayList<Float> sensorTemps = new ArrayList<>();

        int i = getWeekStartIndex();
        int count = 0;

        Date date = data.get(i).getDate();
        Date originalDate = date;

        while(i >= 1 && data.get(i).getWeek() == Integer.parseInt(new SimpleDateFormat("w").format(originalDate))){

            while(i >= 1 && data.get(i).getDateAsString().equals(new SimpleDateFormat("yyyy-MM-dd").format(date))){

                TemperatureData officialTempData = data.get(i - 1);
                TemperatureData sensorTempData = data.get(i);

                officialDayTemps.add(Float.parseFloat(officialTempData.getValue(getTemp)));
                sensorDayTemps.add(Float.parseFloat(sensorTempData.getValue(getTemp)));

                i -= 2;
                count++;

            }

            float officialDayTempSum = 0;
            float sensorDayTempSum = 0;

            for(float x : officialDayTemps)
                officialDayTempSum += x;

            for(float y : sensorDayTemps)
                sensorDayTempSum += y;

            officialTemps.add(officialDayTempSum / count);
            sensorTemps.add(sensorDayTempSum / count);

            XYChart.Data<String, Number> officialData = new XYChart.Data<>(new SimpleDateFormat("EEEE").format(date), officialDayTempSum / count);
            XYChart.Data<String, Number> sensorData = new XYChart.Data<>(new SimpleDateFormat("EEEE").format(date), sensorDayTempSum / count);

            if(controller.getNodeCheckBox().isSelected()) {
                officialData.setNode(new HoveredThresholdNode((i == 0) ? 0 : officialDayTempSum / count, sensorDayTempSum / count, new SimpleDateFormat("EEEE").format(date), true));
                sensorData.setNode(new HoveredThresholdNode((i == 0) ? 0 : sensorDayTempSum / count, officialDayTempSum / count, new SimpleDateFormat("EEEE").format(date), false));
            }

            officialSeries.getData().add(officialData);
            sensorSeries.getData().add(sensorData);

            officialDayTemps.clear();
            sensorDayTemps.clear();

            count = 0;

            date = addDays(date, 1);

        }

        float officialTempSum = 0;
        float sensorTempSum = 0;

        for(float x : officialTemps)
            officialTempSum += x;

        for(float y : sensorTemps)
            sensorTempSum += y;

        controller.getChart().getData().addAll(officialSeries, sensorSeries);
        controller.getWeatherTempLabel().setText(String.format("%.1f", officialTempSum / officialTemps.size()) + unit);
        controller.getSensorTempLabel().setText(String.format("%.1f", sensorTempSum / officialTemps.size()) + unit);
        controller.getDifTempLabel().setText(String.format("%.1f", Math.abs((officialTempSum - sensorTempSum))/officialTemps.size()) + unit);
        controller.getDifTempLabel().setText(String.format("%.1f", Math.abs((officialTempSum - sensorTempSum))/officialTemps.size()) + unit);
        weatherApiAverage = controller.getWeatherTempLabel().getText();
        sensorAverage = controller.getSensorTempLabel().getText();
        diffAverage = controller.getDifTempLabel().getText();
    }

    /**
     * We go through the days in the months that we want and calculate their averages. They are put into an ArrayList, these values are then graphed and
     * their averages are calculated. Individual values are also available via the hoverable nodes which are also created if the node check box is checked.
     */
    public void graphMonth(int nMonths, int jump){

        assert jump % 2 == 0: "Jump value must be a even integer.";

        boolean getTemp = controller.isTemp();

        XYChart.Series officialSeries = new XYChart.Series();
        XYChart.Series sensorSeries = new XYChart.Series();

        ArrayList<Float> officialJumpDayTemps = new ArrayList<>();
        ArrayList<Float> sensorJumpDayTemps = new ArrayList<>();

        ArrayList<Float> officialTemps = new ArrayList<>();
        ArrayList<Float> sensorTemps = new ArrayList<>();

        int i = getMonthStartIndex(nMonths);
        int count = 0;

        Date date = data.get(i).getDate();
        Date originalDate = date;

        while(i >= jump && getMonths(originalDate, nMonths).contains(data.get(i).getMonth())){

            while(i >= jump && getDates(date, jump).contains(data.get(i).getDateAsString())){

                TemperatureData officialTempData = data.get(i - 1);
                TemperatureData sensorTempData = data.get(i);

                officialJumpDayTemps.add(Float.parseFloat(officialTempData.getValue(getTemp)));
                sensorJumpDayTemps.add(Float.parseFloat(sensorTempData.getValue(getTemp)));

                i -= jump;
                count++;
            }

            float officialDayTempSum = 0;
            float sensorDayTempSum = 0;

            for(float x : officialJumpDayTemps)
                officialDayTempSum += x;

            for(float y : sensorJumpDayTemps)
                sensorDayTempSum += y;

            officialTemps.add(officialDayTempSum / count);
            sensorTemps.add(sensorDayTempSum / count);

            XYChart.Data<String, Number> officialData = new XYChart.Data<>(new SimpleDateFormat("dd-MM").format(date), officialDayTempSum / count);
            XYChart.Data<String, Number> sensorData = new XYChart.Data<>(new SimpleDateFormat("dd-MM").format(date), sensorDayTempSum / count);

            if(controller.getNodeCheckBox().isSelected()) {
                officialData.setNode(new HoveredThresholdNode((i == 0) ? 0 : officialDayTempSum / count, sensorDayTempSum / count, new SimpleDateFormat("dd-MM").format(date), true));
                sensorData.setNode(new HoveredThresholdNode((i == 0) ? 0 : sensorDayTempSum / count, officialDayTempSum / count, new SimpleDateFormat("dd-MM").format(date), false));
            }

            officialSeries.getData().add(officialData);
            sensorSeries.getData().add(sensorData);

            officialJumpDayTemps.clear();
            sensorJumpDayTemps.clear();

            count = 0;
            date = addDays(date, jump/2);
        }

        float officialTempSum = 0;
        float sensorTempSum = 0;

        for(float x : officialTemps)
            officialTempSum += x;

        for(float y : sensorTemps)
            sensorTempSum += y;

        controller.getChart().getData().addAll(officialSeries, sensorSeries);
        controller.getWeatherTempLabel().setText(String.format("%.1f", officialTempSum / officialTemps.size()) + unit);
        controller.getSensorTempLabel().setText(String.format("%.1f", sensorTempSum / officialTemps.size()) + unit);
        controller.getDifTempLabel().setText(String.format("%.1f", Math.abs((officialTempSum - sensorTempSum))/officialTemps.size()) + unit);
        weatherApiAverage = controller.getWeatherTempLabel().getText();
        sensorAverage = controller.getSensorTempLabel().getText();
        diffAverage = controller.getDifTempLabel().getText();
    }

    /**
     * We keep on going forward in thew list until we reach the date we are seeking. At this point we are at the end of the day we want.
     * So now we keep on going forward until we are no longer at the date given. At this point we are at the end of the day before the first
     * hour of the date we are looking for so now we can return the current index - 1.
     */
    private int getDayStartIndex(){

        String date = controller.getDatePicker().getValue().toString();

        int i = 0;

        while (!data.get(i).getDateAsString().equals(date))
            i++;

        while (data.get(i).getDateAsString().equals(date))
            i++;

        return --i;
    }

    /**
     * We keep on going forward in thew list until we reach the week number we are seeking. At this point we are at the end of the week we want.
     * So now we keep on going forward until we are no longer at the week given. At this point we are at the end of the week before the first
     * day and hour of the week we are looking for so now we can return the current index - 1.
     */
    private int getWeekStartIndex(){

        TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
        int week = controller.getDatePicker().getValue().get(woy);

        int i = 0;

        while (data.get(i).getWeek() != week)
            i++;

        while (data.get(i).getWeek() == week)
            i++;

        return --i;

    }

    /**
     * We keep on going forward in thew list until we reach the month we are seeking. At this point we are at the end of the month we want.
     * So now we keep on going forward until we are no longer at the month given. At this point we are at the end of the month before the first
     * day and hour of the month we are looking for so now we can return the current index - 1.
     */
    private int getMonthStartIndex(int n){
        int month = controller.getDatePicker().getValue().getMonthValue() - (n - 1);

        if(month <= 0)
            month = 12 + month;

        int i = 0;

        while (data.get(i).getMonth() != month)
            i++;

        while (data.get(i).getMonth() == month)
            i++;

        return --i;
    }

    /**
     * Adds the amount of days to the date given as an argument by the days given as an argument.
     */
    private Date addDays(Date date, int days) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.DATE, + days);

        return cal.getTime();
    }

    /**
     * Gets the month numbers we are interested in and puts them in in an ArrayList.
     */
    private ArrayList<Integer> getMonths(Date date, int n) {

        ArrayList<Integer> months = new ArrayList<>();
        int month = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue();

        for (int i = 0; i < n; i++){

            if (month + i > 12) {
                month = 1;
                i = 0;
            }

            months.add(month + i);

        }

        return months;
    }

    /**
     * Puts the dates in the given day jump in an ArrayList.
     */
    private ArrayList<String> getDates(Date date, int n){

        ArrayList<String> dates = new ArrayList<>();

        for(int i = 0; i < n; i++)
            dates.add(new SimpleDateFormat("yyyy-MM-dd").format(addDays(date, i)));

        return dates;
    }

    /**
     * This class creates the nodes with the hovering functionality.
     */
    class HoveredThresholdNode extends StackPane {
        HoveredThresholdNode(float value, float otherValue, String time, boolean isOfficial) {
            setPrefSize(10, 10);

            String color;

            if (isOfficial)
                color = "default-color0";
            else
                color = "default-color1";

            final Label label = createDataThresholdLabel(value, color);

            setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent mouseEvent) {
                    getChildren().setAll(label);
                    setCursor(Cursor.NONE);
                    toFront();
                    if(isOfficial) {
                        controller.getWeatherTempLabel().setText(String.format("%.1f", value) + unit);
                        controller.getSensorTempLabel().setText(String.format("%.1f", otherValue) + unit);
                    } else {
                        controller.getWeatherTempLabel().setText(String.format("%.1f", otherValue) + unit);
                        controller.getSensorTempLabel().setText(String.format("%.1f", value) + unit);
                    }
                    controller.getShowingLabel().setText(controller.getShowingLabel().getText() + ", " + time);
                    controller.getDifTempLabel().setText(String.format("%.1f", Math.abs(value - otherValue)) + unit);
                }
            });
            setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent mouseEvent) {
                    getChildren().clear();
                    setCursor(Cursor.CROSSHAIR);
                    controller.getWeatherTempLabel().setText(weatherApiAverage);
                    controller.getSensorTempLabel().setText(sensorAverage);
                    controller.getDifTempLabel().setText(diffAverage);
                    controller.getShowingLabel().setText(controller.getShowingLabel().getText().replaceAll(", " + time, ""));
                }
            });
        }

        private Label createDataThresholdLabel(float value, String color) {
            final Label label = new Label(String.format("%.1f", value) + unit);
            label.getStyleClass().addAll(color, "chart-line-symbol", "chart-series-line");
            label.setStyle("-fx-font-size: 14;");

            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }
    }


}
