
package com.amalitech.io;

import com.amalitech.Grade;
import com.amalitech.Subject;
import com.amalitech.CoreSubject;
import com.amalitech.ElectiveSubject;
import com.amalitech.reporting.StudentReport;
import com.amalitech.exceptions.CsvFormatException;
import com.amalitech.exceptions.ImportFailedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

//Imports a detailed StudentReport from the CSV produced by CSVReportExporter.
public final class CSVReportImporter implements Importer<StudentReport> {

    @Override
    public StudentReport importFrom(Path source) throws ImportFailedException, CsvFormatException {
        if (source == null || !Files.exists(source) || !Files.isRegularFile(source)) {
            throw new ImportFailedException("CSV file not found: " + String.valueOf(source));
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
        } catch (IOException ioe) {
            throw new ImportFailedException("Failed reading CSV: " + source.toAbsolutePath(), ioe);
        }

        if (lines.isEmpty())
            throw new CsvFormatException("CSV is empty.");

        // Validate header (tolerate casing/whitespace;
        String header = lines.get(0).trim().toLowerCase();
        String expected = "studentid,name,email,phone,type,subject,type,grade,date";
        if (!header.equals(expected)) {
            throw new CsvFormatException("Unexpected header. Expected: " + expected);
        }

        String studentId = null, name = null, email = null, phone = null, studentType = null;
        List<Grade> grades = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null) continue;
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] cols = splitCsv(line); // robust CSV split honoring quotes
            if (cols.length != 9) {
                throw new CsvFormatException("Row " + (i + 1) + " has " + cols.length + " columns; expected 9.");
            }

            // Extract and normalize
            String sid = cols[0].trim();
            String sname = unescape(cols[1]);
            String semail = unescape(cols[2]);
            String sphone = unescape(cols[3]);
            String stype = cols[4].trim();
            String subjName = unescape(cols[5]);
            String subjType = cols[6].trim();
            String gradeStr = cols[7].trim();
            String date = cols[8].trim(); // Grade DTO stores date automatically;

            if (studentId == null) {
                studentId = sid;
                name = sname;
                email = semail;
                phone = sphone;
                studentType = stype;
            } else if (!studentId.equalsIgnoreCase(sid)) {
                throw new CsvFormatException("Multiple StudentIDs detected (" + studentId + " vs " + sid + ").");
            }

            double gVal;
            try { gVal = Double.parseDouble(gradeStr); }
            catch (NumberFormatException nfe) {
                throw new CsvFormatException("Row " + (i + 1) + ": grade not a number (" + gradeStr + ").");
            }
            if (gVal < 0 || gVal > 100) {
                throw new CsvFormatException("Row " + (i + 1) + ": grade out of range (" + gradeStr + ").");
            }

            Subject subject = "Core".equalsIgnoreCase(subjType)
                    ? new CoreSubject(subjName, "C" + (int)(Math.random()*1000))
                    : new ElectiveSubject(subjName, "E" + (int)(Math.random()*1000));

            grades.add(new Grade(sid, subject, gVal));
        }

        if (studentId == null) throw new CsvFormatException("No data rows found.");

        // Compute aggregates on the fly (simple averages)
        double coreSum=0, coreCnt=0, elSum=0, elCnt=0, allSum=0;
        for (Grade g : grades) {
            allSum += g.getGrade();
            if ("Core".equalsIgnoreCase(g.getSubject().getSubjectType())) { coreSum += g.getGrade(); coreCnt++; }
            else { elSum += g.getGrade(); elCnt++; }
        }
        double coreAvg = coreCnt == 0 ? 0.0 : coreSum / coreCnt;
        double elecAvg = elCnt == 0 ? 0.0 : elSum / elCnt;
        double overallAvg = grades.isEmpty() ? 0.0 : allSum / grades.size();

        return new StudentReport(

                new com.amalitech.RegularStudent(name, 16, email, phone), // age default 16
                grades, coreAvg, elecAvg, overallAvg, phone
        );
    }

    // --- Helpers: small CSV splitter honoring quotes (",") ---
    private static String[] splitCsv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"'); i++; // escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    private static String unescape(String s) {
        if (s == null) return "";
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\"\"", "\"");
    }
}
