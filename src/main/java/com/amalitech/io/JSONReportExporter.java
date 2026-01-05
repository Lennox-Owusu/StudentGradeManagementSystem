
package com.amalitech.io;

import com.amalitech.reporting.StudentReport;
import com.amalitech.exceptions.ExportFailedException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public final class JSONReportExporter {

    public Path exportDetailed(StudentReport report, Path dir, String baseName) throws ExportFailedException {
        try {
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Path target = dir.resolve(baseName + "_detailed.json");

            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"meta\": {\n");
            sb.append("    \"format\": \"student-report\",\n");
            sb.append("    \"version\": \"1.0\"\n");
            sb.append("  },\n");
            sb.append("  \"student\": {\n");
            sb.append("    \"id\": \"").append(esc(report.getStudentId())).append("\",\n");
            sb.append("    \"name\": \"").append(esc(report.getName())).append("\",\n");
            sb.append("    \"email\": \"").append(esc(report.getEmail())).append("\",\n");
            sb.append("    \"phone\": \"").append(esc(report.getPhone())).append("\",\n");
            sb.append("    \"type\": \"").append(esc(report.getType())).append("\"\n");
            sb.append("  },\n");
            sb.append("  \"aggregates\": {\n");
            sb.append("    \"coreAverage\": ").append(String.format("%.2f", report.getCoreAverage())).append(",\n");
            sb.append("    \"electiveAverage\": ").append(String.format("%.2f", report.getElectiveAverage())).append(",\n");
            sb.append("    \"overallAverage\": ").append(String.format("%.2f", report.getOverallAverage())).append("\n");
            sb.append("  },\n");
            sb.append("  \"grades\": [\n");
            for (int i = 0; i < report.getGrades().size(); i++) {
                var g = report.getGrades().get(i);
                sb.append("    {\n");
                sb.append("      \"id\": \"").append(esc(g.getGradeId())).append("\",\n");
                sb.append("      \"date\": \"").append(esc(g.getDate())).append("\",\n");
                sb.append("      \"subject\": {\n");
                sb.append("        \"name\": \"").append(esc(g.getSubject().getSubjectName())).append("\",\n");
                sb.append("        \"type\": \"").append(esc(g.getSubject().getSubjectType())).append("\"\n");
                sb.append("      },\n");
                sb.append("      \"grade\": ").append(String.format("%.2f", g.getGrade())).append("\n");
                sb.append("    }").append(i == report.getGrades().size() - 1 ? "\n" : ",\n");
            }
            sb.append("  ]\n");
            sb.append("}\n");

            Files.writeString(target, sb.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            return target;
        } catch (IOException ioe) {
            throw new ExportFailedException(dir.toAbsolutePath().toString(), ioe);
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
