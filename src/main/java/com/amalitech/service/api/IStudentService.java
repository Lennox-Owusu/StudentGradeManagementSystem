
package com.amalitech.service.api;

import com.amalitech.base.Student;
import com.amalitech.exceptions.ValidationException;

import java.util.List;

public interface IStudentService {
    Student addStudent(String id, String name, String email, String phone, boolean honors) throws ValidationException;
    List<Student> listStudents();
    Student find(String id);
}
