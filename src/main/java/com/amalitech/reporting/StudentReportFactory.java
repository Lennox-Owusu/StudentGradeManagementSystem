
package com.amalitech.reporting;

import com.amalitech.Grade;
import com.amalitech.GradeManager;
import com.amalitech.Student;

import java.time.LocalDateTime;
import java.util.*;

public final class StudentReportFactory {

    public StudentReport build(Student student, GradeManager gradeManager, ReportType type) {
        List<GradeDTO> rows = new ArrayList<>();
        int total = gradeManager.getGradeCount();

        for (int i = 0; i < total; i++) {
            Grade g = gradeManager.getGradeAt(i);
            if (g != null && g.getStudentId().equalsIgnoreCase(student.getStudentId())) {
                rows.add(new GradeDTO(
                        g.getGradeId(),
                        g.getDate(),
                        g.getSubject().getSubjectName(),
                        g.getSubject().getSubjectType(),
                        g.getGrade()
                ));
            }
        }

        // Aggregates (reuse your GradeManager methods)
        double coreAvg = gradeManager.calculateCoreAverage(student.getStudentId());
        double elecAvg = gradeManager.calculateElectiveAverage(student.getStudentId());
        double overallAvg = gradeManager.calculateOverallAverage(student.getStudentId());
        Aggregates aggs = new Aggregates(rows.size(), coreAvg, elecAvg, overallAvg);

        Map<String, String> meta = new LinkedHashMap<>();
        meta.put("generatedAt", LocalDateTime.now().toString());
        meta.put("generator", "StudentReportFactory v1");
        meta.put("reportType", type.name());

        return new StudentReport(
                student.getStudentId(),
                student.getName(),
                student.getEmail(),
                student.getPhone(),
                student.getStudentType(),
                type,
                rows,
                aggs,
                meta
        );
    }
}
