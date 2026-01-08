
package com.amalitech;

import com.amalitech.calculation.StatisticsCalculator;
import com.amalitech.io.CSVParser;
import com.amalitech.io.FileExporter;
import com.amalitech.manager.GradeManager;
import com.amalitech.manager.StudentManager;
import com.amalitech.menu.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.menu.MenuConfig;
import com.amalitech.menu.IDataSeeder;
import com.amalitech.menu.DemoDataSeeder;
import com.amalitech.reporting.GPACalculator;
import com.amalitech.reporting.ReportGenerator;
import com.amalitech.service.*;
import com.amalitech.service.api.*;


import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // Core managers
        StudentManager studentManager = new StudentManager(50);
        GradeManager gradeManager = new GradeManager(200);

        // Seed demo data via abstraction (DIP)
        IDataSeeder seeder = new DemoDataSeeder();
        seeder.seed(studentManager, gradeManager);

        // Instantiate services (DIP via interfaces)
        IReportService reportService = new ReportService(new ReportGenerator(), gradeManager);
        IExportService exportService = new ExportService(new FileExporter());
        IImportService importService = new ImportService(studentManager, gradeManager, new CSVParser());
        IGpaService gpaService = new GpaService(studentManager, gradeManager, new GPACalculator());
        IStatisticsService statisticsService = new StatisticsService(studentManager, gradeManager, new StatisticsCalculator());

        AppContext ctx = new AppContext(studentManager, gradeManager, reportService, exportService, importService, gpaService, statisticsService);

        // Load actions (OCP: add/remove actions in MenuConfig)
        List<MenuAction> actions = MenuConfig.actions();

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.println("\n" +
                    "╔" + "═".repeat(38) + "╗");
            System.out.println("║ STUDENT GRADE MANAGEMENT - MAIN MENU ║");
            System.out.println("╚" + "═".repeat(38) + "╝");

            for (int i = 0; i < actions.size(); i++) {
                System.out.printf("%d. %s%n", (i + 1), actions.get(i).label());
            }
            System.out.println((actions.size() + 1) + ". Exit");

            System.out.print("\nEnter choice: ");
            int choice;
            try { choice = Integer.parseInt(scanner.nextLine()); }
            catch (NumberFormatException e) { System.out.println("Invalid input. Please enter a number."); continue; }

            if (choice == actions.size() + 1) {
                System.out.println("Thank you for using Grade Management System!");
                System.out.println("Goodbye!");
                exit = true;
            } else if (choice >= 1 && choice <= actions.size()) {
                actions.get(choice - 1).execute(scanner, ctx);
            } else {
                System.out.println("Invalid choice. Please select between 1-" + (actions.size() + 1));
            }
        }
        scanner.close();
    }
}
