package com.example.onlineexam.controller;

import com.example.onlineexam.dao.ExamAttemptDao;
import com.example.onlineexam.dao.ExamDao;
import com.example.onlineexam.model.Exam;
import com.example.onlineexam.model.ExamAttempt;
import com.example.onlineexam.util.SceneUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;

public class ResultScreenController {

    @FXML private Label examTitleLabel;
    @FXML private Label scoreLabel;
    @FXML private Label totalLabel;
    @FXML private Label percentLabel;
    @FXML private Label passFailLabel;
    @FXML private Label noteLabel;

    private int attemptId;
    private ExamAttempt attempt;
    private Exam exam;

    private final ExamAttemptDao attemptDao = new ExamAttemptDao();
    private final ExamDao examDao = new ExamDao();

    /** TakeExamController / History থেকে call করবে */
    public void setAttemptId(int attemptId) {
        this.attemptId = attemptId;

        this.attempt = attemptDao.findById(attemptId);
        if (attempt == null) {
            passFailLabel.setText("Result not found!");
            passFailLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
            return;
        }

        this.exam = examDao.findById(attempt.getExamId());

        int score = attempt.getScore();
        int total = attempt.getTotalMarks();
        double percent = total > 0 ? (100.0 * score / total) : 0;

        int passMarks = (exam != null ? exam.getPassingMarks() : 0);
        boolean passed = score >= passMarks;

        examTitleLabel.setText("Exam: " + (exam != null ? exam.getTitle() : "Unknown"));
        scoreLabel.setText(String.valueOf(score));
        totalLabel.setText(String.valueOf(total));
        percentLabel.setText(String.format("%.1f%%", percent));

        if (passed) {
            passFailLabel.setText("✅ PASS");
            passFailLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-weight: bold;");
            noteLabel.setText("Congratulations! You passed (Pass Marks: " + passMarks + ")");
        } else {
            passFailLabel.setText("❌ FAIL");
            passFailLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
            noteLabel.setText("You did not pass (Pass Marks: " + passMarks + ")");
        }
    }

    @FXML
    private void goReview() {
        // attempt null হলে crash avoid
        if (attempt == null) return;

        try {
            ReviewScreenController.open(attemptId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goHistory() {
        try {
            SceneUtil.loadFXML("/fxml/student/student-results.fxml", "My Results");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goDashboard() {
        try {
            SceneUtil.loadFXML("/fxml/student/student-dashboard.fxml", "Student Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
