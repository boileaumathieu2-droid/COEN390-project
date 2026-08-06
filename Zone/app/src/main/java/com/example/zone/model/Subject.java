package com.example.zone.model;

import java.util.ArrayList;

public class Subject {
    private String subjectName;
    private ArrayList<Grades> grades;
    private String subjectID;

    public Subject(String subjectName, String subjectId) {
        this.subjectName = subjectName;
        subjectID = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public ArrayList<Grades> getGrades() {
        return grades;
    }

    public String getSubjectID() {
        return subjectID;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public void setGrades(ArrayList<Grades> grades) {
        this.grades = grades;
    }

    public void setSubjectID(String subjectID) {
        this.subjectID = subjectID;
    }
}


class Grades {
    private String grade;
    private String type;
    private String gradeID;

    public Grades(String grade, String type, String gradeID) {
        this.grade = grade;
        this.type = type;
        this.gradeID = gradeID;
    }

    public String getGrade() {
        return grade;
    }
    public String getType() {
        return type;
    }
    public String getGradeID() {
        return gradeID;
    }
    public void setGrade(String grade) {
        this.grade = grade;
    }

    public void setType(String type) {
        this.type = type;
    }
}