package com.example.onlineexam.model;

public class Student {
    private int studentId;
    private int userId;
    private String fullName;
    private String enrollmentNo;
    private String contact;

    public Student() {}

    public Student(int studentId, int userId, String fullName, String enrollmentNo) {
        this.studentId = studentId;
        this.userId = userId;
        this.fullName = fullName;
        this.enrollmentNo = enrollmentNo;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEnrollmentNo() { return enrollmentNo; }
    public void setEnrollmentNo(String enrollmentNo) { this.enrollmentNo = enrollmentNo; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
}
