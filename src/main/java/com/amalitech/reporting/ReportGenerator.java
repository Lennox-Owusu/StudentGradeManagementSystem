
package com.amalitech.reporting;

import com.amalitech.base.Student;


//Generates simple text reports for a Student.
public class ReportGenerator {

    public String generateStudentReport(Student s) {
        if (s == null) return "Student Report\n(No data)\n";
        StringBuilder sb = new StringBuilder();
        sb.append("Student Report\n")
                .append("--------------\n")
                .append("ID: ").append(s.getStudentId()).append("\n")
                .append("Name: ").append(s.getName()).append("\n")
                .append("Type: ").append(s.getStudentType()).append("\n")
                .append("Email: ").append(s.getEmail()).append("\n")
                .append("Enrolled Subjects: ").append(s.getEnrolledSubjectCount()).append("\n")
                .append("Average Grade: ").append(String.format("%.2f", s.calculateAverageGrade())).append("\n")
                .append("Passing: ").append(s.isPassing() ? "Yes" : "No").append("\n");
        return sb.toString();
    }
}
