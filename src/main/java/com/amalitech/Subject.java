package com.amalitech;



public abstract class Subject {
    private final String subjectName;
    private final String subjectCode;

    protected Subject(String subjectName, String subjectCode) {
        if (subjectName == null || subjectName.isBlank()) {
            throw new IllegalArgumentException("subjectName must not be null or blank");
        }
        if (subjectCode == null || subjectCode.isBlank()) {
            throw new IllegalArgumentException("subjectCode must not be null or blank");
        }
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() { return subjectName; }
    public String getSubjectCode() { return subjectCode; }

    //@return "Elective" to identify the subject type.
    public abstract String getSubjectType();
}


