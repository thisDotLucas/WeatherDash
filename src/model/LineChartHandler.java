package model;

import controller.WeatherDashController;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
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

    public LineChartHandler(WeatherDashController controller){
        this.controller = controller;
        this.data = controller.getData();
    }


    public void graphDay() {

        XYChart.Series officialSeries = new XYChart.Series();
        XYChart.Series sensorSeries = new XYChart.Series();

        ArrayList<Float> officialTemps = new ArrayList<>();
        ArrayList<Float> sensorTemps = new ArrayList<>();

        int i = getDayStartIndex();
        int count = 0;

        while(i >= 0 && data.get(i).getDateAsString().equals(controller.getDatePicker().getValue().toString())){

            TemperatureData officialTempData = data.get(i - 1);
            TemperatureData sensorTempData = data.get(i);

            XYChart.Data<String, Number> officialData = new XYChart.Data<>(officialTempData.getTimeAsString(), Float.parseFloat(officialTempData.getTemperature()));
            XYChart.Data<String, Number> sensorData = new XYChart.Data<>(sensorTempData.getTimeAsString(), Float.parseFloat(sensorTempData.getTemperature()));

            officialData.setNode(new HoveredThresholdNode((i == 0) ? 0 : Float.parseFloat(data.get(i - 1).getTemperature()), Float.parseFloat(data.get(i).getTemperature()), officialTempData.getTimeAsString(), true));
            sensorData.setNode(new HoveredThresholdNode((i == 0) ? 0 : Float.parseFloat(data.get(i).getTemperature()), Float.parseFloat(data.get(i - 1).getTemperature()), sensorTempData.getTimeAsString(), false));

            officialTemps.add(Float.parseFloat(officialTempData.getTemperature()));
            sensorTemps.add(Float.parseFloat(sensorTempData.getTemperature()));

            officialSeries.getData().add(officialData);
            sensorSeries.getData().add(sensorData);

            i -= 2;
            count++;
        }

        float officialTempSum = 0;
        float sensorTempSum = 0;

        for(float x : officialTemps)
            officialTempSum += x;

        for(float y : sensorTemps)
            sensorTempSum += y;

        controller.getChart().getData().addAll(officialSeries, sensorSeries);
        controller.getWeatherTempLabel().setText(String.format("%.1f", officialTempSum / count) + "°C");
        controller.getSensorTempLabel().setText(String.format("%.1f", sensorTempSum / count) + "°C");
        controller.getDifTempLabel().setText(String.format("%.1f", Math.abs(officialTempSum/count - sensorTempSum/count)) + "°C");
        weatherApiAverage = controller.getWeatherTempLabel().getText();
        sensorAverage = controller.getSensorTempLabel().getText();
        diffAverage = controller.getDifTempLabel().getText();
    }


    public void graphWeek() {

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

                officialDayTemps.add(Float.parseFloat(officialTempData.getTemperature()));
                sensorDayTemps.add(Float.parseFloat(sensorTempData.getTemperature()));

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

            officialData.setNode(new HoveredThresholdNode((i == 0) ? 0 : officialDayTempSum / count, sensorDayTempSum / count, new SimpleDateFormat("EEEE").format(date), true));
            sensorData.setNode(new HoveredThresholdNode((i == 0) ? 0 : sensorDayTempSum / count, officialDayTempSum / count, new SimpleDateFormat("EEEE").format(date), false));

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
        controller.getWeatherTempLabel().setText(String.format("%.1f", officialTempSum / officialTemps.size()) + "°C");
        controller.getSensorTempLabel().setText(String.format("%.1f", sensorTempSum / officialTemps.size()) + "°C");
        controller.getDifTempLabel().setText(String.format("%.1f", Math.abs((officialTempSum - sensorTempSum))/officialTemps.size()) + "°C");
        controller.getDifTempLabel().setText(String.format("%.1f", Math.abs((officialTempSum - sensorTempSum))/officialTemps.size()) + "°C");
        weatherApiAverage = controller.getWeatherTempLabel().getText();
        sensorAverage = controller.getSensorTempLabel().getText();
        diffAverage = controller.getDifTempLabel().getText();
    }


    public void graphMonth(int nMonths, int jump){

        XYChart.Series officialSeries = new XYChart.Series();
        XYChart.Series sensorSeries = new XYChart.Series();

        ArrayList<Float> officialThreeDayTemps = new ArrayList<>();
        ArrayList<Float> sensorThreeDayTemps = new ArrayList<>();

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

                officialThreeDayTemps.add(Float.parseFloat(officialTempData.getTemperature()));
                sensorThreeDayTemps.add(Float.parseFloat(sensorTempData.getTemperature()));

                i -= jump;
                count++;
            }

            float officialDayTempSum = 0;
            float sensorDayTempSum = 0;

            for(float x : officialThreeDayTemps)
                officialDayTempSum += x;

            for(float y : sensorThreeDayTemps)
                sensorDayTempSum += y;

            officialTemps.add(officialDayTempSum / count);
            sensorTemps.add(sensorDayTempSum / count);

            XYChart.Data<String, Number> officialData = new XYChart.Data<>(new SimpleDateFormat("dd-MM").format(date), officialDayTempSum / count);
            XYChart.Data<String, Number> sensorData = new XYChart.Data<>(new SimpleDateFormat("dd-MM").format(date), sensorDayTempSum / count);

            officialData.setNode(new HoveredThresholdNode((i == 0) ? 0 : officialDayTempSum / count, sensorDayTempSum / count, new SimpleDateFormat("dd-MM").format(date), true));
            sensorData.setNode(new HoveredThresholdNode((i == 0) ? 0 : sensorDayTempSum / count, officialDayTempSum / count, new SimpleDateFormat("dd-MM").format(date), false));

            officialSeries.getData().add(officialData);
            sensorSeries.getData().add(sensorData);

            officialThreeDayTemps.clear();
            sensorThreeDayTemps.clear();

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
        controller.getWeatherTempLabel().setText(String.format("%.1f", officialTempSum / officialTemps.size()) + "°C");
        controller.getSensorTempLabel().setText(String.format("%.1f", sensorTempSum / officialTemps.size()) + "°C");
        controller.getDifTempLabel().setText(String.format("%.1f", Math.abs((officialTempSum - sensorTempSum))/officialTemps.size()) + "°C");
        weatherApiAverage = controller.getWeatherTempLabel().getText();
        sensorAverage = controller.getSensorTempLabel().getText();
        diffAverage = controller.getDifTempLabel().getText();
    }


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

    private int getDayStartIndex(){

        String date = controller.getDatePicker().getValue().toString();

        int i = 0;

        while (!data.get(i).getDateAsString().equals(date))
            i++;

        while (data.get(i).getDateAsString().equals(date))
            i++;

        return --i;
    }

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


    public Date addDays(Date date, int days) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.DATE, + days);

        return cal.getTime();
    }

    private ArrayList<Integer> getMonths(Date date, int n){

        ArrayList<Integer> months = new ArrayList<>();
        int month = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue();

        for(int i = 0; i < n; i++)
            months.add(month + i);

        return months;
    }

    private ArrayList<String> getDates(Date date, int n){

        ArrayList<String> dates = new ArrayList<>();

        for(int i = 0; i < n; i++)
            dates.add(new SimpleDateFormat("yyyy-MM-dd").format(addDays(date, i)));

        return dates;
    }

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
                        controller.getWeatherTempLabel().setText(String.format("%.1f", value) + "°C");
                        controller.getSensorTempLabel().setText(String.format("%.1f", otherValue) + "°C");
                    } else {
                        controller.getWeatherTempLabel().setText(String.format("%.1f", otherValue) + "°C");
                        controller.getSensorTempLabel().setText(String.format("%.1f", value) + "°C");
                    }
                    controller.getShowingLabel().setText(controller.getShowingLabel().getText() + " " + time);
                    controller.getDifTempLabel().setText(String.format("%.1f", Math.abs(value - otherValue)) + "°C");
                }
            });
            setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override public void handle(MouseEvent mouseEvent) {
                    getChildren().clear();
                    setCursor(Cursor.CROSSHAIR);
                    controller.getWeatherTempLabel().setText(weatherApiAverage);
                    controller.getSensorTempLabel().setText(sensorAverage);
                    controller.getDifTempLabel().setText(diffAverage);
                    controller.getShowingLabel().setText(controller.getShowingLabel().getText().replaceAll(" " + time, ""));
                }
            });
        }

        private Label createDataThresholdLabel(float value, String color) {
            final Label label = new Label(String.format("%.1f", value) + "°C");
            label.getStyleClass().addAll(color, "chart-line-symbol", "chart-series-line");
            label.setStyle("-fx-font-size: 14;");

            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }
    }


}
