package com.example.zone.controller;

import com.example.zone.model.Database;
import com.example.zone.model.Subject;
import com.example.zone.view.GradesTrackerView;

import java.util.ArrayList;

public class GradesTracker {
    private Database database;
    public GradesTracker(Database database) {this.database = database;}

    public void addSubject(String subjectName, int userID) {
        database.addSubject(userID, subjectName);
    }

    public boolean subjectAlreadyExists(int userID, String subjectName) {
        return database.subjectAlreadyExists(userID, subjectName);
    }
    public ArrayList<Subject> getSubjects(int userID) {
        return database.getSubjects(userID);
    }
}
