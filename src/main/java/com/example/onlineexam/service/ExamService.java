package com.example.onlineexam.service;

import com.example.onlineexam.dao.ExamDao;
import com.example.onlineexam.dao.QuestionDao;
import com.example.onlineexam.model.Exam;
import com.example.onlineexam.model.Question;

import java.util.List;

public class ExamService {

    private final ExamDao examDao;
    private final QuestionDao questionDao;

    public ExamService() {
        this.examDao = new ExamDao();
        this.questionDao = new QuestionDao();
    }

    public void validateExam(Exam exam, List<Question> questions) {
        if (exam == null) throw new IllegalArgumentException("Exam is null");
        if (exam.getTitle() == null || exam.getTitle().trim().isEmpty())
            throw new IllegalArgumentException("Title is required");
        if (exam.getDurationMinutes() <= 0)
            throw new IllegalArgumentException("Duration must be > 0");
        if (exam.getPassingMarks() < 0)
            throw new IllegalArgumentException("Pass marks must be >= 0");
        if (questions == null || questions.isEmpty())
            throw new IllegalArgumentException("Add at least one question");

        for (Question q : questions) {
            if (q.getQuestionText() == null || q.getQuestionText().trim().isEmpty())
                throw new IllegalArgumentException("Question text required");
            if (q.getOptionA() == null || q.getOptionA().trim().isEmpty()
                    || q.getOptionB() == null || q.getOptionB().trim().isEmpty()
                    || q.getOptionC() == null || q.getOptionC().trim().isEmpty()
                    || q.getOptionD() == null || q.getOptionD().trim().isEmpty())
                throw new IllegalArgumentException("All 4 options required");
            char c = Character.toUpperCase(q.getCorrectAnswer());
            if ("ABCD".indexOf(c) == -1)
                throw new IllegalArgumentException("Correct answer must be A/B/C/D");
            if (q.getMarks() <= 0)
                throw new IllegalArgumentException("Marks must be > 0");
        }

        int total = computeTotalMarks(questions);
        if (exam.getPassingMarks() > total)
            throw new IllegalArgumentException("Pass marks can't exceed total marks");
    }

    public int computeTotalMarks(List<Question> questions) {
        int sum = 0;
        for (Question q : questions) sum += q.getMarks();
        return sum;
    }


    public Exam getExamWithQuestions(int examId) {
        Exam e = examDao.findById(examId);
        if (e == null) return null;
        e.setQuestions(questionDao.findByExamId(examId));
        return e;
    }
}
