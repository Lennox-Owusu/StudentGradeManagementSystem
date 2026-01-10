
package com.amalitech;

import com.amalitech.base.CoreSubject;
import com.amalitech.base.ElectiveSubject;
import com.amalitech.base.HonorsStudent;
import org.junit.Test;

import static org.junit.Assert.*;

public class HonorsStudentTest {

    @Test
    public void constructor_initializes_and_type_isHonors() {
        HonorsStudent s = new HonorsStudent("Kwaku Mensah", 22, "kwaku@example.com", "0240000003");
        assertNotNull(s);
        assertEquals("Honors Student", s.getStudentType());
        assertEquals(60.0, s.getPassingGrade(), 0.0001);
        assertNotNull(s.getStudentId());
        assertTrue(s.getStudentId().matches("STU\\d{3}"));
    }

    @Test
    public void recordGrade_acceptsValidValues_andCalculatesAverage() {
        HonorsStudent s = new HonorsStudent("Ama Serwaa", 23, "ama@example.com", "0242222222");

        assertTrue(s.recordGrade(60.0));   // boundary for passing rule (not avg rule)
        assertTrue(s.recordGrade(85.0));
        assertTrue(s.recordGrade(95.0));

        double avg = s.calculateAverageGrade(); // (60 + 85 + 95)/3 = 80.0
        assertEquals(80.0, avg, 0.0001);
    }

    @Test
    public void recordGrade_rejectsInvalidValues_returnsFalse() {
        HonorsStudent s = new HonorsStudent("Yaw Boateng", 24, "yaw@example.com", "0243333333");

        assertFalse(s.recordGrade(-1.0));
        assertFalse(s.recordGrade(100.5));
        assertFalse(s.recordGrade(Double.NaN));
        assertEquals(0.0, s.calculateAverageGrade(), 0.0001);
    }

    @Test
    public void isPassing_usesAverage_vs_threshold60() {
        HonorsStudent s = new HonorsStudent("Efua Naa", 25, "efua@example.com", "efua@example.com");

        // Below threshold
        assertTrue(s.recordGrade(59.0));
        assertFalse("avg 59 < 60 -> not passing", s.isPassing());

        // On threshold
        assertTrue(s.recordGrade(61.0)); // now avg = (59 + 61)/2 = 60
        assertTrue("avg 60 == 60 -> passing", s.isPassing());
    }

    @Test
    public void honorsEligibility_trueWhenAverageAtLeast85() {
        HonorsStudent s = new HonorsStudent("Kojo Mensah", 22, "kojo@example.com", "0245555555");

        // Make average exactly 85
        assertTrue(s.recordGrade(85.0));
        assertTrue(s.checkHonorsEligibility());
    }

    @Test
    public void honorsEligibility_falseWhenAverageBelow85() {
        HonorsStudent s = new HonorsStudent("Akua Owusu", 19, "akua@example.com", "0246666666");

        assertTrue(s.recordGrade(84.99));
        assertFalse(s.checkHonorsEligibility());
    }

    @Test
    public void honorsEligibility_trueWhenAverageAbove85() {
        HonorsStudent s = new HonorsStudent("Yaw Boateng", 24, "yaw@example.com", "0243333333");

        assertTrue(s.recordGrade(90.0));
        assertTrue(s.checkHonorsEligibility());
    }

    @Test
    public void enrollSubject_countsUniqueCodes() {
        HonorsStudent s = new HonorsStudent("Kwesi Anane", 23, "kwesi@example.com", "0247777777");

        s.enrollSubject(new CoreSubject("Mathematics", "MTH101"));
        s.enrollSubject(new CoreSubject("English", "ENG101"));
        s.enrollSubject(new ElectiveSubject("Music", "MUS110"));

        // duplicates ignored
        s.enrollSubject(new CoreSubject("Mathematics", "MTH101"));

        assertEquals(3, s.getEnrolledSubjectCount());
    }

    @Test
    public void getPassingGrade_is60_andTypeMatches() {
        HonorsStudent s = new HonorsStudent("ExtraH", 23, "extrah@example.com", "0247777777");
        assertEquals("Honors Student", s.getStudentType());
        assertEquals(60.0, s.getPassingGrade(), 0.0001);
    }

    @Test
    public void honorsEligibility_thresholds_falseBelow_trueAt_trueAbove() {
        HonorsStudent s = new HonorsStudent("Elig", 23, "elig@example.com", "0246666666");

        assertTrue(s.recordGrade(84.99));
        assertFalse("Below 85 -> not eligible", s.checkHonorsEligibility());

        // reset scenario: make average exactly 85 with another grade
        assertTrue(s.recordGrade(85.01)); // (84.99 + 85.01)/2 = 85.0
        assertTrue("Exactly 85 -> eligible", s.checkHonorsEligibility());

        // add another high to push above 85
        assertTrue(s.recordGrade(90.0));
        assertTrue("Above 85 -> eligible", s.checkHonorsEligibility());
    }

    @Test
    public void honorsRecordGrade_invalidValuesReturnFalse() {
        HonorsStudent s = new HonorsStudent("InvH", 24, "invh@example.com", "0245555555");
        assertFalse(s.recordGrade(-0.1));
        assertFalse(s.recordGrade(100.5));
        assertFalse(s.recordGrade(Double.NaN));

    }
}