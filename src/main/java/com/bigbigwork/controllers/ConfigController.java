package com.bigbigwork.controllers;

import com.bigbigwork.util.ConfigUtil;
import com.bigbigwork.vo.Configure;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class ConfigController implements Initializable {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @FXML
    public GridPane gridPane;
    @FXML
    public TextArea stopAppScript;
    @FXML
    public TextArea startAppScript;
    @FXML
    public TextArea findAppScript;
    @FXML
    public TextArea findProxyScript;
    @FXML
    public TextArea startPython;

    public void cancel(ActionEvent actionEvent) {
        Stage stage = (Stage) gridPane.getScene().getWindow();
        stage.close();
    }

    public void save(ActionEvent actionEvent) {
        try {
            Configure configure = ConfigUtil.getConfigure();
            configure.setStopAPPScript(stopAppScript.getText());
            configure.setStartAPPScript(startAppScript.getText());
            configure.setFindAPPScript(findAppScript.getText());
            configure.setFindProxyScript(findProxyScript.getText());
            configure.setStartPython(startPython.getText());

            ConfigUtil.updateConfigure(configure);
            LOG.info("update configure:{}",configure);
            Stage stage = (Stage) gridPane.getScene().getWindow();
            stage.close();
        }catch (Exception e){
            LOG.error("update configure error:",e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Configure configure = ConfigUtil.getConfigure();
            stopAppScript.setText(configure.getStopAPPScript());
            startAppScript.setText(configure.getStartAPPScript());
            findAppScript.setText(configure.getFindAPPScript());
            findProxyScript.setText(configure.getFindProxyScript());
            startPython.setText(configure.getStartPython());
        } catch (Exception e) {
            LOG.error("read configure error:",e);
            e.printStackTrace();
        }
    }
}
