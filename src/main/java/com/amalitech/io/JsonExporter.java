
package com.amalitech.io;

import com.amalitech.reporting.StudentReport;
import com.amalitech.reporting.GradeDTO;
import com.amalitech.reporting.Aggregates;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;

public final class JsonExporter implements Exporter<StudentReport> {

    @Override
    public ExportResult exportTo(StudentReport report, Path baseDir) throws ImportExportException {
        try {
            Path dir = NioFileUtils.ensureDir(baseDir.resolve("json"));
            String name = NioFileUtils.safeFileName(report.getName()) + "_" + report.getReportType().name().toLowerCase() + ".json";
            Path file = dir.resolve(name);

            long start = System.nanoTime();
            try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                StringBuilder sb = new StringBuilder(4096);
                sb.append("{\n");
                sb.append("  \"student\": {\n");
                sb.append("    \"id\": \"").append(escape(report.getStudentId())).append("\",\n");
                sb.append("    \"name\": \"").append(escape(report.getName())).append("\",\n");
                sb.append("    \"email\": \"").append(escape(report.getEmail())).append("\",\n");
                sb.append("    \"phone\": \"").append(escape(report.getPhone())).append("\",\n");
                sb.append("    \"type\": \"").append(escape(report.getStudentType())).append("\"\n");
                sb.append("  },\n");

                sb.append("  \"grades\": [\n");
                for (int i = 0; i < report.getGrades().size(); i++) {
                    GradeDTO g = report.getGrades().get(i);
                    sb.append("    {")
                            .append("\"gradeId\":\"").append(escape(g.getGradeId())).append("\",")
                            .append("\"date\":\"").append(escape(g.getDate())).append("\",")
                            .append("\"subjectName\":\"").append(escape(g.getSubjectName())).append("\",")
                            .append("\"subjectType\":\"").append(escape(g.getSubjectType())).append("\",")
                            .append("\"grade\":").append(g.getGrade())
                            .append("}");
                    if (i < report.getGrades().size() - 1) sb.append(",");
                    sb.append("\n");
                }
                sb.append("  ],\n");

                Aggregates a = report.getAggregates();
                sb.append("  \"aggregates\": {\n");
                sb.append("    \"totalGrades\": ").append(a.getTotalGrades()).append(",\n");
                sb.append("    \"coreAverage\": ").append(String.format("%.2f", a.getCoreAverage())).append(",\n");
                sb.append("    \"electiveAverage\": ").append(String.format("%.2f", a.getElectiveAverage())).append(",\n");
                sb.append("    \"overallAverage\": ").append(String.format("%.2f", a.getOverallAverage())).append("\n");
                sb.append("  },\n");

                sb.append("  \"metadata\": {\n");
                int idx = 0;
                for (Map.Entry<String, String> e : report.getMetadata().entrySet()) {
                    sb.append("    \"").append(escape(e.getKey())).append("\": \"").append(escape(e.getValue())).append("\"");
                    if (idx++ < report.getMetadata().size() - 1) sb.append(",");
                    sb.append("\n");
                }
                sb.append("  }\n");
                sb.append("}\n");

                bw.write(sb.toString());
            }
            long millis = (System.nanoTime() - start) / 1_000_000;
            long bytes = NioFileUtils.fileSize(file);
            return new ExportResult(bytes, millis, file.getFileName().toString(), "./reports/json/");
        } catch (Exception e) {
            throw new ImportExportException("JSON export failed", e);
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
