
package com.amalitech.ui;

import com.amalitech.service.api.IImportService;
import com.amalitech.service.ImportResult;
import com.amalitech.util.ErrorHandler;
import com.amalitech.util.Validators;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ImportUI {
    public static void bulkImportGrades(Scanner scanner, IImportService importService) {
        System.out.println("\nBULK IMPORT GRADES");
        System.out.println("─".repeat(50));
        System.out.println("Place your CSV file in: ./imports/");
        System.out.println();
        System.out.println("CSV Format Required:");
        System.out.println("StudentID,SubjectName,SubjectType,Grade");
        System.out.println("Example: STU001,Mathematics,Core,85");
        System.out.println();

        String baseName;
        while (true) {
            System.out.print("Enter filename (without extension): ");
            String input = scanner.nextLine();
            try { baseName = Validators.requireNotBlank("Filename", input); break; }
            catch (IllegalArgumentException iae) { ErrorHandler.handle("Bulk Import > filename", iae); }
        }

        Path importsDir = Paths.get("./imports");
        try { if (!Files.exists(importsDir)) Files.createDirectories(importsDir); }
        catch (IOException e) { System.out.println("Failed to ensure imports directory: " + e.getMessage()); System.out.print("\nPress Enter to continue..."); scanner.nextLine(); return; }

        Path csvPath = importsDir.resolve(baseName + ".csv");
        System.out.print("\nValidating file... ");
        if (!Files.exists(csvPath) || !Files.isRegularFile(csvPath)) {
            System.out.println("✗");
            System.out.println("File not found: " + csvPath);
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        } else { System.out.println("✓"); }

        ImportResult result = importService.importCsv(csvPath, true);

        System.out.println();
        System.out.println("IMPORT SUMMARY");
        System.out.println("─".repeat(50));
        System.out.println("Total Rows: " + result.getTotalRows());
        System.out.println("Successfully Imported: " + result.getSuccessCount());
        System.out.println("Failed: " + result.getFailureCount());
        if (!result.getFailures().isEmpty()) {
            System.out.println();
            System.out.println("Failed Records:");
            for (String f : result.getFailures()) System.out.println(f);
        }
        System.out.println();
        System.out.println("✓ Import completed!");
        System.out.println(result.getSuccessCount() + " grades added to system");
        if (result.getLogPath() != null) {
            System.out.println("See " + result.getLogPath().getFileName() + " for details");
        }
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}
