
package com.amalitech.menu;

import com.amalitech.manager.StudentManager;
import com.amalitech.manager.GradeManager;

/**
 * Abstraction for seeding demo/sample data into managers.
 */
public interface IDataSeeder {
    void seed(StudentManager studentManager, GradeManager gradeManager);
}
