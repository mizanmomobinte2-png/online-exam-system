package com.example.onlineexam.controller;

import com.example.onlineexam.dao.ExamAttemptDao;
import com.example.onlineexam.dao.QuestionDao;
import com.example.onlineexam.dao.StudentAnswerDao;
import com.example.onlineexam.model.Exam;
import com.example.onlineexam.model.Question;
import com.example.onlineexam.service.SessionManager;
import com.example.onlineexam.util.SceneUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TakeExamController {

    @FXML private Label timerLabel;
    @FXML private Label questionNumberLabel;
    @FXML private Label questionTextLabel;
    @FXML private VBox optionsBox;
    @FXML private FlowPane paletteFlow;
    @FXML private VBox questionPanel;

    private Exam exam;
    private int attemptId;
    private List<Question> questions;
    private int currentIndex;
    private List<Character> answers; // index -> selected answer
    private Timeline timer;
    private AtomicInteger remainingSeconds;

    private final QuestionDao questionDao = new QuestionDao();
    private final StudentAnswerDao answerDao = new StudentAnswerDao();
    private final ExamAttemptDao attemptDao = new ExamAttemptDao();

    public void setExamAndAttempt(Exam ex, int attemptId) {
        this.exam = ex;
        this.attemptId = attemptId;
        this.questions = questionDao.findByExamId(exam.getExamId());
        this.answers = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) answers.add(null);
        this.currentIndex = 0;
        this.remainingSeconds = new AtomicInteger(exam.getDurationMinutes() * 60);

        // Timer
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            int sec = remainingSeconds.decrementAndGet();
            if (sec <= 0) {
                timer.stop();
                Platform.runLater(this::doSubmit);
                return;
            }
            int m = sec / 60, s = sec % 60;
            Platform.runLater(() -> {
                timerLabel.setText(String.format("Time: %02d:%02d", m, s));
                if (sec <= 300) timerLabel.setStyle("-fx-text-fill: #e74c3c;");
            });
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();

        buildPalette();
        showQuestion(0);
    }

    private void buildPalette() {
        paletteFlow.getChildren().clear();
        for (int i = 0; i < questions.size(); i++) {
            final int idx = i;
            Button btn = new Button(String.valueOf(i + 1));
            btn.setPrefSize(35, 35);
            btn.setStyle("-fx-background-color: #bdc3c7;");
            btn.setOnAction(e -> showQuestion(idx));
            paletteFlow.getChildren().add(btn);
        }
    }

    private void updatePaletteStyle() {
        for (int i = 0; i < paletteFlow.getChildren().size(); i++) {
            Button btn = (Button) paletteFlow.getChildren().get(i);
            if (i == currentIndex) {
                btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
            } else if (answers.get(i) != null) {
                btn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
            } else {
                btn.setStyle("-fx-background-color: #bdc3c7;");
            }
        }
    }

    private void showQuestion(int index) {
        currentIndex = index;
        if (index < 0 || index >= questions.size()) return;

        Question q = questions.get(index);
        questionNumberLabel.setText("Question " + (index + 1) + " of " + questions.size());
        questionTextLabel.setText(q.getQuestionText());

        optionsBox.getChildren().clear();
        ToggleGroup group = new ToggleGroup();
        Character selected = answers.get(index);
        for (char opt : new char[]{'A', 'B', 'C', 'D'}) {
            String text = opt == 'A' ? q.getOptionA() : opt == 'B' ? q.getOptionB() : opt == 'C' ? q.getOptionC() : q.getOptionD();
            RadioButton rb = new RadioButton(opt + ") " + text);
            rb.setToggleGroup(group);
            rb.setUserData(opt);
            if (selected != null && selected == opt) rb.setSelected(true);
            rb.selectedProperty().addListener((obs, old, val) -> {
                if (val) answers.set(index, opt);
            });
            optionsBox.getChildren().add(rb);
        }
        updatePaletteStyle();
    }

    private void saveCurrentAnswer() {
        for (var child : optionsBox.getChildren()) {
            if (child instanceof RadioButton rb && rb.isSelected()) {
                Character ans = (Character) rb.getUserData();
                answers.set(currentIndex, ans);
                Question q = questions.get(currentIndex);
                boolean correct = (q.getCorrectAnswer() == ans);
                int marks = correct ? q.getMarks() : 0;
                answerDao.saveAnswer(attemptId, q.getQuestionId(), ans, correct, marks);
                break;
            }
        }
    }

    @FXML
    private void previousQuestion() {
        saveCurrentAnswer();
        if (currentIndex > 0) showQuestion(currentIndex - 1);
    }

    @FXML
    private void nextQuestion() {
        saveCurrentAnswer();
        if (currentIndex < questions.size() - 1) {
            showQuestion(currentIndex + 1);
        }
    }

    @FXML
    private void submitExam() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Submit Exam");
        alert.setHeaderText("Are you sure you want to submit?");
        alert.setContentText("You cannot change answers after submission.");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            doSubmit();
        }
    }

    private void doSubmit() {
        if (timer != null) timer.stop();
        saveCurrentAnswer();

        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            Character ans = answers.get(i);
            Question q = questions.get(i);
            if (ans != null && q.getCorrectAnswer() == ans) {
                score += q.getMarks();
            }
        }
        attemptDao.submit(attemptId, score, LocalDateTime.now());

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Exam Submitted");
        info.setHeaderText("Your exam has been submitted.");
        info.setContentText("Score: " + score + " / " + exam.getTotalMarks());
        info.showAndWait();

        try {
            SceneUtil.loadFXML("/fxml/student/student-dashboard.fxml", "Student Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
