package com.amalitech;



import java.time.LocalDate;

public class Grade {
    private static int gradeCounter = 0;
    private final String gradeId;
    private final String studentId;
    private final Subject subject;
    private final double grade;
    private final String date;

    public Grade(String studentId, Subject subject, double grade) {

        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("studentId must not be null or blank");
        }
        if (subject == null) {
            throw new IllegalArgumentException("subject must not be null");
        }
        if (Double.isNaN(grade) || grade < 0.0 || grade > 100.0) {
            throw new IllegalArgumentException("grade must be between 0 and 100");
        }

        //State initialization
        this.gradeId = "GRD" + (++gradeCounter);
        this.studentId = studentId;
        this.subject = subject;
        this.grade = grade;
        this.date = LocalDate.now().toString();
    }

    //Getters
    public String getGradeId() {
        return gradeId; }
    public String getStudentId() {
        return studentId; }
    public Subject getSubject() {
        return subject; }
    public double getGrade() {
        return grade; }
    public String getDate() {
        return date; }
}


