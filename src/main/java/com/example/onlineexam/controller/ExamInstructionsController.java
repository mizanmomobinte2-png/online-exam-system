package com.example.onlineexam.controller;

import com.example.onlineexam.dao.ExamDao;
import com.example.onlineexam.dao.ExamAttemptDao;
import com.example.onlineexam.dao.QuestionDao;
import com.example.onlineexam.model.Exam;
import com.example.onlineexam.model.ExamAttempt;
import com.example.onlineexam.service.SessionManager;
import com.example.onlineexam.util.SceneUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class ExamInstructionsController {

    @FXML private Label examTitleLabel;
    @FXML private Label detailsLabel;
    @FXML private TextArea instructionsArea;
    @FXML private CheckBox acceptCheckbox;
    @FXML private Button startButton;

    private Exam exam;
    private final ExamDao examDao = new ExamDao();
    private final ExamAttemptDao attemptDao = new ExamAttemptDao();
    private final QuestionDao questionDao = new QuestionDao();

    public void setExam(Exam ex) {
        this.exam = ex;
        if (exam != null) {
            examTitleLabel.setText(exam.getTitle());
            int qCount = questionDao.findByExamId(exam.getExamId()).size();
            detailsLabel.setText("Duration: " + exam.getDurationMinutes() + " min | Questions: " + qCount + " | Total Marks: " + exam.getTotalMarks());
            instructionsArea.setText(
                "1. You have " + exam.getDurationMinutes() + " minutes to complete this exam.\n" +
                "2. Each question has four options. Select one answer.\n" +
                "3. You can navigate between questions using Previous/Next.\n" +
                "4. Click 'Submit Exam' when done. The exam will auto-submit when time expires.\n" +
                "5. Do not switch windows or copy-paste during the exam.\n" +
                "6. Good luck!"
            );
        }
        acceptCheckbox.selectedProperty().addListener((obs, old, val) -> startButton.setDisable(!val));
    }

    @FXML
    private void startExam() {
        if (exam == null) return;
        int studentId = SessionManager.getCurrentStudent().getStudentId();
        ExamAttempt existing = attemptDao.findByExamAndStudent(exam.getExamId(), studentId);
        if (existing != null) {
            return; // already attempted
        }
        ExamAttempt attempt = new ExamAttempt();
        attempt.setExamId(exam.getExamId());
        attempt.setStudentId(studentId);
        attempt.setStartTime(LocalDateTime.now());
        attempt.setTotalMarks(exam.getTotalMarks());
        int attemptId = attemptDao.create(attempt);
        if (attemptId <= 0) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student/take-exam.fxml"));
            Parent root = loader.load();
            TakeExamController ctrl = loader.getController();
            ctrl.setExamAndAttempt(exam, attemptId);
            Stage stage = (Stage) examTitleLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Exam: " + exam.getTitle());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) examTitleLabel.getScene().getWindow();
        stage.close();
    }
}
