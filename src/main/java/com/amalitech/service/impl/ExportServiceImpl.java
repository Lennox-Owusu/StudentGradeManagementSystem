
package com.amalitech.service.impl;

import com.amalitech.base.Grade;
import com.amalitech.manager.GradeManager;
import com.amalitech.base.Student;
import com.amalitech.exceptions.ExportFailedException;
import com.amalitech.io.ExportCoordinator;
import com.amalitech.reporting.ReportType;
import com.amalitech.reporting.StudentReport;
import com.amalitech.service.api.ExportSummary;
import com.amalitech.service.api.FormatResult;
import com.amalitech.service.api.IExportService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ExportServiceImpl implements IExportService {
    private final GradeManager gm;
    private final ExportCoordinator coordinator;

    public ExportServiceImpl(GradeManager gm, ExportCoordinator coordinator) {
        this.gm = gm; this.coordinator = coordinator;
    }

    @Override
    public void exportDetailedReport(Student student, int formatChoice) throws ExportFailedException {
        // Delegate to new API with DETAILED type
        exportReport(student, formatChoice, ReportType.DETAILED);
    }

    @Override
    public ExportSummary exportReport(Student student, int formatChoice, ReportType reportType)
            throws ExportFailedException {
        // Collect grades for the student
        var list = new ArrayList<Grade>();
        for (int i = 0; i < gm.getGradeCount(); i++) {
            Grade g = gm.getGradeAt(i);
            if (g != null && g.getStudentId().equalsIgnoreCase(student.getStudentId())) list.add(g);
        }
        double coreAvg = gm.calculateCoreAverage(student.getStudentId());
        double elecAvg = gm.calculateElectiveAverage(student.getStudentId());
        double overall = gm.calculateOverallAverage(student.getStudentId());

        // Build StudentReport — use student's actual phone
        StudentReport report = new StudentReport(
                student, list, coreAvg, elecAvg, overall, student.getPhone()
        );

        boolean doCsv = (formatChoice == 1 || formatChoice == 4);
        boolean doJson = (formatChoice == 2 || formatChoice == 4);
        boolean doBin = (formatChoice == 3 || formatChoice == 4);

        Path base = Paths.get("./reports");
        String styleSuffix = switch (reportType) {
            case SUMMARY -> "summary";
            case TRANSCRIPT -> "transcript";
            case ANALYTICS -> "analytics";
            default -> "detailed";
        };
        String baseName = student.getName().toLowerCase().replaceAll("\\s+", "_") + "_" + styleSuffix;

        // Run export (parallel when 4=All); coordinator returns timing/size metrics
        ExportCoordinator.PerformanceSummary perf =
                coordinator.exportAll(report, base, baseName, doCsv, doJson, doBin);  // uses csv/json/bin exporters internally [3](https://amalitech-my.sharepoint.com/personal/lennox_afriyie_amalitech_com/Documents/Microsoft%20Copilot%20Chat%20Files/ExportCoordinator.java)

        // Map to ExportSummary + per-format results (filenames are deterministic)
        ExportSummary summary = new ExportSummary();
        summary.setTotalTimeMs(perf.totalMillis);
        summary.setTotalSizeBytes(perf.totalBytes);
        summary.setParallelWrites((doCsv?1:0) + (doJson?1:0) + (doBin?1:0));

        // Compression Ratio (binary vs JSON) — coordinator already computes; keep number only
        if (perf.jsonBytes > 0 && perf.binBytes > 0) {
            double ratio = (double) perf.jsonBytes / (double) perf.binBytes;
            summary.setCompressionRatio(ratio);
        }

        if (doCsv) {
            summary.addResult(new FormatResult(
                    "CSV", baseName + ".csv", "./reports/csv/",
                    perf.csvBytes, perf.csvMillis,
                    "Rows: " + list.size() + " grades + header"
            ));
        }
        if (doJson) {
            summary.addResult(new FormatResult(
                    "JSON", baseName + ".json", "./reports/json/",
                    perf.jsonBytes, perf.jsonMillis,
                    "Structure: Nested objects with metadata"
            ));
        }
        if (doBin) {
            summary.addResult(new FormatResult(
                    "Binary", baseName + ".dat", "./reports/binary/",
                    perf.binBytes, perf.binMillis,
                    "Format: Serialized StudentReport object"
            ));
        }

        return summary;
    }
}

