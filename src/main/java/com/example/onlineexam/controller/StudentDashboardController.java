package com.example.onlineexam.controller;

import com.example.onlineexam.service.SessionManager;
import com.example.onlineexam.util.SceneUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class StudentDashboardController {

    @FXML private javafx.scene.control.Label welcomeLabel;

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentStudent() != null) {
            welcomeLabel.setText("Welcome, " + SessionManager.getCurrentStudent().getFullName() + "!");
        }
    }

    @FXML
    private void goToDashboard() throws IOException {
        SceneUtil.loadFXML("/fxml/student/student-dashboard.fxml", "Student Dashboard");
    }

    @FXML
    private void goToAvailableExams() throws IOException {
        SceneUtil.loadFXML("/fxml/student/available-exams.fxml", "Available Exams");
    }

    @FXML
    private void goToResults() throws IOException {
        SceneUtil.loadFXML("/fxml/student/student-results.fxml", "My Results");
    }

    @FXML
    private void logout() throws IOException {
        SessionManager.logout();
        SceneUtil.loadFXML("/fxml/welcome.fxml", "Online Exam System - Welcome");
    }
}
