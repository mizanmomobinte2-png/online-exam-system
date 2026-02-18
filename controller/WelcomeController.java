package com.example.onlineexam.controller;

import com.example.onlineexam.util.SceneUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeController {

    @FXML
    private void goToLogin() throws IOException {
        SceneUtil.loadFXML("/fxml/login.fxml", "Online Exam System - Login");
    }

    @FXML
    private void goToRegister() throws IOException {
        SceneUtil.loadFXML("/fxml/register.fxml", "Online Exam System - Register");
    }
}
