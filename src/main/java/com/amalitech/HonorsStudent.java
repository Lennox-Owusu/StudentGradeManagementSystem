package com.amalitech;


//Represents an honors student with additional eligibility checks.
public class HonorsStudent extends Student {

    //Constructs an honors student with personal details.
    public HonorsStudent(String name, int age, String email, String phone) {
        super(name, age, email);
    }


    @Override
    public String getStudentType() { return "Honors Student"; }


    //return the passing grade threshold for honors students (60%)
    @Override
    public double getPassingGrade() {
        return 60.0; }


    //return true if average grade >= 85
    public boolean checkHonorsEligibility() {
        return calculateAverageGrade() >= 85.0;
    }
}

