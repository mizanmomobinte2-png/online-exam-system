package com.example.onlineexam.controller;

import com.example.onlineexam.model.Student;
import com.example.onlineexam.model.Teacher;
import com.example.onlineexam.model.User;
import com.example.onlineexam.service.AuthService;
import com.example.onlineexam.service.SessionManager;
import com.example.onlineexam.util.SceneUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username/email and password.");
            return;
        }

        User user = authService.login(username, password);
        if (user == null) {
            showError("Invalid username/email or password.");
            return;
        }

        Student student = null;
        Teacher teacher = null;
        if (user.getRole() == User.UserRole.STUDENT) {
            student = authService.getStudentByUserId(user.getUserId());
        } else if (user.getRole() == User.UserRole.TEACHER) {
            teacher = authService.getTeacherByUserId(user.getUserId());
        }

        SessionManager.setCurrentUser(user, student, teacher);

        try {
            if (user.getRole() == User.UserRole.STUDENT) {
                SceneUtil.loadFXML("/fxml/student/student-dashboard.fxml", "Student Dashboard");
            } else {
                SceneUtil.loadFXML("/fxml/teacher/teacher-dashboard.fxml", "Teacher Dashboard");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() throws IOException {
        SceneUtil.loadFXML("/fxml/welcome.fxml", "Online Exam System - Welcome");
    }

    @FXML
    private void goToRegister() throws IOException {
        SceneUtil.loadFXML("/fxml/register.fxml", "Online Exam System - Register");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
