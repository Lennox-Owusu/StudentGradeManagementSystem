
package com.amalitech.io;

import com.amalitech.reporting.StudentReport;
import com.amalitech.reporting.GradeDTO;
import com.amalitech.reporting.ReportType;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public final class CsvExporter implements Exporter<StudentReport> {

    @Override
    public ExportResult exportTo(StudentReport report, Path baseDir) throws ImportExportException {
        try {
            Path dir = NioFileUtils.ensureDir(baseDir.resolve("csv"));
            String name = NioFileUtils.safeFileName(report.getName()) + "_" + report.getReportType().name().toLowerCase() + ".csv";
            Path file = dir.resolve(name);

            long start = System.nanoTime();
            try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                // header
                bw.write("gradeId,date,subjectName,subjectType,grade");
                bw.newLine();
                for (GradeDTO g : report.getGrades()) {
                    bw.write(g.getGradeId() + "," + g.getDate() + "," + escapeCsv(g.getSubjectName()) + "," + g.getSubjectType() + "," + g.getGrade());
                    bw.newLine();
                }
            }
            long millis = (System.nanoTime() - start) / 1_000_000;
            long bytes = NioFileUtils.fileSize(file);
            return new ExportResult(bytes, millis, file.getFileName().toString(), "./reports/csv/");
        } catch (Exception e) {
            throw new ImportExportException("CSV export failed", e);
        }
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        String v = s.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"")) return "\"" + v + "\"";
        return v;
    }
}
