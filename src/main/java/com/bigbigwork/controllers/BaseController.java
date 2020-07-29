package com.bigbigwork.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

public class BaseController {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    protected static final Supplier<String> rightNow = () -> LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    @FXML
    public BorderPane boarderPane;

    public void period() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/period.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setTitle("访问频率配置");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        }catch (Exception e){
            LOG.error("period error:", e);
        }
    }

    public void scheculer() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/scheduler.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setTitle("重启计划配置");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        }catch (Exception e){
            LOG.error("scheduler error:", e);
        }
    }
    public void startRecord(){
        try {
            ((Stage) boarderPane.getScene().getWindow()).setIconified(true);

            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/record.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage stage = new Stage();
            rightCornerStage(stage,600,400);
//            stage.initStyle(StageStyle.TRANSPARENT);
//            scene.setFill(null);
            stage.toBack();
            stage.setResizable(false);
            stage.setTitle("录屏教学");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        }catch (Exception e){
            LOG.error("scheduler error:", e);
        }
    }

    public void config() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/config.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setTitle("脚本配置");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        }catch (Exception e){
            LOG.error("scheduler error:", e);
        }
    }

    private void rightCornerStage(Stage stage, double width, double height) {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        //set Stage boundaries to the lower right corner of the visible bounds of the main screen
        stage.setX(primaryScreenBounds.getMinX() + primaryScreenBounds.getWidth() - width);
        stage.setY(primaryScreenBounds.getMinY() + primaryScreenBounds.getHeight() - height);
    }

}
