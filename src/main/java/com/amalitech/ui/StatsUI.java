
package com.amalitech.ui;

import com.amalitech.service.api.IStatisticsService;
import java.util.Scanner;

public class StatsUI {
    public static void viewClassStatistics(Scanner scanner, IStatisticsService statisticsService) {
        String report = statisticsService.buildClassStatisticsReport();
        System.out.println(report);
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}
