
package com.amalitech;

import org.junit.Test;

import java.time.LocalDate;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class GradeTest {

    // --- Helper: create a Subject instance for tests ---
    private Subject createSubject() {
        // If you have CoreSubject in src/main/java/com/amalitech, this should work:
        return new CoreSubject("Mathematics", "MTH101");


    }

    // --- VALID CONSTRUCTION TESTS ---

    @Test
    public void constructsValidGrade_withAllFieldsSet() {
        Subject math = createSubject();
        Grade g = new Grade("STU001", math, 85.0);

        assertNotNull("gradeId should be generated", g.getGradeId());
        assertTrue("gradeId should start with 'GRD'", g.getGradeId().startsWith("GRD"));
        assertEquals("studentId should be stored", "STU001", g.getStudentId());
        assertEquals("subject should be stored", math, g.getSubject());
        assertEquals("grade value should be stored", 85.0, g.getGrade(), 0.0001);

        // Date is ISO string from LocalDate.now()
        String today = LocalDate.now().toString();
        assertEquals("date should be today's ISO date", today, g.getDate());
    }

    @Test
    public void allowsBoundaryGrades_zeroAndHundred() {
        Subject math = createSubject();

        Grade g0 = new Grade("STU002", math, 0.0);
        assertEquals(0.0, g0.getGrade(), 0.0001);

        Grade g100 = new Grade("STU003", math, 100.0);
        assertEquals(100.0, g100.getGrade(), 0.0001);
    }

    @Test
    public void gradeIdIncrementsAcrossInstances() {
        Subject math = createSubject();

        Grade g1 = new Grade("STU010", math, 70.0);
        Grade g2 = new Grade("STU011", math, 71.0);

        // Both start with GRD and should not be equal
        assertTrue(g1.getGradeId().startsWith("GRD"));
        assertTrue(g2.getGradeId().startsWith("GRD"));
        assertNotEquals("Each gradeId should be unique", g1.getGradeId(), g2.getGradeId());
    }

    @Test
    public void dateIsIsoFormat_yyyy_MM_dd() {
        Subject math = createSubject();
        Grade g = new Grade("STU099", math, 60.0);

        String date = g.getDate();
        // Simple ISO date pattern check (yyyy-MM-dd)
        Pattern iso = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        assertTrue("date should be ISO yyyy-MM-dd", iso.matcher(date).matches());
    }

    // --- INVALID INPUT TESTS ---

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullStudentId() {
        Subject math = createSubject();
        new Grade(null, math, 50.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankStudentId() {
        Subject math = createSubject();
        new Grade("   ", math, 50.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullSubject() {
        new Grade("STU004", null, 50.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeGrade() {
        Subject math = createSubject();
        new Grade("STU005", math, -0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsGradeAbove100() {
        Subject math = createSubject();
        new Grade("STU006", math, 100.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNaNGrade() {
        Subject math = createSubject();
        new Grade("STU007", math, Double.NaN);
    }

    // --- IMMUTABILITY / READ-ONLY STATE CHECKS ---
    @Test
    public void fieldsRemainReadOnly_afterConstruction() {
        Subject math = createSubject();
        Grade g = new Grade("STU008", math, 77.0);

        // We only have getters; no setters exist.
        assertEquals("STU008", g.getStudentId());
        assertEquals(math, g.getSubject());
        assertEquals(77.0, g.getGrade(), 0.0001);
        assertNotNull(g.getGradeId());
        assertNotNull(g.getDate());
    }

}
