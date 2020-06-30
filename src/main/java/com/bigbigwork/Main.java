package com.bigbigwork;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("(￣▽￣)~*");
        primaryStage.setScene(new Scene(root, 1000, 500));

        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            System.exit(0);
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
