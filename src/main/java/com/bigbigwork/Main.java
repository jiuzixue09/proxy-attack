package com.bigbigwork;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("(￣▽￣)~*");
        rightCornerStage(primaryStage,1000,500);
        primaryStage.setScene(new Scene(root, 1000, 500));

        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            System.exit(0);
        });

    }

    private void rightCornerStage(Stage stage, double width, double height) {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        //set Stage boundaries to the lower right corner of the visible bounds of the main screen
        stage.setX(primaryScreenBounds.getMinX() + primaryScreenBounds.getWidth() - width);
        stage.setY(primaryScreenBounds.getMinY() + primaryScreenBounds.getHeight() - height);
    }


    public static void main(String[] args) {
        if(Objects.nonNull(args) && args.length > 0){
            System.setProperty("AUTOT_RAFFIC_ATTACK", args[0]);
        }
        launch(args);
    }
}
