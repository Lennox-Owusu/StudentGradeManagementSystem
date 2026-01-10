
package com.amalitech.io;

import com.amalitech.base.Grade;
import com.amalitech.base.HonorsStudent;
import com.amalitech.base.RegularStudent;
import com.amalitech.base.Student;
import com.amalitech.manager.GradeManager;
import com.amalitech.manager.StudentManager;
import com.amalitech.reporting.StudentReport;
import com.amalitech.util.AppLogger;
import com.amalitech.exceptions.*;

import java.nio.file.Path;

public final class ImportCoordinator {

    public enum Format { CSV, JSON, BINARY, AUTO }

    private final CSVReportImporter csv = new CSVReportImporter();
    private final JSONReportImporter json = new JSONReportImporter();
    private final BinaryReportImporter bin = new BinaryReportImporter();

    public StudentReport loadReport(Path path, Format format) throws DomainException {
        Format f = (format == Format.AUTO) ? detect(path) : format;
        switch (f) {
            case CSV -> { return csv.importFrom(path); }
            case JSON -> { return json.importFrom(path); }
            case BINARY -> { return bin.importFrom(path); }
            default -> throw new ImportFailedException("Unknown format for: " + path);
        }
    }

    public void mergeIntoSystem(StudentReport report, StudentManager sm, GradeManager gm) {
        if (report == null) return;
        String sid = report.getStudentId();
        Student s = sm.findStudent(sid);

        if (s == null) {
            // Create a new student based on type;
            if ("Honors Student".equalsIgnoreCase(report.getType())) {
                s = new HonorsStudent(report.getName(), 16, report.getEmail(), report.getPhone());
            } else {
                s = new RegularStudent(report.getName(), 16, report.getEmail(), report.getPhone());
            }

            sm.addStudent(s);
            AppLogger.info("Import: created student (newId=" + s.getStudentId() + ", importedId=" + sid + ")");
        }

        // Add grades and enroll subjects
        for (Grade g : report.getGrades()) {
            gm.addGrade(g);
            s.recordGrade(g.getGrade());
            s.enrollSubject(g.getSubject());
        }

        AppLogger.info("Import merge finished for student " + sid
                + " — grades added: " + report.getTotalGrades());
    }

    private static Format detect(Path p) {
        String name = p.getFileName().toString().toLowerCase();
        if (name.endsWith(".csv")) return Format.CSV;
        if (name.endsWith(".json")) return Format.JSON;
        if (name.endsWith(".dat")) return Format.BINARY;
        return Format.AUTO;
    }
}
