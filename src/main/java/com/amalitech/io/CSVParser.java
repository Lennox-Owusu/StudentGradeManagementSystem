
package com.amalitech.io;

import java.util.ArrayList;
import java.util.List;


//CSVParser: focuses ONLY on parsing CSV lines into columns.
public class CSVParser {


     //Parses raw lines into rows of String[] columns.
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

            String[] cols = line.split(",", -1);
            rows.add(cols);
        }
        return rows;
    }

    //Case-insensitive check for the expected 4-column header.
    private boolean isHeader(String line) {
        if (line == null) return false;
        String lower = line.trim().toLowerCase();
        return lower.startsWith("studentid,") && lower.contains(",subjectname,")
                && lower.contains(",subjecttype,") && lower.endsWith(",grade");
    }
}
