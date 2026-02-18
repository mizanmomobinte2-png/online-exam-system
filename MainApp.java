package com.example.onlineexam;

import com.example.onlineexam.util.SceneUtil;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneUtil.setPrimaryStage(primaryStage);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        try {
            SceneUtil.loadFXML("/fxml/welcome.fxml", "Online Exam System - Welcome");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
