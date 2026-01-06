
package com.amalitech.util;

import com.amalitech.exceptions.*;
import java.nio.file.AccessDeniedException;

public final class ErrorHandler {
    private ErrorHandler() {}

    //Shows a friendly console message and logs details.
    public static void handle(String context, Throwable error) {

        switch (error) {

            case com.amalitech.exceptions.ValidationException ve -> {
                AppLogger.error(context + ": regex validation failed", ve);
                System.out.println("\n" + ve.getMessage());
            }
            case InvalidGradeException ige -> {
                AppLogger.error(context + ": invalid grade value=" + ige.getValue(), ige);
                println("✖ Invalid grade. Grades must be between 0 and 100.",
                        "Try entering a number in range (e.g., 75).");
            }
            case CsvFormatException cfe -> {
                AppLogger.error(context + ": CSV format issue", cfe);
                println("✖ CSV format error.",
                        "Ensure the header is: StudentID,SubjectName,SubjectType,Grade and every row has 4 columns.");
            }
            case ExportFailedException efe -> {
                AppLogger.error(context + ": export failed", efe);
                println("✖ Export failed.",
                        "Check the target path is writable and try again. If the file is open in another app, close it first.");
            }
            case IllegalArgumentException iae -> {
                AppLogger.error(context + ": illegal argument", iae);
                println("✖ Invalid input: " + iae.getMessage(),
                        "Provide valid values and retry.");
            }
            case AccessDeniedException ade -> {
                AppLogger.error(context + ": filesystem permission denied", ade);
                println("✖ Permission denied while accessing files.",
                        "Run the app with proper permissions or choose a different folder.");
            }
            case null, default -> {
                // Specific types only
                AppLogger.error(context + ": unexpected error", error);
                println("✖ Unexpected error occurred.",
                        "Please try the operation again. If the issue persists, check logs in ./logs/app.log.");
            }
        }
    }

    private static void println(String message, String suggestion) {
        System.out.println("\n" + message);
        System.out.println("• Suggestion: " + suggestion + "\n");
    }
}
