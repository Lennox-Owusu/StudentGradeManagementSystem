
package com.amalitech.menu;

import com.amalitech.manager.StudentManager;
import com.amalitech.manager.GradeManager;
import com.amalitech.service.api.*;

public class AppContext {
    public final StudentManager studentManager;
    public final GradeManager gradeManager;
    public final IReportService reportService;
    public final IExportService exportService;
    public final IImportService importService;
    public final IGpaService gpaService;
    public final IStatisticsService statisticsService;

    public AppContext(StudentManager studentManager,
                      GradeManager gradeManager,
                      IReportService reportService,
                      IExportService exportService,
                      IImportService importService,
                      IGpaService gpaService,
                      IStatisticsService statisticsService) {
        this.studentManager = studentManager;
        this.gradeManager = gradeManager;
        this.reportService = reportService;
        this.exportService = exportService;
        this.importService = importService;
        this.gpaService = gpaService;
        this.statisticsService = statisticsService;
    }
}
