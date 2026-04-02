package com.example.onlineexam.service;

import com.example.onlineexam.model.Question;
import java.util.*;

public class ExamEngine {

    private List<Question> questions;
    private int currentIndex = 0;
    private Map<Integer,String> answers = new HashMap<>();

    public ExamEngine(List<Question> questions){
        Collections.shuffle(questions);
        this.questions = questions;
    }

    public Question getCurrentQuestion(){
        return questions.get(currentIndex);
    }

    public int getCurrentIndex(){
        return currentIndex;
    }

    public void next(){
        if(currentIndex < questions.size()-1){
            currentIndex++;
        }
    }

    public void previous(){
        if(currentIndex > 0){
            currentIndex--;
        }
    }

    public void saveAnswer(int questionId,String option){
        answers.put(questionId,option);
    }

    public Map<Integer,String> getAnswers(){
        return answers;
    }

}
