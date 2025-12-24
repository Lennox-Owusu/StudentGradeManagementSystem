
package com.amalitech.io;

import com.amalitech.exceptions.CsvFormatException;
import java.util.ArrayList;
import java.util.List;

public class CSVParser {
    public List<String[]> parseLines(List<String> lines, boolean hasHeader) throws CsvFormatException {
        List<String[]> rows = new ArrayList<>();
        if (lines == null || lines.isEmpty()) return rows;

        int start = 0;
        if (hasHeader) {
            if (!isHeader(lines.get(0))) {
                throw new CsvFormatException("Header must be: StudentID,SubjectName,SubjectType,Grade");
            }
            start = 1;
        }

        for (int i = start; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null) continue;
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] cols = line.split(",", -1);
            if (cols.length != 4) {
                throw new CsvFormatException("Row " + (i + 1) + " has " + cols.length + " columns; expected 4.");
            }
            rows.add(cols);
        }
        return rows;
    }

    private boolean isHeader(String line) {
        if (line == null) return false;
        String lower = line.trim().toLowerCase();
        return lower.startsWith("studentid,") && lower.contains(",subjectname,")
                && lower.contains(",subjecttype,") && lower.endsWith(",grade");
    }
}
