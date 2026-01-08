
package com.amalitech.menu;

import com.amalitech.menu.actions.*;
import java.util.ArrayList;
import java.util.List;

public class MenuConfig {
    public static List<MenuAction> actions() {
        List<MenuAction> list = new ArrayList<>();
        list.add(new AddStudentAction());
        list.add(new ViewStudentsAction());
        list.add(new RecordGradeAction());
        list.add(new ViewGradeReportAction());
        list.add(new ExportGradeReportAction());
        list.add(new CalculateGPAAction());
        list.add(new BulkImportGradesAction());
        list.add(new ViewClassStatisticsAction());
        list.add(new SearchStudentsAction());
        return list;
    }
}
