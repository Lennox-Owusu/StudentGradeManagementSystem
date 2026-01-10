
package com.amalitech.service.impl;

import com.amalitech.base.HonorsStudent;
import com.amalitech.base.RegularStudent;
import com.amalitech.base.Student;
import com.amalitech.manager.StudentManager;
import com.amalitech.exceptions.ValidationException;
import com.amalitech.service.api.IStudentService;
import com.amalitech.util.RegexValidators;

import java.util.Arrays;
import java.util.List;

public class StudentServiceImpl implements IStudentService {
    private final StudentManager sm;

    public StudentServiceImpl(StudentManager sm) { this.sm = sm; }

    @Override
    public Student addStudent(String id, String name, String email, String phone, boolean honors) throws ValidationException {
        // Centralized validation (ISP: dedicated validator utility)
        String sid   = RegexValidators.requireStudentId(id);
        String sname = RegexValidators.requireName(name);
        String sem   = RegexValidators.requireEmail(email);
        String ph    = RegexValidators.requirePhone(phone);

        Student s = honors
                ? new HonorsStudent(sid, sname, 16, sem, ph)
                : new RegularStudent(sid, sname, 16, sem, ph);

        sm.addStudent(s);
        return s;
    }

    @Override public List<Student> listStudents() { return Arrays.asList(sm.getStudents()); }
    @Override public Student find(String id) { return sm.findStudent(id); }
}
