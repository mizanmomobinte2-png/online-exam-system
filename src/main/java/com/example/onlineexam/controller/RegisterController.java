package com.example.onlineexam.controller;

import com.example.onlineexam.service.AuthService;
import com.example.onlineexam.util.SceneUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class RegisterController implements Initializable {

    @FXML private RadioButton studentRadio;
    @FXML private RadioButton teacherRadio;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private TextField enrollmentField;
    @FXML private TextField departmentField;
    @FXML private TextField contactField;
    @FXML private Label errorLabel;

    private ToggleGroup roleGroup;
    private final AuthService authService = new AuthService();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleGroup = studentRadio.getToggleGroup();
        if (roleGroup != null) roleGroup.selectedToggleProperty().addListener((obs, old, selected) -> {
            boolean isStudent = selected == studentRadio;
            enrollmentField.setVisible(isStudent);
            enrollmentField.setManaged(isStudent);
            departmentField.setVisible(!isStudent);
            departmentField.setManaged(!isStudent);
        });
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        String fullName = fullNameField.getText().trim();
        String contact = contactField.getText().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            showError("Please fill all required fields.");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Invalid email format.");
            return;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }

        boolean isStudent = studentRadio.isSelected();
        if (isStudent) {
            String enrollment = enrollmentField.getText().trim();
            if (enrollment.isEmpty()) {
                showError("Enrollment number is required for students.");
                return;
            }
            if (authService.registerStudent(username, email, password, fullName, enrollment, contact)) {
                try {
                    SceneUtil.loadFXML("/fxml/login.fxml", "Online Exam System - Login");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                showError("Registration failed. Please try again.");
            }
        } else {
            String department = departmentField.getText().trim();
            if (authService.registerTeacher(username, email, password, fullName, department, contact)) {
                try {
                    SceneUtil.loadFXML("/fxml/login.fxml", "Online Exam System - Login");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                showError("Registration failed. Please try again.");
            }
        }
    }

    @FXML
    private void goBack() throws IOException {
        SceneUtil.loadFXML("/fxml/welcome.fxml", "Online Exam System - Welcome");
    }

    @FXML
    private void goToLogin() throws IOException {
        SceneUtil.loadFXML("/fxml/login.fxml", "Online Exam System - Login");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
