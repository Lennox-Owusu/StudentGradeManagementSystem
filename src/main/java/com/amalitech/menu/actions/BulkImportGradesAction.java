
package com.amalitech.menu.actions;

import com.amalitech.*;
import com.amalitech.app.AppContext;
import com.amalitech.io.CSVParser;
import com.amalitech.menu.MenuAction;
import com.amalitech.util.ErrorHandler;
import com.amalitech.util.Validators;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BulkImportGradesAction implements MenuAction {
    private final AppContext ctx;
    public BulkImportGradesAction(AppContext ctx) { this.ctx = ctx; }

    @Override public String label() { return "Bulk Import Grades (CSV)"; }

    @Override public void execute() {
        System.out.println("Place CSV in ./imports");
        String base;
        while (true) {
            try {
                System.out.print("Enter filename (without extension): ");
                base = Validators.requireNotBlank("Filename", ctx.scanner.nextLine());
                break;
            } catch (Exception e) { ErrorHandler.handle("Bulk Import > filename", e); }
        }

        Path csv = Paths.get("./imports").resolve(base + ".csv");
        if (!Files.exists(csv)) { System.out.println("Not found: " + csv); return; }

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path log = Paths.get("./imports").resolve("import_log_" + ts + ".txt");

        int total=0, success=0, failed=0;
        List<String> failures = new ArrayList<>();
        CSVParser parser = new CSVParser();

        try (BufferedWriter w = Files.newBufferedWriter(log, StandardCharsets.UTF_8);
             var lines = Files.lines(csv, StandardCharsets.UTF_8)) {

            var raw = lines.map(s -> s==null? "" : s).toList();
            var rows = parser.parseLines(raw, true);

            for (int i=0;i<rows.size();i++) {
                String[] r = rows.get(i);
                total++;
                if (r.length != 4) { failed++; log(w, failures, "Row "+(i+1)+": wrong column count ("+r.length+")"); continue; }

                String sid = safe(r[0]).toUpperCase();
                String subj = safe(r[1]);
                String type = safe(r[2]);
                String gradeStr = safe(r[3]);

                Student stu = ctx.students.find(sid);
                if (stu == null) { failed++; log(w, failures, "Row "+(i+1)+": invalid StudentID ("+sid+")"); continue; }

                boolean isCore = "Core".equalsIgnoreCase(type), isElec = "Elective".equalsIgnoreCase(type);
                if (!isCore && !isElec) { failed++; log(w, failures, "Row "+(i+1)+": invalid SubjectType ("+type+")"); continue; }

                double pct;
                try { pct = Double.parseDouble(gradeStr); }
                catch (NumberFormatException nfe) { failed++; log(w, failures, "Row "+(i+1)+": grade not a number ("+gradeStr+")"); continue; }
                if (pct < 0 || pct > 100) { failed++; log(w, failures, "Row "+(i+1)+": grade out of range ("+gradeStr+")"); continue; }

                Subject subject = isCore ? new CoreSubject(subj, "C"+(int)(Math.random()*1000))
                        : new ElectiveSubject(subj, "E"+(int)(Math.random()*1000));

                try {
                    ctx.grades.recordGrade(stu, subject, pct);
                    success++;
                } catch (Exception e) {
                    failed++; log(w, failures, "Row "+(i+1)+": "+e.getMessage());
                }
            }

            w.newLine(); w.write("IMPORT SUMMARY"); w.newLine();
            w.write("Total Rows: " + total); w.newLine();
            w.write("Successful: " + success); w.newLine();
            w.write("Failed: " + failed); w.newLine();

        } catch (Exception e) {
            ErrorHandler.handle("Bulk Import > IO/Parsing", e);
            return;
        }

        System.out.printf("Summary — Total: %d  Success: %d  Failed: %d%n", total, success, failed);
        if (!failures.isEmpty()) {
            System.out.println("Failed rows:"); failures.forEach(System.out::println);
        }
        System.out.println("Log: " + log.toAbsolutePath());
    }

    private static String safe(String s){ return s==null? "" : s.trim(); }
    private static void log(BufferedWriter w, List<String> list, String msg) throws Exception {
        list.add(msg); w.write(msg); w.newLine();
    }
}
