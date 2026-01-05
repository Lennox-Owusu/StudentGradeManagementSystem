
package com.amalitech.io;

import com.amalitech.reporting.StudentReport;
import com.amalitech.exceptions.ExportFailedException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public final class CSVReportExporter {

    public Path exportDetailed(StudentReport report, Path dir, String baseName) throws ExportFailedException {
        try {
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Path target = dir.resolve(baseName + "_detailed.csv");

            // Header + rows (12 grades + header as in screenshot)
            StringBuilder sb = new StringBuilder();
            sb.append("StudentID,Name,Email,Phone,Type,Subject,Type,Grade,Date\n");
            for (var g : report.getGrades()) {
                sb.append(report.getStudentId()).append(',')
                        .append(escape(report.getName())).append(',')
                        .append(escape(report.getEmail())).append(',')
                        .append(escape(report.getPhone())).append(',')
                        .append(report.getType()).append(',')
                        .append(escape(g.getSubject().getSubjectName())).append(',')
                        .append(g.getSubject().getSubjectType()).append(',')
                        .append(g.getGrade()).append(',')
                        .append(g.getDate()).append('\n');
            }
            Files.writeString(target, sb.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            return target;
        } catch (IOException ioe) {
            throw new ExportFailedException(dir.toAbsolutePath().toString(), ioe);
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        String t = s.replace("\"", "\"\"");
        if (t.contains(",") || t.contains("\n")) return "\"" + t + "\"";
        return t;
    }
}
