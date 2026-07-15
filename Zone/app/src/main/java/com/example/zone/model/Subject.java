package com.example.zone.model;

import java.util.ArrayList;

public class Subject {
    private String subjectName;
    private ArrayList<String> grades;

    private int subjectID;

    public Subject(int subjectID, String subjectName, ArrayList<String> grades) {
        this.subjectName = subjectName;
        this.grades = grades;
        this.subjectID = subjectID;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public ArrayList<String> getGrades() {
        return grades;
    }

    public int getSubjectID() {
        return subjectID;
    }
}
