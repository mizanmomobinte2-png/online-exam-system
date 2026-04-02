package com.example.onlineexam.controller;

import com.example.onlineexam.dao.ExamAttemptDao;
import com.example.onlineexam.dao.QuestionDao;
import com.example.onlineexam.dao.StudentAnswerDao;
import com.example.onlineexam.model.Exam;
import com.example.onlineexam.model.ExamAttempt;
import com.example.onlineexam.model.Question;
import com.example.onlineexam.service.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
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
    private int attemptId = -1;
    private List<Question> questions = new ArrayList<>();
    private int currentIndex = 0;
    private List<Character> answers = new ArrayList<>();
    private Timeline timer;
    private AtomicInteger remainingSeconds;

    private final QuestionDao questionDao = new QuestionDao();
    private final StudentAnswerDao answerDao = new StudentAnswerDao();
    private final ExamAttemptDao attemptDao = new ExamAttemptDao();

    public void setExam(Exam ex) {
        this.exam = ex;

        if (exam == null) {
            showError("Exam data not found.");
            return;
        }

        this.questions = questionDao.findByExamId(exam.getExamId());

        System.out.println("Exam ID = " + exam.getExamId());
        System.out.println("Questions loaded = " + questions.size());

        if (questions == null || questions.isEmpty()) {
            showError("This exam has no questions yet.");
            return;
        }

        createAttempt();

        if (attemptId <= 0) {
            showError("Could not start exam attempt.");
            return;
        }

        this.answers = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            answers.add(null);
        }

        this.currentIndex = 0;
        this.remainingSeconds = new AtomicInteger(exam.getDurationMinutes() * 60);

        startTimer();
        buildPalette();
        showQuestion(0);
    }

    private void createAttempt() {
        try {
            ExamAttempt attempt = new ExamAttempt();
            attempt.setExamId(exam.getExamId());
            attempt.setStudentId(SessionManager.getCurrentStudent().getStudentId());
            attempt.setStartTime(LocalDateTime.now());
            attempt.setTotalMarks(exam.getTotalMarks());
            attempt.setScore(0);
            attempt.setStatus(ExamAttempt.AttemptStatus.IN_PROGRESS);

            attemptId = attemptDao.create(attempt);
            System.out.println("Created attemptId = " + attemptId);

        } catch (Exception e) {
            e.printStackTrace();
            attemptId = -1;
        }
    }

    private void startTimer() {
        int totalSeconds = remainingSeconds.get();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            int sec = remainingSeconds.decrementAndGet();

            if (sec <= 0) {
                timer.stop();
                Platform.runLater(this::doSubmit);
                return;
            }

            int m = sec / 60;
            int s = sec % 60;

            Platform.runLater(() -> {
                timerLabel.setText(String.format("Time: %02d:%02d", m, s));
                if (sec <= 300) {
                    timerLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                }
            });
        }));

        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void buildPalette() {
        paletteFlow.getChildren().clear();

        for (int i = 0; i < questions.size(); i++) {
            final int idx = i;

            Button btn = new Button(String.valueOf(i + 1));
            btn.setPrefSize(35, 35);
            btn.setStyle("-fx-background-color: #bdc3c7;");

            btn.setOnAction(e -> {
                saveCurrentAnswer();
                showQuestion(idx);
            });

            paletteFlow.getChildren().add(btn);
        }

        updatePaletteStyle();
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
        if (index < 0 || index >= questions.size()) {
            return;
        }

        currentIndex = index;
        Question q = questions.get(index);

        questionNumberLabel.setText("Question " + (index + 1) + " of " + questions.size());
        questionTextLabel.setText(q.getQuestionText());

        optionsBox.getChildren().clear();

        ToggleGroup group = new ToggleGroup();
        Character selected = answers.get(index);

        addOption(group, 'A', q.getOptionA(), selected, index);
        addOption(group, 'B', q.getOptionB(), selected, index);
        addOption(group, 'C', q.getOptionC(), selected, index);
        addOption(group, 'D', q.getOptionD(), selected, index);

        updatePaletteStyle();
    }

    private void addOption(ToggleGroup group, char optionLetter, String optionText, Character selected, int answerIndex) {
        RadioButton rb = new RadioButton(optionLetter + ") " + optionText);
        rb.setToggleGroup(group);
        rb.setUserData(optionLetter);

        if (selected != null && selected == optionLetter) {
            rb.setSelected(true);
        }

        rb.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                answers.set(answerIndex, optionLetter);
                updatePaletteStyle();
            }
        });

        optionsBox.getChildren().add(rb);
    }

    private void saveCurrentAnswer() {
        if (questions == null || questions.isEmpty()) {
            return;
        }

        if (attemptId <= 0) {
            return;
        }

        Character selectedAnswer = answers.get(currentIndex);
        if (selectedAnswer == null) {
            return;
        }

        Question q = questions.get(currentIndex);
        boolean correct = (q.getCorrectAnswer() == selectedAnswer);
        int marks = correct ? q.getMarks() : 0;

        try {
            answerDao.saveAnswer(attemptId, q.getQuestionId(), selectedAnswer, correct, marks);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void previousQuestion() {
        saveCurrentAnswer();
        if (currentIndex > 0) {
            showQuestion(currentIndex - 1);
        }
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
        if (timer != null) {
            timer.stop();
        }

        saveCurrentAnswer();

        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            Character ans = answers.get(i);
            Question q = questions.get(i);

            if (ans != null && q.getCorrectAnswer() == ans) {
                score += q.getMarks();
            }
        }

        try {
            attemptDao.submit(attemptId, score, LocalDateTime.now());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student/result-screen.fxml"));
            Parent root = loader.load();

            ResultScreenController controller = loader.getController();
            controller.setAttemptId(attemptId);

            Stage stage = (Stage) timerLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Result");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Exam Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}