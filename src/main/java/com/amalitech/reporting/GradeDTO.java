
package com.amalitech.reporting;

import java.io.Serializable;

public final class GradeDTO implements Serializable {
    private final String gradeId;
    private final String date;           // keep your existing date string
    private final String subjectName;
    private final String subjectType;    // "Core" / "Elective"
    private final double grade;          // 0–100

    public GradeDTO(String gradeId, String date, String subjectName, String subjectType, double grade) {
        this.gradeId = gradeId;
        this.date = date;
        this.subjectName = subjectName;
        this.subjectType = subjectType;
        this.grade = grade;
    }
    public String getGradeId() { return gradeId; }
    public String getDate() { return date; }
    public String getSubjectName() { return subjectName; }
    public String getSubjectType() { return subjectType; }
    public double getGrade() { return grade; }
}
