package com.bigbigwork.controllers;

import com.bigbigwork.util.ConfigUtil;
import com.bigbigwork.vo.Configure;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SchedulerController {
    @FXML
    public ComboBox<String> startHourComboBox;
    @FXML
    public ComboBox<String> stopMinuteComboBox;
    @FXML
    public ComboBox<String> stopHourComboBox;
    @FXML
    public ComboBox<String> startMinuteComboBox;

    @FXML
    public GridPane gridPane;

    @FXML
    public void initialize() {
        ObservableList<String> hours = FXCollections.observableArrayList();
        ObservableList<String> minutes = FXCollections.observableArrayList();
        List<String> h = IntStream.rangeClosed(1, 24).boxed().map(it -> String.format("%02d", it)).collect(Collectors.toList());
        List<String> m = IntStream.rangeClosed(0, 11).boxed().map(it -> String.format("%02d", it * 5)).collect(Collectors.toList());
        hours.addAll(h);
        minutes.addAll(m);
        startHourComboBox.setItems(hours);
        stopHourComboBox.setItems(hours);
        startMinuteComboBox.setItems(minutes);
        stopMinuteComboBox.setItems(minutes);

        try {
            Configure configure = ConfigUtil.getConfigure();
            startHourComboBox.getSelectionModel().select(configure.getStartHour());
            startMinuteComboBox.getSelectionModel().select(configure.getStartMinute());
            stopHourComboBox.getSelectionModel().select(configure.getStopHour());
            stopMinuteComboBox.getSelectionModel().select(configure.getStopMinute());
        } catch (Exception e) {

            e.printStackTrace();
        }

    }


    private boolean isBlank(String str){
        return null == str || str.isEmpty() || str.trim().isEmpty();
    }

    private boolean isNoneBlank(String str){
        return !isBlank(str);
    }



    public Optional<String> comb(String hour, String minute){
        Optional<String> optional = Optional.empty();
        if(isBlank(hour)) return optional;
        minute = Optional.ofNullable(minute).orElse("00");

        String concat = hour.concat(":").concat(minute);
        optional = Optional.of(concat);
        return optional;
    }

    public void save(ActionEvent actionEvent) {
        Configure configure = null;
        try {
            configure = ConfigUtil.getConfigure();
            String startHour = startHourComboBox.getSelectionModel().getSelectedItem();
            String startMinute = startMinuteComboBox.getSelectionModel().getSelectedItem();
            String stopHour = stopHourComboBox.getSelectionModel().getSelectedItem();
            String stopMinute = stopMinuteComboBox.getSelectionModel().getSelectedItem();
            Optional<String> startTime = comb(startHour, startMinute);
            Optional<String> stopTime = comb(stopHour, stopMinute);

            startTime.ifPresent(configure::setStartTime);
            stopTime.ifPresent(configure::setStopTime);
            ConfigUtil.updateConfigure(configure);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            System.out.println(configure);

            Stage stage = (Stage) gridPane.getScene().getWindow();
            stage.close();

        }

    }

    public void cancel(ActionEvent actionEvent) {
        Stage stage = (Stage) gridPane.getScene().getWindow();
        stage.close();
    }
}
