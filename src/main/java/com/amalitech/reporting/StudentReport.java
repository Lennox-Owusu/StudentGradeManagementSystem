
package com.amalitech.reporting;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public final class StudentReport implements Serializable {
    private final String studentId;
    private final String name;
    private final String email;
    private final String phone;
    private final String studentType;
    private final ReportType reportType;

    private final List<GradeDTO> grades;
    private final Aggregates aggregates;
    private final Map<String, String> metadata; // e.g., generatedAt, generatorVersion

    public StudentReport(String studentId, String name, String email, String phone,
                         String studentType, ReportType reportType,
                         List<GradeDTO> grades, Aggregates aggregates,
                         Map<String, String> metadata) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.studentType = studentType;
        this.reportType = reportType;
        this.grades = grades;
        this.aggregates = aggregates;
        this.metadata = metadata;
    }

    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getStudentType() { return studentType; }
    public ReportType getReportType() { return reportType; }
    public List<GradeDTO> getGrades() { return grades; }
    public Aggregates getAggregates() { return aggregates; }
    public Map<String, String> getMetadata() { return metadata; }
}
