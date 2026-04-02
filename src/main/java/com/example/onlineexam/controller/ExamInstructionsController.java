package com.example.onlineexam.controller;

import com.example.onlineexam.dao.QuestionDao;
import com.example.onlineexam.model.Exam;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ExamInstructionsController {

    @FXML private Label examTitleLabel;
    @FXML private Label detailsLabel;
    @FXML private TextArea instructionsArea;
    @FXML private CheckBox acceptCheckbox;
    @FXML private Button startButton;

    private Exam exam;
    private final QuestionDao questionDao = new QuestionDao();

    @FXML
    public void initialize() {
        startButton.setDisable(true);

        acceptCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            startButton.setDisable(!newVal);
        });
    }

    public void setExam(Exam ex) {
        this.exam = ex;

        if (exam != null) {
            examTitleLabel.setText(exam.getTitle());

            int qCount = questionDao.findByExamId(exam.getExamId()).size();

            detailsLabel.setText(
                    "Duration: " + exam.getDurationMinutes() +
                    " min | Questions: " + qCount +
                    " | Total Marks: " + exam.getTotalMarks()
            );

            instructionsArea.setText(
                    "1. You have " + exam.getDurationMinutes() + " minutes to complete this exam.\n" +
                    "2. Each question has four options. Select one answer.\n" +
                    "3. You can navigate between questions using Previous/Next.\n" +
                    "4. Click 'Submit Exam' when done. The exam will auto-submit when time expires.\n" +
                    "5. Do not switch windows or copy-paste during the exam.\n" +
                    "6. Good luck!"
            );
        }
    }

    @FXML
    private void startExam() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student/take-exam.fxml"));
            Parent root = loader.load();

            TakeExamController controller = loader.getController();
            controller.setExam(exam);

            Stage stage = new Stage();
            stage.setTitle("Take Exam");
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) startButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) examTitleLabel.getScene().getWindow();
        stage.close();
    }
}