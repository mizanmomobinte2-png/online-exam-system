package com.example.onlineexam.controller;

import com.example.onlineexam.service.SessionManager;
import com.example.onlineexam.util.SceneUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;

public class TeacherDashboardController {

    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentTeacher() != null) {
            welcomeLabel.setText("Welcome, " + SessionManager.getCurrentTeacher().getFullName() + "!");
        }
    }

    @FXML
    private void goToDashboard() throws IOException {
        SceneUtil.loadFXML("/fxml/teacher/teacher-dashboard.fxml", "Teacher Dashboard");
    }

    @FXML
    private void goToCreateExam() throws IOException {
        SceneUtil.loadFXML("/fxml/teacher/create-exam.fxml", "Create Exam");
    }

    @FXML
    private void goToMyExams() throws IOException {
        SceneUtil.loadFXML("/fxml/teacher/my-exams.fxml", "My Exams");
    }

    @FXML
    private void goToResults() throws IOException {
        SceneUtil.loadFXML("/fxml/teacher/teacher-results.fxml", "Results & Reports");
    }

    @FXML
    private void logout() throws IOException {
        SessionManager.logout();
        SceneUtil.loadFXML("/fxml/welcome.fxml", "Online Exam System - Welcome");
    }
}
