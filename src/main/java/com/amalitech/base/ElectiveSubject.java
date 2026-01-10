package com.amalitech.base;

//Represents an elective (non-mandatory) subject a student may choose.
public class ElectiveSubject extends Subject {


    //Constructs an elective subject with a name and code.
    public ElectiveSubject(String subjectName, String subjectCode) {
        super(subjectName, subjectCode);
    }


    //@return "Elective" to identify the subject type.
    @Override
    public String getSubjectType() {

        return "Elective";
    }

}

