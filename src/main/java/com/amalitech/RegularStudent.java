package com.amalitech;

//Represents a regular student with standard passing grade logic.
public class RegularStudent extends Student {

    //Constructs a regular student with personal details.
    public RegularStudent(String name, int age, String email, String phone)
    {
        super(name, age, email);
    }


    public RegularStudent(String studentId, String name, int age, String email, String phone) {
        super(studentId, name, age, email);
    }



    @Override
    public String getStudentType() { return "Regular Student"; }

    @Override
    public double getPassingGrade() {
        return 50.0; }
}

