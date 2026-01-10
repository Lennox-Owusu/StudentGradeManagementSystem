
package com.amalitech;

import com.amalitech.base.ElectiveSubject;
import com.amalitech.base.Subject;
import org.junit.Test;

import static org.junit.Assert.*;

public class ElectiveSubjectTest {

    @Test
    public void holdsNameCode_andReturnsElectiveType() {
        Subject s = new ElectiveSubject("Music", "MUS110");
        assertEquals("Music", s.getSubjectName());
        assertEquals("MUS110", s.getSubjectCode());
        assertEquals("Elective", s.getSubjectType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullName() {
        new ElectiveSubject(null, "MUS110");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankName() {
        new ElectiveSubject("   ", "MUS110");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullCode() {
        new ElectiveSubject("Music", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankCode() {
        new ElectiveSubject("Music", "   ");
    }
}
