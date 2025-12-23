
package com.amalitech.io;

import java.util.ArrayList;
import java.util.List;

/**
 * CSVParser: focuses ONLY on parsing CSV lines into columns.
 * - Does not perform file I/O (reading/writing).
 * - Does not validate business rules (student IDs, subject types, ranges).
 * - Keeps empty fields via split(",", -1).
 */
public class CSVParser {

    /**
     * Parses raw lines into rows of String[] columns.
     * - Skips blank lines.
     * - Optionally skips header if present ("studentid,subjectname,subjecttype,grade").
     *
     * @param lines    raw file lines
     * @param hasHeader true if the first line is a header row
     * @return list of rows (String[4]) or longer; caller validates column count
     */
    public List<String[]> parseLines(List<String> lines, boolean hasHeader) {
        List<String[]> rows = new ArrayList<>();
        if (lines == null || lines.isEmpty()) return rows;

        int start = 0;
        if (hasHeader && isHeader(lines.get(0))) {
            start = 1;
        }

        for (int i = start; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null) continue;
            line = line.trim();
            if (line.isEmpty()) continue;

            // Keep empty tokens, do not trim tokens here (caller can trim)
            String[] cols = line.split(",", -1);
            rows.add(cols);
        }
        return rows;
    }

    /** Case-insensitive check for the expected 4-column header. */
    private boolean isHeader(String line) {
        if (line == null) return false;
        String lower = line.trim().toLowerCase();
        return lower.startsWith("studentid,") && lower.contains(",subjectname,")
                && lower.contains(",subjecttype,") && lower.endsWith(",grade");
    }
}
