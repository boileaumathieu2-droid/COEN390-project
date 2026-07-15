package com.example.zone.model;

import java.util.ArrayList;

public class Subject {
    private String subjectName;
    private ArrayList<String> grades;

    private long subjectID;

    public Subject(String subjectName, ArrayList<String> grades){
        this.subjectName = subjectName;
        this.grades = grades;
    }
    public String getSubjectName(){ return subjectName; }
    public ArrayList<String> getGrades(){ return grades; }
}
