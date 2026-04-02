package com.example.onlineexam.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ExamScreenController implements Initializable {

    @FXML private Label timerLabel;
    @FXML private Label questionCountLabel;
    @FXML private Label questionLabel;
    @FXML private ProgressBar progressBar;

    @FXML private RadioButton optionA;
    @FXML private RadioButton optionB;
    @FXML private RadioButton optionC;
    @FXML private RadioButton optionD;

    @FXML private ToggleGroup optionGroup;

    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Button submitButton;

    private int currentIndex = 0;

    // temporary demo questions
    private final String[] questions = {
            "What is the capital of Bangladesh?",
            "Which data structure uses FIFO order?",
            "Which keyword is used to inherit a class in Java?"
    };

    private final String[][] options = {
            {"Dhaka", "Chittagong", "Khulna", "Rajshahi"},
            {"Stack", "Queue", "Tree", "Graph"},
            {"this", "super", "extends", "implements"}
    };

    private final String[] correctAnswers = {"A", "B", "C"};

    // questionIndex -> selected option (A/B/C/D)
    private final Map<Integer, String> selectedAnswers = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadQuestion();
    }

    private void loadQuestion() {
        questionCountLabel.setText("Question " + (currentIndex + 1) + " of " + questions.length);
        questionLabel.setText(questions[currentIndex]);

        optionA.setText("A. " + options[currentIndex][0]);
        optionB.setText("B. " + options[currentIndex][1]);
        optionC.setText("C. " + options[currentIndex][2]);
        optionD.setText("D. " + options[currentIndex][3]);

        optionGroup.selectToggle(null);

        String savedAnswer = selectedAnswers.get(currentIndex);
        if (savedAnswer != null) {
            switch (savedAnswer) {
                case "A" -> optionGroup.selectToggle(optionA);
                case "B" -> optionGroup.selectToggle(optionB);
                case "C" -> optionGroup.selectToggle(optionC);
                case "D" -> optionGroup.selectToggle(optionD);
            }
        }

        progressBar.setProgress((double) (currentIndex + 1) / questions.length);

        previousButton.setDisable(currentIndex == 0);
        nextButton.setDisable(currentIndex == questions.length - 1);
    }

    private void saveCurrentAnswer() {
        RadioButton selected = (RadioButton) optionGroup.getSelectedToggle();
        if (selected == null) {
            selectedAnswers.remove(currentIndex);
            return;
        }

        if (selected == optionA) {
            selectedAnswers.put(currentIndex, "A");
        } else if (selected == optionB) {
            selectedAnswers.put(currentIndex, "B");
        } else if (selected == optionC) {
            selectedAnswers.put(currentIndex, "C");
        } else if (selected == optionD) {
            selectedAnswers.put(currentIndex, "D");
        }
    }

    @FXML
    private void handleNext() {
        saveCurrentAnswer();

        if (currentIndex < questions.length - 1) {
            currentIndex++;
            loadQuestion();
        }
    }

    @FXML
    private void handlePrevious() {
        saveCurrentAnswer();

        if (currentIndex > 0) {
            currentIndex--;
            loadQuestion();
        }
    }

    @FXML
    private void handleSubmit() {
        saveCurrentAnswer();

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Submit Exam");
        confirmAlert.setHeaderText("Are you sure you want to submit?");
        confirmAlert.setContentText("Once submitted, you cannot change your answers.");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(yesButton, noButton);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                int score = calculateScore();

                Alert resultAlert = new Alert(Alert.AlertType.INFORMATION);
                resultAlert.setTitle("Exam Submitted");
                resultAlert.setHeaderText("Submission Successful");
                resultAlert.setContentText("Your score is " + score + " out of " + questions.length);
                resultAlert.showAndWait();
            }
        });
    }

    private int calculateScore() {
        int score = 0;

        for (int i = 0; i < questions.length; i++) {
            String selected = selectedAnswers.get(i);
            if (selected != null && selected.equals(correctAnswers[i])) {
                score++;
            }
        }

        return score;
    }
}