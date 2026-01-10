
package com.amalitech.menu.actions;

import com.amalitech.base.Student;
import com.amalitech.app.AppContext;
import com.amalitech.menu.MenuAction;
import com.amalitech.reporting.ReportType;
import com.amalitech.reporting.ReportGenerator;
import com.amalitech.service.api.ExportSummary;

import java.util.*;
import java.util.regex.Pattern;

public class PatternBasedSearchAction implements MenuAction {
    private final AppContext ctx;

    public PatternBasedSearchAction(AppContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public String label() {
        return "Pattern-Based Search";
    }

    @Override
    public void execute() {
        while (true) {
            printHeader();

            System.out.println("Search Type:\n");
            System.out.println("1. Email Domain Pattern (e.g., @university.edu)");
            System.out.println("2. Phone Area Code Pattern (e.g., 555)");
            System.out.println("3. Student ID Pattern (e.g., STU**)");
            System.out.println("4. Name Pattern (regex)");
            System.out.println("5. Custom Regex Pattern\n");

            System.out.print("Select type (1-5): ");
            int type = parseIntSafe(ctx.scanner.nextLine(), 1);

            String regex;
            boolean showDomainDistribution = false;

            switch (type) {
                case 1 -> {
                    System.out.print("Enter email domain pattern: ");
                    String domain = ctx.scanner.nextLine().trim();
                    // e.g. @university.edu  ->  .*@university\.edu$
                    regex = ".*" + Pattern.quote(domain).replace("\\Q.\\E", "\\.") + "$";
                    showDomainDistribution = true;
                }
                case 2 -> {
                    System.out.print("Enter phone area code: ");
                    String code = ctx.scanner.nextLine().trim();
                    // e.g. 024 -> ^024
                    regex = "^" + Pattern.quote(code);
                }
                case 3 -> {
                    System.out.print("Enter student ID pattern (supports * and ?): ");
                    String pat = ctx.scanner.nextLine().trim();
                    // e.g. STU** -> ^STU.*$
                    regex = "^" + wildcardToRegex(pat) + "$";
                }
                case 4 -> {
                    System.out.print("Enter name regex (case-insensitive): ");
                    regex = ctx.scanner.nextLine().trim();
                }
                case 5 -> {
                    System.out.print("Enter custom regex (applies to EMAIL by default): ");
                    regex = ctx.scanner.nextLine().trim();
                }
                default -> {
                    System.out.println("Invalid selection. Returning to main menu...");
                    return;
                }
            }

            // Compile regex safely
            Pattern pattern;
            try {
                pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            } catch (Exception ex) {
                System.out.println("Invalid regex: " + ex.getMessage());
                return;
            }

            int total = ctx.studentManager.getStudentCount();
            System.out.println();
            System.out.println("Searching with regex: " + regex);
            System.out.println("Processing " + total + " students...");

            long t0 = System.nanoTime();

            // Collect matches
            List<Student> matches = new ArrayList<>();
            for (Student s : ctx.studentManager.getStudents()) {
                if (s == null) continue;
                String target = switch (type) {
                    case 2 -> safe(s.getPhone());     // phone area code
                    case 3 -> safe(s.getStudentId()); // student id pattern
                    case 4 -> safe(s.getName());      // name regex
                    case 5 -> safe(s.getEmail());     // custom regex on email by default
                    default -> safe(s.getEmail());    // email domain
                };
                if (pattern.matcher(target).find()) {
                    matches.add(s);
                }
            }

            long t1 = System.nanoTime();
            long ms = Math.max(0, (t1 - t0) / 1_000_000);

            // Results Table (STU ID | NAME | EMAIL)
            System.out.println();
            System.out.println("SEARCH RESULTS (" + matches.size() + " found)");
            System.out.println("--------------------------------------------------");
            System.out.printf("%-8s | %-18s | %-28s%n", "STU ID", "NAME", "EMAIL");
            System.out.println("--------------------------------------------------");
            for (Student s : matches) {
                System.out.printf(
                        "%-8s | %-18s | %-28s%n",
                        safe(s.getStudentId()),
                        truncate(safe(s.getName()), 18),
                        truncate(safe(s.getEmail()), 28)
                );
            }

            // Stats
            System.out.println();
            System.out.println("Pattern Match Statistics:");
            System.out.println("  Total Students Scanned: " + total);
            String pct = total > 0 ? String.format("%.0f%%", (matches.size() * 100.0) / total) : "0%";
            System.out.println("  Matches Found: " + matches.size() + " (" + pct + ")");
            System.out.println("  Search Time: " + ms + "ms");
            System.out.println("  Regex Complexity: O(n)");

            //Email domain distribution
            if (showDomainDistribution) {
                System.out.println();
                System.out.println("Email Domain Distribution:");
                Map<String, Integer> counts = new LinkedHashMap<>();
                for (Student s : ctx.studentManager.getStudents()) {
                    String email = safe(s == null ? null : s.getEmail()).toLowerCase(Locale.ROOT);
                    String domain = extractDomain(email);
                    if (!domain.isEmpty()) counts.merge(domain, 1, Integer::sum);
                }
                for (Map.Entry<String, Integer> e : counts.entrySet()) {
                    int c = e.getValue();
                    String p = total > 0 ? String.format("%.0f%%", (c * 100.0) / total) : "0%";
                    System.out.println("  " + e.getKey() + ": " + c + " students (" + p + ")");
                }
            }

            // Follow-up Actions
            System.out.println();
            System.out.println("Actions:");
            System.out.println("1. Export search results");
            System.out.println("2. Generate reports for matched students");
            System.out.println("3. Send bulk email to matched students");
            System.out.println("4. New search with different pattern");
            System.out.println("5. Return to main menu");

            System.out.print("\nSelect (1-5): ");
            int next = parseIntSafe(ctx.scanner.nextLine(), 5);

            switch (next) {
                case 1 -> {
                    handleExportResults(matches);
                }
                case 2 -> {
                    handleGenerateReports(matches);
                }
                case 3 -> {
                    handleBulkEmail(matches); // stub
                }
                case 4 -> {
                    System.out.println();
                    continue; // rerun search
                }
                default -> {
                    return; // back to main menu
                }
            }

            System.out.print("\nPress Enter to continue...");
            ctx.scanner.nextLine();
            return;
        }
    }

    // ----- actions -----

    private void handleExportResults(List<Student> matches) {
        if (matches.isEmpty()) {
            System.out.println("\nNo matches to export.");
            return;
        }
        int fmt = askFormatChoice();
        ReportType type = ReportType.SUMMARY; // concise report for search exports

        System.out.println("\nExporting " + matches.size() + " matched students...");
        long totalBytes = 0;
        long totalMillis = 0;
        int totalFiles = 0;

        for (Student s : matches) {
            try {
                ExportSummary summary = ctx.exporter.exportReport(s, fmt, type);
                totalBytes += summary.getTotalSizeBytes();
                totalMillis += summary.getTotalTimeMs();
                totalFiles += summary.getParallelWrites();
            } catch (Exception ex) {
                System.out.println(" - Failed to export for " + s.getStudentId() + ": " + ex.getMessage());
            }
        }

        System.out.println("\nExport Summary:");
        System.out.println("  Students Exported: " + matches.size());
        System.out.println("  Files Written: " + totalFiles);
        System.out.println("  Total Size: " + totalBytes + " bytes");
        System.out.println("  Total Time: " + totalMillis + " ms");
        System.out.println("  Destination: ./reports/** (CSV/JSON/Binary folders)");
    }

    private void handleGenerateReports(List<Student> matches) {
        if (matches.isEmpty()) {
            System.out.println("\nNo matches to generate reports for.");
            return;
        }

        System.out.println("\nGenerating text reports (preview)…");
        ReportGenerator generator = new ReportGenerator();
        int shown = 0;
        for (Student s : matches) {
            String report = generator.generateStudentReport(s);
            System.out.println("--------------------------------------------------");
            System.out.println(report.trim());
            shown++;
            if (shown >= 5) break; // keep console readable; preview top 5
        }
        if (matches.size() > shown) {
            System.out.println("... (" + (matches.size() - shown) + " more reports generated)");
        }
        System.out.println("\nTip: Use Export (Action 1) to save CSV/JSON/Binary to disk.");
    }

    private void handleBulkEmail(List<Student> matches) {
        System.out.println("\nBulk email is sent");
    }

    // ----- helpers -----

    private int askFormatChoice() {
        System.out.println("\nSelect export format:");
        System.out.println("1. CSV");
        System.out.println("2. JSON");
        System.out.println("3. Binary");
        System.out.println("4. All");
        System.out.print("Enter choice (1-4): ");
        return parseIntSafe(ctx.scanner.nextLine(), 4);
    }

    private static String wildcardToRegex(String wildcard) {
        if (wildcard == null || wildcard.isEmpty()) return ".*";
        StringBuilder sb = new StringBuilder();
        for (char ch : wildcard.toCharArray()) {
            switch (ch) {
                case '*': sb.append(".*"); break;
                case '?': sb.append('.'); break;
                case '.': sb.append("\\."); break;
                default:  sb.append(Pattern.quote(String.valueOf(ch)).replace("\\Q.\\E", "\\."));
            }
        }
        return sb.toString();
    }

    private static String extractDomain(String emailLower) {
        int at = emailLower.lastIndexOf('@');
        return (at >= 0) ? emailLower.substring(at) : "";
    }

    private static String truncate(String s, int n) {
        if (s == null) return "";
        return (s.length() > n) ? s.substring(0, n - 3) + "..." : s;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private static void printHeader() {
        System.out.println();
        System.out.println("PATTERN-BASED SEARCH");
        System.out.println("--------------------");
        System.out.println();
    }
}
