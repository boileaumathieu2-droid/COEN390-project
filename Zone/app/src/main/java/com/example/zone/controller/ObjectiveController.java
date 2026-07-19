package com.example.zone.controller;

import com.example.zone.model.Database;
import com.example.zone.model.Objective;

import java.util.ArrayList;

public class ObjectiveController {
    private Database database;
    public ObjectiveController(Database database) {this.database = database;}

    public long addObjective(int userID, String text, String date){
        return database.addObjective(userID, text, date);
    }

    public boolean deleteObjective(int objectiveID){
        return database.deleteObjective(objectiveID);
    }

    public ArrayList<Objective> getObjectives(int userID){
        return database.getObjectives(userID);
    }

    public ArrayList<Objective> getObjectivesforDate(int userID, String date){
        return database.getObjectivesForDate(userID, date);
    }

}
