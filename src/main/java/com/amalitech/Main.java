
package com.amalitech;

import com.amalitech.app.AppContext;
import com.amalitech.app.ConsoleApp;
import com.amalitech.exceptions.ValidationException;
import com.amalitech.menu.actions.*;

public class Main {
    public static void main(String[] args) throws ValidationException {
        AppContext ctx = new AppContext();

        // (Optional) preload sample data
        ctx.students.addStudent("STU001","Alice Johnson","alice@school.edu","0241108345", false);
        ctx.students.addStudent("STU002","Bob Smith","bob@school.edu","0256521345", true);
        ctx.students.addStudent("STU003","Carol Martinez","carol@school.edu","0545678345", false);

        ConsoleApp app = new ConsoleApp(ctx)
                .register(new AddStudentAction(ctx))             // 1
                .register(new ViewStudentsAction(ctx))           // 2
                .register(new RecordGradeAction(ctx))            // 3
                .register(new ViewGradeReportAction(ctx))        // 4
                .register(new ExportReportAction(ctx))           // 5
                .register(new ImportDataAction(ctx))             // 6
                .register(new BulkImportGradesAction(ctx))       // 7
                .register(new CalculateGpaAction(ctx))           // 8
                .register(new ViewClassStatisticsAction(ctx))    // 9
                .register(new RealTimeDashboardAction(ctx))      // 10
                .register(new PerformanceMonitorAction(ctx))     // 11
                .register(new SearchStudentsAction(ctx))         // 12
                .register(new QueryGradeHistoryAction(ctx))      // 13
                .register(new CacheManagementAction(ctx))        // 14
                .register(new AuditTrailAction(ctx))             // 15
                ;

        try {
            app.run();
        } finally {
            ctx.shutdown();
        }
    }
}
