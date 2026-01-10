
package com.amalitech;

import com.amalitech.base.CoreSubject;
import com.amalitech.base.Subject;
import org.junit.Test;

import static org.junit.Assert.*;

public class CoreSubjectTest {

    @Test
    public void holdsNameCode_andReturnsCoreType() {
        Subject s = new CoreSubject("Mathematics", "MTH101");
        assertEquals("Mathematics", s.getSubjectName());
        assertEquals("MTH101", s.getSubjectCode());
        assertEquals("Core", s.getSubjectType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullName() {
        new CoreSubject(null, "MTH101");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankName() {
        new CoreSubject("   ", "MTH101");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullCode() {
        new CoreSubject("Mathematics", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankCode() {
        new CoreSubject("Mathematics", "   ");
    }
}