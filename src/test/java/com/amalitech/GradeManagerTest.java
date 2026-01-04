
package com.amalitech;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.regex.Pattern;

public class GradeManagerTest {

    @Test
    public void addGrade_incrementsCount_andGetGradeAtReturnsStoredGrade() {
        GradeManager gm = new GradeManager(5);

        RegularStudent s = new RegularStudent("Esi Boakye", 21, "esi@example.com", "0240000002");
        String sid = s.getStudentId();

        Subject math = new CoreSubject("Mathematics", "MTH101");
        Grade g1 = new Grade(sid, math, 90.0);

        gm.addGrade(g1);

        assertEquals(1, gm.getGradeCount());
        Grade fetched = gm.getGradeAt(0);
        assertNotNull(fetched);
        assertSame("Should be the same object reference", g1, fetched);
        assertEquals("MTH101", fetched.getSubject().getSubjectCode());
        assertEquals(90.0, fetched.getGrade(), 0.0001);

        // GradeId and Date basic checks
        assertTrue(fetched.getGradeId().startsWith("GRD"));
        assertTrue("Date should be yyyy-MM-dd",
                Pattern.compile("\\d{4}-\\d{2}-\\d{2}").matcher(fetched.getDate()).matches());
    }

    @Test
    public void getGradeAt_outOfBounds_returnsNull() {
        GradeManager gm = new GradeManager(2);

        RegularStudent s = new RegularStudent("Ama Serwaa", 23, "ama@example.com", "0242222222");
        Subject sci = new CoreSubject("Science", "SCI101");

        gm.addGrade(new Grade(s.getStudentId(), sci, 75.0));

        assertNull("Negative index -> null", gm.getGradeAt(-1));
        assertNull("Index equal to count -> null", gm.getGradeAt(1));
        assertNotNull("Index 0 -> valid", gm.getGradeAt(0));
    }

    @Test
    public void addGrade_whenFull_doesNotIncreaseCount() {
        GradeManager gm = new GradeManager(2);

        RegularStudent s = new RegularStudent("Yaw Boateng", 24, "yaw@example.com", "0243333333");
        Subject math = new CoreSubject("Mathematics", "MTH101");

        gm.addGrade(new Grade(s.getStudentId(), math, 60.0));
        gm.addGrade(new Grade(s.getStudentId(), math, 70.0));

        assertEquals(2, gm.getGradeCount());

        // Attempt to add third grade; capacity is 2
        gm.addGrade(new Grade(s.getStudentId(), math, 80.0));
        assertEquals("Count should remain at capacity", 2, gm.getGradeCount());
        assertNull("Index 2 should be null/out of bounds", gm.getGradeAt(2));
    }

    @Test
    public void calculateCoreAverage_filtersByTypeCore_onlyForStudent() {
        GradeManager gm = new GradeManager(10);
        RegularStudent s = new RegularStudent("Kojo Mensah", 22, "kojo@example.com", "0245555555");
        String sid = s.getStudentId();

        Subject coreMath = new CoreSubject("Mathematics", "MTH101");
        Subject coreEng = new CoreSubject("English", "ENG101");
        Subject electiveFrench = new ElectiveSubject("French", "FRE102");

        // Core grades for the student
        gm.addGrade(new Grade(sid, coreMath, 80.0));
        gm.addGrade(new Grade(sid, coreEng, 60.0));

        // Elective grade for the same student should be ignored by core average
        gm.addGrade(new Grade(sid, electiveFrench, 100.0));

        // Grade for another student should be ignored
        gm.addGrade(new Grade("STU999", coreMath, 90.0));

        double coreAvg = gm.calculateCoreAverage(sid);
        assertEquals("(80 + 60) / 2 = 70.0", 70.0, coreAvg, 0.0001);
    }

    @Test
    public void calculateElectiveAverage_filtersByTypeElective_onlyForStudent() {
        GradeManager gm = new GradeManager(10);
        HonorsStudent s = new HonorsStudent("Akua Owusu", 19, "akua@example.com", "0246666666");
        String sid = s.getStudentId();

        Subject electiveMusic = new ElectiveSubject("Music", "MUS110");
        Subject electiveFrench = new ElectiveSubject("French", "FRE102");
        Subject coreSci = new CoreSubject("Science", "SCI101");

        gm.addGrade(new Grade(sid, electiveMusic, 85.0));
        gm.addGrade(new Grade(sid, electiveFrench, 95.0));

        // Core grade should be ignored for elective average
        gm.addGrade(new Grade(sid, coreSci, 50.0));

        double electiveAvg = gm.calculateElectiveAverage(sid);
        assertEquals("(85 + 95) / 2 = 90.0", 90.0, electiveAvg, 0.0001);
    }

    @Test
    public void calculateOverallAverage_countsAllGrades_forStudentOnly() {
        GradeManager gm = new GradeManager(10);
        RegularStudent s = new RegularStudent("Efua Naa", 25, "efua@example.com", "0244444444");
        String sid = s.getStudentId();

        Subject math = new CoreSubject("Mathematics", "MTH101");
        Subject french = new ElectiveSubject("French", "FRE102");

        gm.addGrade(new Grade(sid, math, 60.0));
        gm.addGrade(new Grade(sid, french, 90.0));
        gm.addGrade(new Grade("STU999", math, 100.0)); // other student

        double overallAvg = gm.calculateOverallAverage(sid);
        assertEquals("(60 + 90) / 2 = 75.0", 75.0, overallAvg, 0.0001);
    }

    @Test
    public void gradeIds_areUnique_acrossMultipleGrades() {
        GradeManager gm = new GradeManager(5);
        RegularStudent s = new RegularStudent("Kwesi Anane", 23, "kwesi@example.com", "0247777777");
        String sid = s.getStudentId();
        Subject math = new CoreSubject("Mathematics", "MTH101");

        Grade g1 = new Grade(sid, math, 60.0);
        Grade g2 = new Grade(sid, math, 70.0);

        gm.addGrade(g1);
        gm.addGrade(g2);

        assertNotEquals("Each Grade should have a unique ID", g1.getGradeId(), g2.getGradeId());
    }

    // ---------- EDGE CASES BELOW ----------

    @Test
    public void capacityZero_managerCannotStoreAnyGrade() {
        GradeManager gm = new GradeManager(0);

        RegularStudent s = new RegularStudent("Empty", 20, "empty@example.com", "0240000000");
        Subject math = new CoreSubject("Mathematics", "MTH101");
        Grade g = new Grade(s.getStudentId(), math, 100.0);

        gm.addGrade(g); // ignored due to capacity 0
        assertEquals(0, gm.getGradeCount());
        assertNull(gm.getGradeAt(0));
    }

    @Test
    public void calculateCoreAverage_returnsZero_whenNoCoreGradesForStudent() {
        GradeManager gm = new GradeManager(5);

        RegularStudent s = new RegularStudent("NoCore", 21, "nocore@example.com", "0241010101");
        String sid = s.getStudentId();

        Subject electiveMusic = new ElectiveSubject("Music", "MUS110");

        gm.addGrade(new Grade(sid, electiveMusic, 80.0)); // only elective
        assertEquals(0.0, gm.calculateCoreAverage(sid), 0.0001);
    }

    @Test
    public void calculateElectiveAverage_returnsZero_whenNoElectiveGradesForStudent() {
        GradeManager gm = new GradeManager(5);

        RegularStudent s = new RegularStudent("NoElective", 22, "noel@example.com", "0242020202");
        String sid = s.getStudentId();

        Subject coreSci = new CoreSubject("Science", "SCI101");

        gm.addGrade(new Grade(sid, coreSci, 70.0)); // only core
        assertEquals(0.0, gm.calculateElectiveAverage(sid), 0.0001);
    }

    @Test
    public void calculateOverallAverage_returnsZero_whenNoGradesForStudent() {
        GradeManager gm = new GradeManager(5);

        RegularStudent s = new RegularStudent("None", 23, "none@example.com", "0243030303");
        String sid = s.getStudentId();

        assertEquals(0.0, gm.calculateOverallAverage(sid), 0.0001);
    }

    @Test
    public void averages_areCaseInsensitive_onStudentId() {
        GradeManager gm = new GradeManager(5);

        RegularStudent s = new RegularStudent("Case", 23, "case@example.com", "0249090909");
        String sidUpper = s.getStudentId();        // e.g. STU###
        String sidLower = sidUpper.toLowerCase();  // same letters in lower case

        Subject coreSci = new CoreSubject("Science", "SCI101");
        Subject electiveArt = new ElectiveSubject("Art", "ART101");

        gm.addGrade(new Grade(sidUpper, coreSci, 50.0));
        gm.addGrade(new Grade(sidUpper, electiveArt, 100.0));

        // Using lower-case ID should still match due to equalsIgnoreCase in manager
        assertEquals(50.0, gm.calculateCoreAverage(sidLower), 0.0001);
        assertEquals(100.0, gm.calculateElectiveAverage(sidLower), 0.0001);
        assertEquals(75.0, gm.calculateOverallAverage(sidLower), 0.0001);
    }

    @Test
    public void boundaryGrades_zeroAndHundred_areValidInAverages() {
        GradeManager gm = new GradeManager(5);

        RegularStudent s = new RegularStudent("Bounds", 22, "bounds@example.com", "0248080808");
        String sid = s.getStudentId();

        Subject math = new CoreSubject("Mathematics", "MTH101");
        Subject eng = new CoreSubject("English", "ENG101");

        gm.addGrade(new Grade(sid, math, 0.0));
        gm.addGrade(new Grade(sid, eng, 100.0));

        assertEquals(50.0, gm.calculateCoreAverage(sid), 0.0001);
        assertEquals(50.0, gm.calculateOverallAverage(sid), 0.0001);
    }

    @Test
    public void date_isTodayIso_whenGradeAdded() {
        GradeManager gm = new GradeManager(1);

        RegularStudent s = new RegularStudent("Date", 20, "date@example.com", "0247070707");
        Subject math = new CoreSubject("Mathematics", "MTH101");

        Grade g = new Grade(s.getStudentId(), math, 88.0);
        gm.addGrade(g);

        Grade fetched = gm.getGradeAt(0);
        assertNotNull(fetched);
        // Already validated format elsewhere; here we just ensure it's not empty
        assertNotNull(fetched.getDate());
        assertTrue("Date string must have length >= 10", fetched.getDate().length() >= 10);
    }
}