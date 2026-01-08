
package com.amalitech.service;

import com.amalitech.manager.StudentManager;
import com.amalitech.manager.GradeManager;
import com.amalitech.model.Student;
import com.amalitech.model.Subject;
import com.amalitech.model.CoreSubject;
import com.amalitech.model.ElectiveSubject;
import com.amalitech.model.Grade;
import com.amalitech.io.CSVParser;
import com.amalitech.util.AppLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ImportService implements com.amalitech.service.api.IImportService{

    private final StudentManager studentManager;
    private final GradeManager gradeManager;
    private final CSVParser parser;

    public ImportService(StudentManager studentManager, GradeManager gradeManager, CSVParser parser) {
        this.studentManager = studentManager;
        this.gradeManager = gradeManager;
        this.parser = parser;
    }

    /**
     * Import grades from a CSV file.
     * @param csvPath Path to CSV file (e.g., ./imports/grades.csv)
     * @param autoSkipHeader true to automatically skip header line if present
     * @return ImportResult summary
     */
    public ImportResult importCsv(Path csvPath, boolean autoSkipHeader) {
        // Check file
        if (csvPath == null || !Files.exists(csvPath) || !Files.isRegularFile(csvPath)) {
            return new ImportResult(csvPath, null, 0, 0, 0,
                    List.of("File not found or not a regular file: " + csvPath), false);
        }

        // Prepare log path next to the CSV file (./imports/)
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path logPath = csvPath.getParent().resolve("import_log_" + ts + ".txt");

        int totalRows = 0;
        int success = 0;
        int failed = 0;
        List<String> failures = new ArrayList<>();
        List<String> rawLines = new ArrayList<>();

        // Read CSV lines
        try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                rawLines.add(line);
            }
        } catch (IOException e) {
            failures.add("I/O error while reading CSV: " + e.getMessage());
            writeLogSafely(logPath, failures, totalRows, success, failed);
            return new ImportResult(csvPath, logPath, totalRows, success, failed, failures, true);
        }

        // Parse rows using your existing CSVParser
        List<String[]> rows;
        try {
            rows = parser.parseLines(rawLines, autoSkipHeader); // auto-skip header if present
        } catch (com.amalitech.exceptions.CsvFormatException cfe) {
            failures.add("CSV format error: " + cfe.getMessage());
            writeLogSafely(logPath, failures, totalRows, success, failed);
            return new ImportResult(csvPath, logPath, totalRows, success, failed, failures, true);
        }

        // Process rows
        try (BufferedWriter log = Files.newBufferedWriter(logPath, StandardCharsets.UTF_8)) {
            for (int i = 0; i < rows.size(); i++) {
                String[] parts = rows.get(i);
                int rowNum = i + 1;
                totalRows++;

                // Validate column count
                if (parts.length != 4) {
                    failed++;
                    String reason = "Row " + rowNum + ": Invalid column count (" + parts.length + ")";
                    failures.add(reason);
                    log.write(reason); log.newLine();
                    continue;
                }

                String sid = safeTrim(parts[0]).toUpperCase();
                String subj = safeTrim(parts[1]);
                String type = safeTrim(parts[2]);
                String gradeStr = safeTrim(parts[3]);

                // Validate student
                Student student = studentManager.findStudent(sid);
                if (student == null) {
                    failed++;
                    String reason = "Row " + rowNum + ": Invalid student ID (" + sid + ")";
                    failures.add(reason);
                    log.write(reason); log.newLine();
                    continue;
                }

                // Validate subject type
                boolean isCore = "Core".equalsIgnoreCase(type);
                boolean isElective = "Elective".equalsIgnoreCase(type);
                if (!isCore && !isElective) {
                    failed++;
                    String reason = "Row " + rowNum + ": Invalid subject type (" + type + ")";
                    failures.add(reason);
                    log.write(reason); log.newLine();
                    continue;
                }

                // Parse grade value
                double pct;
                try {
                    pct = Double.parseDouble(gradeStr);
                } catch (NumberFormatException nfe) {
                    failed++;
                    String reason = "Row " + rowNum + ": Grade not a number (" + gradeStr + ")";
                    failures.add(reason);
                    log.write(reason); log.newLine();
                    continue;
                }
                if (pct < 0 || pct > 100) {
                    failed++;
                    String reason = "Row " + rowNum + ": Grade out of range (" + gradeStr + ")";
                    failures.add(reason);
                    log.write(reason); log.newLine();
                    continue;
                }

                // Build subject
                Subject subject = isCore
                        ? new CoreSubject(subj, "C" + (int) (Math.random() * 1000))
                        : new ElectiveSubject(subj, "E" + (int) (Math.random() * 1000));

                // Create and store grade
                Grade grade = new Grade(sid, subject, pct);
                gradeManager.addGrade(grade);

                // Record via Student & enroll subject
                boolean ok = student.recordGrade(pct);
                if (!ok) {
                    failed++;
                    String reason = "Row " + rowNum + ": Student grade storage full or invalid";
                    failures.add(reason);
                    log.write(reason); log.newLine();
                    continue;
                }
                student.enrollSubject(subject);

                // Success
                success++;
            }

            // Write import summary
            log.newLine();
            log.write("IMPORT SUMMARY"); log.newLine();
            log.write("Total Rows: " + totalRows); log.newLine();
            log.write("Successfully Imported: " + success); log.newLine();
            log.write("Failed: " + failed); log.newLine();

        } catch (IOException e) {
            failures.add("I/O error while writing import log: " + e.getMessage());
        }

        // Operational log (optional, consistent with your style)
        AppLogger.info(String.format("Bulk Import completed: total=%d success=%d failed=%d file=%s",
                totalRows, success, failed, csvPath.getFileName()));

        return new ImportResult(csvPath, logPath, totalRows, success, failed, failures, true);
    }

    // Helpers
    private static String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }

    private static void writeLogSafely(Path logPath, List<String> failures, int total, int success, int failed) {
        if (logPath == null) return;
        try (BufferedWriter log = Files.newBufferedWriter(logPath, StandardCharsets.UTF_8)) {
            for (String f : failures) { log.write(f); log.newLine(); }
            log.newLine();
            log.write("IMPORT SUMMARY"); log.newLine();
            log.write("Total Rows: " + total); log.newLine();
            log.write("Successfully Imported: " + success); log.newLine();
            log.write("Failed: " + failed); log.newLine();
        } catch (IOException ignored) { /* best-effort */ }
    }
}
