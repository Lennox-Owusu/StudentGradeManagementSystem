package com.amalitech.model;

//Represents a mandatory core subject that all students must take.
public class CoreSubject extends Subject {

    public CoreSubject(String subjectName, String subjectCode) {
        super(subjectName, subjectCode);
    }

    @Override
    public String getSubjectType() {

        return "Core";
    }

}

