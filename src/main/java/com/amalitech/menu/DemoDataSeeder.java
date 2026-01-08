
package com.amalitech.menu;

import com.amalitech.manager.StudentManager;
import com.amalitech.manager.GradeManager;
import com.amalitech.model.HonorsStudent;
import com.amalitech.model.RegularStudent;

/**
 * Seeds demo students (and optionally grades) for quick testing.
 * Responsibility: seeding only (SRP).
 */
public class DemoDataSeeder implements IDataSeeder {

    @Override
    public void seed(StudentManager studentManager, GradeManager gradeManager) {
        // Students
        studentManager.addStudent(new RegularStudent("Alice Johnson", 16, "alice@school.edu", "0241108345"));
        studentManager.addStudent(new HonorsStudent("Bob Smith", 17, "bob@school.edu", "0256521345"));
        studentManager.addStudent(new RegularStudent("Carol Martinez", 15, "carol@school.edu", "0545678345"));
        studentManager.addStudent(new HonorsStudent("David Chen", 18, "david@school.edu", "0536789435"));
        studentManager.addStudent(new RegularStudent("Emma Wilson", 16, "emma@school.edu", "0237896072"));


    }
}
