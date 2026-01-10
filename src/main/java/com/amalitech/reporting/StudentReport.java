
package com.amalitech.reporting;

import com.amalitech.base.Grade;
import com.amalitech.base.Student;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Serializable DTO used by all exporters (CSV/JSON/Binary). */
public final class StudentReport implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String studentId;
    private final String name;
    private final String email;
    private final String phone;
    private final String type;
    private final int totalGrades;
    private final List<Grade> grades = new ArrayList<>();
    private final double coreAverage;
    private final double electiveAverage;
    private final double overallAverage;

    public StudentReport(Student student,
                         List<Grade> gradeList,
                         double coreAvg,
                         double elecAvg,
                         double overallAvg,
                         String phoneOverride) {
        this.studentId = student.getStudentId();
        this.name = student.getName();
        this.email = student.getEmail();
        this.phone = phoneOverride;
        this.type = student.getStudentType();
        this.totalGrades = gradeList.size();
        this.grades.addAll(gradeList);
        this.coreAverage = coreAvg;
        this.electiveAverage = elecAvg;
        this.overallAverage = overallAvg;
    }

    // Getters
    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getType() { return type; }
    public int getTotalGrades() { return totalGrades; }
    public List<Grade> getGrades() { return new ArrayList<>(grades); }
    public double getCoreAverage() { return coreAverage; }
    public double getElectiveAverage() { return electiveAverage; }
    public double getOverallAverage() { return overallAverage; }
}
