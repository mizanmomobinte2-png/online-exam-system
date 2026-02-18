package com.example.onlineexam.model;

public class Teacher {
    private int teacherId;
    private int userId;
    private String fullName;
    private String department;
    private String contact;

    public Teacher() {}

    public Teacher(int teacherId, int userId, String fullName, String department) {
        this.teacherId = teacherId;
        this.userId = userId;
        this.fullName = fullName;
        this.department = department;
    }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
}
