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

import java.util.Arrays;
import java.util.List;

public class PeriodController {
    @FXML
    public GridPane gridPane;
    @FXML
    public ComboBox<Integer> requestsComboBox;
    @FXML
    public ComboBox<Integer> sleepTimeComboBox;
    @FXML
    public ComboBox<Integer> periodComboBox;
    @FXML
    public ComboBox<Integer> errorPeriodComboBox;


    @FXML
    public void initialize() {
        ObservableList<Integer> observableList = FXCollections.observableArrayList();
        List<Integer> list = Arrays.asList(0,1,2,3,4,5,10,15,20,30,40,50,60,80,100,200,500,1000);
        observableList.addAll(list);

        requestsComboBox.setItems(observableList);
        sleepTimeComboBox.setItems(observableList);
        periodComboBox.setItems(observableList);
        errorPeriodComboBox.setItems(observableList);

        try {
            Configure configure = ConfigUtil.getConfigure();
            requestsComboBox.getSelectionModel().select(configure.getRequests());
            sleepTimeComboBox.getSelectionModel().select(configure.getSleepTime());
            periodComboBox.getSelectionModel().select(configure.getPeriod());
            errorPeriodComboBox.getSelectionModel().select(configure.getErrorPeriod());
        } catch (Exception e) {

            e.printStackTrace();
        }

    }


    public void cancel(ActionEvent actionEvent) {
        Stage stage = (Stage) gridPane.getScene().getWindow();
        stage.close();
    }

    public void save(ActionEvent actionEvent) {
        Configure configure = null;
        try {
            configure = ConfigUtil.getConfigure();
            Integer requests = requestsComboBox.getSelectionModel().getSelectedItem();
            Integer sleepTime = sleepTimeComboBox.getSelectionModel().getSelectedItem();
            Integer period = periodComboBox.getSelectionModel().getSelectedItem();
            Integer errorPeriod = errorPeriodComboBox.getSelectionModel().getSelectedItem();
            configure.setRequests(requests);
            configure.setSleepTime(sleepTime);
            configure.setPeriod(period);
            configure.setErrorPeriod(errorPeriod);
            ConfigUtil.updateConfigure(configure);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            System.out.println(configure);

            Stage stage = (Stage) gridPane.getScene().getWindow();
            stage.close();

        }
    }
}
