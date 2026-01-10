package com.amalitech.base;


//Represents an entity capable of recording and validating grades.
public interface Gradable {

    //Records a grade for this entity
    boolean recordGrade(double grade);

    //Checks whether a grade is within acceptable bounds.
    boolean validateGrade(double grade);
}

