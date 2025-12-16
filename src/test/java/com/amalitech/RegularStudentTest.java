
package com.amalitech;

import org.junit.Test;

import static org.junit.Assert.*;

public class RegularStudentTest {

    @Test
    public void constructor_initializes_and_type_isRegular() {
        RegularStudent s = new RegularStudent("Esi Boakye", 21, "esi@example.com", "0240000002");
        assertNotNull(s);
        assertEquals("Regular Student", s.getStudentType());
        assertEquals(50.0, s.getPassingGrade(), 0.0001);
        assertNotNull("studentId should be auto-generated", s.getStudentId());
        assertTrue("studentId must match STU###", s.getStudentId().matches("STU\\d{3}"));
    }

    @Test
    public void recordGrade_acceptsValidValues_andCalculatesAverage() {
        RegularStudent s = new RegularStudent("Kofi Asare", 20, "kofi@example.com", "0241111111");

        assertTrue(s.recordGrade(50.0));   // boundary
        assertTrue(s.recordGrade(75.5));
        assertTrue(s.recordGrade(100.0));  // boundary

        double avg = s.calculateAverageGrade(); // (50 + 75.5 + 100) / 3 = 75.166...
        assertEquals(75.1667, avg, 0.001);
    }

    @Test
    public void recordGrade_rejectsInvalidValues_returnsFalse() {
        RegularStudent s = new RegularStudent("Ama Serwaa", 23, "ama@example.com", "0242222222");

        assertFalse("negative should be rejected", s.recordGrade(-0.1));
        assertFalse("above 100 should be rejected", s.recordGrade(100.1));
        assertFalse("NaN should be rejected", s.recordGrade(Double.NaN));

        // No grades recorded; average is 0
        assertEquals(0.0, s.calculateAverageGrade(), 0.0001);
    }

    @Test
    public void isPassing_usesAverage_vs_threshold50() {
        RegularStudent s = new RegularStudent("Yaw Boateng", 24, "yaw@example.com", "0243333333");

        // Below threshold
        assertTrue(s.recordGrade(49.0));
        assertFalse("avg 49 < 50 -> not passing", s.isPassing());

        // Exactly at threshold
        assertTrue(s.recordGrade(51.0)); // now avg = (49 + 51)/2 = 50
        assertTrue("avg 50 == 50 -> passing", s.isPassing());
    }

    @Test
    public void capacity_limit_after50Grades_nextReturnsFalse() {
        RegularStudent s = new RegularStudent("Efua Naa", 25, "efua@example.com", "0244444444");

        // Fill up to capacity
        for (int i = 0; i < 50; i++) {
            assertTrue("grade " + i + " should be stored", s.recordGrade(60.0));
        }

        // 51st grade should be rejected due to capacity
        assertFalse("exceeds capacity", s.recordGrade(70.0));

        // Average should still be computed from 50 grades
        assertEquals(60.0, s.calculateAverageGrade(), 0.0001);
    }

    @Test
    public void enrollSubject_addsUniqueCodes_duplicatesIgnored() {
        RegularStudent s = new RegularStudent("Kojo Mensah", 22, "kojo@example.com", "0245555555");

        Subject coreMath = new CoreSubject("Mathematics", "MTH101");
        Subject coreEng  = new CoreSubject("English", "ENG101");
        Subject electiveFrench = new ElectiveSubject("French", "FRE102");

        s.enrollSubject(coreMath);
        s.enrollSubject(coreEng);
        s.enrollSubject(electiveFrench);

        // Duplicate enrollments ignored by Set
        s.enrollSubject(new CoreSubject("Mathematics", "MTH101"));
        s.enrollSubject(new ElectiveSubject("French", "FRE102"));

        assertEquals("unique subject codes should be counted", 3, s.getEnrolledSubjectCount());
    }

    @Test
    public void calculateAverageGrade_returnsZero_whenNoGrades() {
        RegularStudent s = new RegularStudent("Akua Owusu", 19, "akua@example.com", "0246666666");
        assertEquals(0.0, s.calculateAverageGrade(), 0.0001);
        assertFalse("0 < 50 -> not passing", s.isPassing());
    }

    @Test
    public void getPassingGrade_is50_andTypeMatches() {
        RegularStudent s = new RegularStudent("Extra", 20, "extra@example.com", "0249999999");
        assertEquals("Regular Student", s.getStudentType());
        assertEquals(50.0, s.getPassingGrade(), 0.0001);
    }

    @Test
    public void recordGrade_returnsFalse_forInvalidValues() {
        RegularStudent s = new RegularStudent("Invalids", 20, "invalids@example.com", "0248888888");
        assertFalse(s.recordGrade(-1.0));
        assertFalse(s.recordGrade(100.1));
        assertFalse(s.recordGrade(Double.NaN));
    }

}