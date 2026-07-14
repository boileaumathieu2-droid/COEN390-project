package com.example.zone.controller;

import com.example.zone.model.Database;
import com.example.zone.model.Subject;
import com.example.zone.view.GradesTrackerView;

import java.util.ArrayList;

public class GradesTracker {
    private Database database;
    public GradesTracker(Database database) {this.database = database;}

    public long addSubject(Subject subject, int userID) {

        return database.addSubject(
                userID,
                subject.getSubjectName()
        );
    }
    public ArrayList<Subject> getSubjects(int userID) {
        return database.getSubjects(userID);
    }
}
