
package com.amalitech.service;

import com.amalitech.model.Grade;
import com.amalitech.manager.GradeManager;
import com.amalitech.model.Student;
import com.amalitech.reporting.ReportGenerator;

import java.util.List;

public class ReportService implements com.amalitech.service.api.IReportService {
    private final ReportGenerator reportGenerator;
    private final GradeManager gradeManager;

    public ReportService(ReportGenerator reportGenerator, GradeManager gradeManager) {
        this.reportGenerator = reportGenerator;
        this.gradeManager = gradeManager;
    }

    /** Export report builder (1=summary, 2=detailed, 3=both). */
    public String buildReport(Student student, int choice) {
        StringBuilder report = new StringBuilder();
        report.append("╔").append("═".repeat(50)).append("╗\n");
        report.append("║ EXPORT GRADE REPORT ║\n");
        report.append("╚").append("═".repeat(50)).append("╝\n\n");

        if (choice == 1 || choice == 3) {
            report.append(reportGenerator.generateStudentReport(student)).append("\n");
        }
        if (choice == 2 || choice == 3) {
            report.append("\nDETAILED GRADES:\n");
            report.append(String.format("%-8s │ %-10s │ %-20s │ %-8s │ %-7s%n",
                    "GRD ID", "DATE", "SUBJECT", "TYPE", "GRADE"));
            report.append("─".repeat(70)).append("\n");
            for (int i = 0; i < gradeManager.getGradeCount(); i++) {
                Grade g = gradeManager.getGradeAt(i);
                if (g != null && g.getStudentId().equalsIgnoreCase(student.getStudentId())) {
                    report.append(String.format("%-8s │ %-10s │ %-20s │ %-8s │ %-7.2f%n",
                            g.getGradeId(), g.getDate(), g.getSubject().getSubjectName(),
                            g.getSubject().getSubjectType(), g.getGrade()));
                }
            }
            report.append("─".repeat(70)).append("\n");
        }
        return report.toString();
    }

    /** View report builder for console (summary + history if any grades). */
    public String buildViewReport(Student student) {
        StringBuilder view = new StringBuilder();
        view.append("\nVIEW GRADE REPORT\n").append("─".repeat(50)).append("\n");
        view.append(reportGenerator.generateStudentReport(student)).append("\n");
        boolean hasGrades = false; int total = 0;
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            Grade g = gradeManager.getGradeAt(i);
            if (g != null && g.getStudentId().equalsIgnoreCase(student.getStudentId())) {
                hasGrades = true; total++;
            }
        }
        if (!hasGrades) {
            view.append("No grades recorded for this student\n").append("─".repeat(50)).append("\n");
            return view.toString();
        }
        view.append("\nGRADE HISTORY\n");
        view.append(String.format("%-8s │ %-10s │ %-20s │ %-8s │ %-7s%n",
                "GRD ID", "DATE", "SUBJECT", "TYPE", "GRADE"));
        view.append("─".repeat(70)).append("\n");
        for (int i = gradeManager.getGradeCount() - 1; i >= 0; i--) {
            Grade g = gradeManager.getGradeAt(i);
            if (g != null && g.getStudentId().equalsIgnoreCase(student.getStudentId())) {
                view.append(String.format("%-8s │ %-10s │ %-20s │ %-8s │ %-7s%n",
                        g.getGradeId(), g.getDate(), g.getSubject().getSubjectName(),
                        g.getSubject().getSubjectType(), g.getGrade()));
            }
        }
        view.append("─".repeat(70)).append("\n");
        view.append("Total Grades: ").append(total).append("\n");
        view.append(String.format("Core Subjects Average: %.2f%n", gradeManager.calculateCoreAverage(student.getStudentId())));
        view.append(String.format("Elective Subjects Average: %.2f%n", gradeManager.calculateElectiveAverage(student.getStudentId())));
        view.append(String.format("Overall Average: %.2f%n", gradeManager.calculateOverallAverage(student.getStudentId())));
        view.append("\nPerformance Summary:\n");
        view.append("Passing all core subjects\n");
        view.append(String.format("Meeting passing grade requirement (%.0f%%)%n", student.getPassingGrade()));
        return view.toString();
    }

    /** Search results report builder (table string for export). */
    public String buildSearchResultsReport(List<Student> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("SEARCH RESULTS (").append(results.size()).append(" found)").append("\n");
        sb.append("─".repeat(50)).append("\n");
        sb.append(String.format("%-8s │ %-18s │ %-8s │ %-5s%n", "STU ID", "NAME", "TYPE", "AVG"));
        sb.append("─".repeat(50)).append("\n");
        if (results.isEmpty()) {
            sb.append("(no matches)\n").append("─".repeat(50)).append("\n");
            return sb.toString();
        }
        for (Student s : results) {
            String name = s.getName().length() > 18 ? s.getName().substring(0, 15) + "..." : s.getName();
            sb.append(String.format("%-8s │ %-18s │ %-8s │ %5.1f%%%n",
                    s.getStudentId(), name, s.getStudentType(), s.calculateAverageGrade()));
        }
        sb.append("─".repeat(50)).append("\n");
        return sb.toString();
    }
}
