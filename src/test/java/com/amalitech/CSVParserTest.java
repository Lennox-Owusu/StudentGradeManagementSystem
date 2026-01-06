
package com.amalitech;

import com.amalitech.exceptions.CsvFormatException;
import com.amalitech.io.CSVParser;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class CSVParserTest {
    @Test
    public void parses_header_and_skips_blanks() throws CsvFormatException {
        CSVParser parser = new CSVParser();
        List<String> lines = Arrays.asList(
                "StudentID,SubjectName,SubjectType,Grade",
                "STU001,Mathematics,Core,85",
                "",
                "STU002,Art,Elective,78"
        );

        List<String[]> rows = parser.parseLines(lines, true);
        assertEquals(2, rows.size());
        assertArrayEquals(new String[]{"STU001","Mathematics","Core","85"}, rows.get(0));
        assertArrayEquals(new String[]{"STU002","Art","Elective","78"}, rows.get(1));
    }

    @Test
    public void preserves_empty_tokens() throws CsvFormatException {
        CSVParser parser = new CSVParser();
        List<String> lines = Arrays.asList(
                "StudentID,SubjectName,SubjectType,Grade",
                "STU003,,Core,90"
        );

        List<String[]> rows = parser.parseLines(lines, true);
        assertEquals(1, rows.size());
        String[] cols = rows.get(0);
        assertEquals(4, cols.length);
        assertEquals("STU003", cols[0]);
        assertEquals("", cols[1]);
        assertEquals("Core", cols[2]);
        assertEquals("90", cols[3]);
    }
}
