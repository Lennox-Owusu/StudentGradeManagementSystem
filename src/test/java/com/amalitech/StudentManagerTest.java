
package com.amalitech;

import org.junit.Test;

import static org.junit.Assert.*;

public class StudentManagerTest {

    @Test
    public void addStudents_incrementsCount_andStoresReferences() {
        StudentManager mgr = new StudentManager(3);

        RegularStudent r = new RegularStudent("Esi Boakye", 21, "esi@example.com", "0240000002");
        HonorsStudent h = new HonorsStudent("Kwaku Mensah", 22, "kwaku@example.com", "0240000003");

        mgr.addStudent(r);
        mgr.addStudent(h);

        assertEquals(2, mgr.getStudentCount());

        Student[] all = mgr.getStudents();
        assertNotNull(all);
        assertEquals(2, all.length);
        assertSame("First student should be the same reference", r, all[0]);
        assertSame("Second student should be the same reference", h, all[1]);
    }

    @Test
    public void findStudent_byId_isCaseInsensitive_andReturnsNullWhenMissing() {
        StudentManager mgr = new StudentManager(2);
        RegularStudent r = new RegularStudent("Ama Serwaa", 23, "ama@example.com", "0242222222");

        mgr.addStudent(r);

        String ridUpper = r.getStudentId();     // e.g., STU001
        String ridLower = ridUpper.toLowerCase();

        Student foundUpper = mgr.findStudent(ridUpper);
        Student foundLower = mgr.findStudent(ridLower);

        assertNotNull(foundUpper);
        assertNotNull(foundLower);
        assertSame(r, foundUpper);
        assertSame(r, foundLower);

        assertNull("Non-existent ID returns null", mgr.findStudent("STU999"));
    }

    @Test
    public void addStudent_whenFull_doesNotIncreaseCount() {
        StudentManager mgr = new StudentManager(2);

        RegularStudent s1 = new RegularStudent("Kojo", 20, "kojo@example.com", "0241111111");
        HonorsStudent s2 = new HonorsStudent("Akua", 21, "akua@example.com", "0242222222");
        RegularStudent s3 = new RegularStudent("Yaw", 22, "yaw@example.com", "0243333333");

        mgr.addStudent(s1);
        mgr.addStudent(s2);
        assertEquals(2, mgr.getStudentCount());

        // This will print "Student list is full.", but not add
        mgr.addStudent(s3);
        assertEquals(2, mgr.getStudentCount());
        assertNull("Third student not stored; should not be found", mgr.findStudent(s3.getStudentId()));
    }

    @Test
    public void getStudents_returnsACopy_notTheInternalArray() {
        StudentManager mgr = new StudentManager(2);

        RegularStudent s1 = new RegularStudent("Ama", 23, "ama@example.com", "0242222222");
        HonorsStudent s2 = new HonorsStudent("Yaw", 24, "yaw@example.com", "0243333333");

        mgr.addStudent(s1);
        mgr.addStudent(s2);

        Student[] snapshot = mgr.getStudents();
        assertEquals(2, snapshot.length);

        // Mutate the snapshot; this must not affect the manager's internal storage
        snapshot[0] = null;

        Student[] snapshot2 = mgr.getStudents();
        assertNotNull("Manager should not be affected by external mutation", snapshot2[0]);
        assertSame(s1, snapshot2[0]);
        assertSame(s2, snapshot2[1]);
    }

    @Test
    public void getAverageClassGrade_usesEachStudentsAverage() {
        StudentManager mgr = new StudentManager(3);

        RegularStudent s1 = new RegularStudent("Esi", 21, "esi@example.com", "0240000002");
        HonorsStudent s2 = new HonorsStudent("Kwaku", 22, "kwaku@example.com", "0240000003");
        RegularStudent s3 = new RegularStudent("Kojo", 20, "kojo@example.com", "0241111111");

        mgr.addStudent(s1);
        mgr.addStudent(s2);
        mgr.addStudent(s3);

        // s1 average: (50 + 100) / 2 = 75
        assertTrue(s1.recordGrade(50.0));
        assertTrue(s1.recordGrade(100.0));

        // s2 average: (60 + 90) / 2 = 75
        assertTrue(s2.recordGrade(60.0));
        assertTrue(s2.recordGrade(90.0));

        // s3 average: no grades -> 0
        double classAvg = mgr.getAverageClassGrade();
        assertEquals("Average of (75 + 75 + 0) / 3", 50.0, classAvg, 0.0001);
    }

    @Test
    public void getAverageClassGrade_returnsZero_whenNoStudents() {
        StudentManager mgr = new StudentManager(5);
        assertEquals(0.0, mgr.getAverageClassGrade(), 0.0001);
    }

    // ---------- EDGE CASES BELOW ----------

    @Test
    public void capacityZero_managerCannotStoreAnyStudent() {
        StudentManager mgr = new StudentManager(0);
        RegularStudent r = new RegularStudent("Esi", 21, "esi@example.com", "0240000002");

        mgr.addStudent(r); // prints "Student list is full."
        assertEquals(0, mgr.getStudentCount());
        assertNull(mgr.findStudent(r.getStudentId()));
        assertEquals("Returned snapshot must be empty", 0, mgr.getStudents().length);
    }

    @Test
    public void findStudent_onEmptyManager_returnsNull() {
        StudentManager mgr = new StudentManager(3);
        assertNull(mgr.findStudent("STU001"));
    }

    @Test
    public void ids_areUnique_forMultipleAddedStudents() {
        StudentManager mgr = new StudentManager(3);

        RegularStudent s1 = new RegularStudent("A", 18, "a@example.com", "0240000001");
        RegularStudent s2 = new RegularStudent("B", 19, "b@example.com", "0240000002");

        mgr.addStudent(s1);
        mgr.addStudent(s2);

        assertNotEquals("IDs should differ", s1.getStudentId(), s2.getStudentId());
        assertTrue(s1.getStudentId().matches("STU\\d{3}"));
        assertTrue(s2.getStudentId().matches("STU\\d{3}"));
    }

    @Test
    public void averageClass_withExtremeGrades_andOneMidValue() {
        StudentManager mgr = new StudentManager(3);

        RegularStudent s1 = new RegularStudent("Low", 20, "low@example.com", "0241000000");
        RegularStudent s2 = new RegularStudent("High", 20, "high@example.com", "0242000000");
        RegularStudent s3 = new RegularStudent("Mid", 20, "mid@example.com", "0243000000");

        mgr.addStudent(s1);
        mgr.addStudent(s2);
        mgr.addStudent(s3);

        assertTrue(s1.recordGrade(0.0));     // avg 0
        assertTrue(s2.recordGrade(100.0));   // avg 100
        assertTrue(s3.recordGrade(50.0));    // avg 50

        double avg = mgr.getAverageClassGrade();
        assertEquals("(0 + 100 + 50) / 3 = 50", 50.0, avg, 0.0001);
    }

    @Test
    public void getStudents_lengthMatchesCount_afterPartialFill() {
        StudentManager mgr = new StudentManager(5);

        RegularStudent s1 = new RegularStudent("X", 18, "x@example.com", "0240000001");
        RegularStudent s2 = new RegularStudent("Y", 19, "y@example.com", "0240000002");

        mgr.addStudent(s1);
        mgr.addStudent(s2);

        assertEquals(2, mgr.getStudentCount());
        assertEquals("Snapshot should contain only filled entries", 2, mgr.getStudents().length);
    }
}

