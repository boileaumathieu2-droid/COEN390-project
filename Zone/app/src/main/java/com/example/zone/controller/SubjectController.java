package com.example.zone.controller;

import com.example.zone.model.Database;

import java.util.ArrayList;

public class SubjectController {
    private  Database database;
    public SubjectController(Database database){this.database = database;}
    public boolean addGrade(long subjectID, String grade){
        return database.addGrade(subjectID, grade);
    }

    public ArrayList<String> getGrades(int subjectId){
        return database.getGrades(subjectId);
    }

    public boolean deleteSubject(int subjectID){
        return database.deleteSubject(subjectID);
    }
}
