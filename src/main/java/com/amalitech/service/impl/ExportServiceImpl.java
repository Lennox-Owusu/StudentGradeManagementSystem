
package com.amalitech.service.impl;

import com.amalitech.Grade;
import com.amalitech.GradeManager;
import com.amalitech.Student;
import com.amalitech.exceptions.ExportFailedException;
import com.amalitech.io.ExportCoordinator;
import com.amalitech.reporting.StudentReport;
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
        // Collect grades for the student
        var list = new ArrayList<Grade>();
        for (int i = 0; i < gm.getGradeCount(); i++) {
            Grade g = gm.getGradeAt(i);
            if (g != null && g.getStudentId().equalsIgnoreCase(student.getStudentId())) list.add(g);
        }

        double coreAvg = gm.calculateCoreAverage(student.getStudentId());
        double elecAvg = gm.calculateElectiveAverage(student.getStudentId());
        double overall = gm.calculateOverallAverage(student.getStudentId());

        StudentReport report = new StudentReport(
                student, list, coreAvg, elecAvg, overall, "+1-555-0123"
        );

        boolean doCsv  = (formatChoice == 1 || formatChoice == 4);
        boolean doJson = (formatChoice == 2 || formatChoice == 4);
        boolean doBin  = (formatChoice == 3 || formatChoice == 4);

        Path base = Paths.get("./reports");
        String baseName = student.getName().toLowerCase().replaceAll("\\s+", "_") + "_detailed";

        coordinator.exportAll(report, base, baseName, doCsv, doJson, doBin);
    }
}
