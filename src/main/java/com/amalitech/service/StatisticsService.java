
package com.amalitech.service;

import com.amalitech.model.Grade;
import com.amalitech.manager.GradeManager;
import com.amalitech.model.Student;
import com.amalitech.manager.StudentManager;
import com.amalitech.calculation.StatisticsCalculator;

import java.util.*;

public class StatisticsService implements com.amalitech.service.api.IStatisticsService {
    private final StudentManager studentManager;
    private final GradeManager gradeManager;
    private final StatisticsCalculator stats;

    public StatisticsService(StudentManager studentManager, GradeManager gradeManager, StatisticsCalculator stats) {
        this.studentManager = studentManager;
        this.gradeManager = gradeManager;
        this.stats = stats;
    }

    /** Builds a formatted "Class Statistics" report string for console viewing. */
    public String buildClassStatisticsReport() {
        List<Grade> allGrades = new ArrayList<>();
        for (int i = 0; i < gradeManager.getGradeCount(); i++) {
            Grade g = gradeManager.getGradeAt(i);
            if (g != null) allGrades.add(g);
        }
        int totalStudents = studentManager.getStudentCount();
        int totalGrades = allGrades.size();

        StringBuilder out = new StringBuilder();
        out.append("\nCLASS STATISTICS\n").append("─".repeat(60)).append("\n");
        out.append("\nTotal Students: ").append(totalStudents).append("\n");
        out.append("Total Grades Recorded: ").append(totalGrades).append("\n");
        if (totalGrades == 0) {
            out.append("\nNo grades in the system yet.\n");
            return out.toString();
        }

        // Numeric values
        List<Double> values = new ArrayList<>(totalGrades);
        for (Grade g : allGrades) values.add(g.getGrade());

        // Grade distribution bands
        int[] bands = stats.gradeBandsCounts(values);

        // Min/Max with owning Grade references
        double min = 101, max = -1;
        Grade minG = null, maxG = null;
        for (Grade g : allGrades) {
            double v = g.getGrade();
            if (v > max) { max = v; maxG = g; }
            if (v < min) { min = v; minG = g; }
        }

        out.append("\nGRADE DISTRIBUTION\n").append("─".repeat(60)).append("\n");
        printBand(out, "90–100% (A):", bands[0], totalGrades);
        printBand(out, "80–89% (B):", bands[1], totalGrades);
        printBand(out, "70–79% (C):", bands[2], totalGrades);
        printBand(out, "60–69% (D):", bands[3], totalGrades);
        printBand(out, "0–59% (F):", bands[4], totalGrades);

        // Statistical analysis
        double mean = stats.mean(values);
        double median = stats.median(values);
        double modeVal = stats.modeRounded(values);
        double std = stats.stdDevPopulation(values);
        out.append("\nSTATISTICAL ANALYSIS\n").append("─".repeat(60)).append("\n");
        out.append(String.format("Mean (Average): %.1f%%%n", mean));
        out.append(String.format("Median: %.1f%%%n", median));
        out.append(String.format("Mode: %.1f%%%n", modeVal));
        out.append(String.format("Standard Deviation: %.1f%%%n", std));
        out.append(String.format("Range: %.1f%% (%.0f%% – %.0f%%)%n", (max - min), min, max));

        // Highest/Lowest grade records
        if (maxG != null) {
            out.append(String.format("%nHighest Grade: %.0f%% (%s - %s)%n",
                    maxG.getGrade(), maxG.getStudentId(), maxG.getSubject().getSubjectName()));
        }
        if (minG != null) {
            out.append(String.format("Lowest Grade: %.0f%% (%s - %s)%n",
                    minG.getGrade(), minG.getStudentId(), minG.getSubject().getSubjectName()));
        }

        // Subject performance
        double coreSum = 0, coreCnt = 0, elecSum = 0, elecCnt = 0;
        Map<String, double[]> subjectAgg = new HashMap<>(); // name -> [sum, count]
        for (Grade g : allGrades) {
            String type = g.getSubject().getSubjectType();
            if ("Core".equalsIgnoreCase(type)) { coreSum += g.getGrade(); coreCnt++; }
            else if ("Elective".equalsIgnoreCase(type)) { elecSum += g.getGrade(); elecCnt++; }
            String name = g.getSubject().getSubjectName();
            double[] sc = subjectAgg.getOrDefault(name, new double[]{0.0, 0.0});
            sc[0] += g.getGrade(); sc[1] += 1.0;
            subjectAgg.put(name, sc);
        }
        double coreAvg = coreCnt == 0 ? 0.0 : coreSum / coreCnt;
        double elecAvg = elecCnt == 0 ? 0.0 : elecSum / elecCnt;
        out.append("\nSUBJECT PERFORMANCE\n").append("─".repeat(60)).append("\n");
        out.append(String.format("Core Subjects: %.1f%% average%n", coreAvg));
        printSubjectAvg(out, subjectAgg, "Mathematics");
        printSubjectAvg(out, subjectAgg, "English");
        printSubjectAvg(out, subjectAgg, "Science");
        out.append("\n");
        out.append(String.format("Elective Subjects: %.1f%% average%n", elecAvg));
        printSubjectAvg(out, subjectAgg, "Music");
        printSubjectAvg(out, subjectAgg, "Art");
        printSubjectAvg(out, subjectAgg, "Physical Education");

        // Student type comparison
        Map<String, Integer> gradesPerStudent = new HashMap<>();
        for (Grade g : allGrades) {
            gradesPerStudent.put(g.getStudentId(), gradesPerStudent.getOrDefault(g.getStudentId(), 0) + 1);
        }
        double regSumAvg = 0.0; int regCount = 0;
        double honSumAvg = 0.0; int honCount = 0;
        for (Student s : studentManager.getStudents()) {
            Integer scnt = gradesPerStudent.get(s.getStudentId());
            if (scnt == null || scnt == 0) continue; // skip students with no grades
            double avg = s.calculateAverageGrade();
            String type = s.getStudentType();
            if ("Regular".equalsIgnoreCase(type) || "Regular Student".equalsIgnoreCase(type)) {
                regSumAvg += avg; regCount++;
            } else if ("Honors".equalsIgnoreCase(type) || "Honors Student".equalsIgnoreCase(type)) {
                honSumAvg += avg; honCount++;
            }
        }
        out.append("\nSTUDENT TYPE COMPARISON\n").append("─".repeat(60)).append("\n");
        out.append(String.format("Regular Students: %.1f%% average (%d students)%n",
                regCount == 0 ? 0.0 : regSumAvg / regCount, regCount));
        out.append(String.format("Honors Students: %.1f%% average (%d students)%n",
                honCount == 0 ? 0.0 : honSumAvg / honCount, honCount));

        return out.toString();
    }

    // Helpers
    private static void printBand(StringBuilder out, String label, int count, int total) {
        double pct = total == 0 ? 0.0 : (count * 100.0 / total);
        String bar = buildBar(count, total);
        out.append(String.format("%-13s %s %4.1f%% (%d grades)%n", label, bar, pct, count));
    }

    private static String buildBar(int count, int total) {
        int filled = (total == 0) ? 0 : (int) Math.round((count * 1.0 / total) * 28);
        filled = Math.max(0, Math.min(filled, 28));
        String filledPart = "█".repeat(filled);
        String emptyPart = "░".repeat(28 - filled);
        return filledPart + emptyPart;
    }

    private static void printSubjectAvg(StringBuilder out, Map<String, double[]> agg, String subject) {
        double[] sc = agg.get(subject);
        if (sc != null && sc[1] > 0) {
            double avg = sc[0] / sc[1];
            out.append(String.format("%s: %6.1f%%%n", subject, avg));
        } else {
            out.append(String.format("%s: %6.1f%%%n", subject, 0.0));
        }
    }
}
