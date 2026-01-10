
package com.amalitech;

import com.amalitech.base.RegularStudent;
import com.amalitech.reporting.ReportGenerator;
import org.junit.Test;
import static org.junit.Assert.*;

public class ReportGeneratorTest {

    @Test
    public void generates_basic_student_report() {
        RegularStudent s = new RegularStudent("Esi Boakye", 21, "esi@example.com", "0240000002");
        s.recordGrade(80);
        s.recordGrade(70);

        ReportGenerator gen = new ReportGenerator();
        String report = gen.generateStudentReport(s);

        assertNotNull(report);
        assertTrue(report.contains("Student Report"));
        assertTrue(report.contains("Esi Boakye"));
        assertTrue(report.contains("Regular Student"));
        assertTrue(report.contains("Average Grade"));
        assertTrue(report.contains("Passing"));
    }
}
