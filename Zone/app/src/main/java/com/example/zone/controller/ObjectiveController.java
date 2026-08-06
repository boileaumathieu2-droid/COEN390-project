package com.example.zone.controller;

import com.example.zone.model.Database;
import com.example.zone.model.Objective;

import java.util.ArrayList;

public class ObjectiveController {
    private Database database;
    public ObjectiveController(Database database) {this.database = database;}

    public long addObjective(String userID, String text, String date){
        return database.addObjective(userID, text, date);
    }

    public long addTask(
            String userID,
            String eventName,
            String dueDate,
            String completionTime,
            String taskType,
            String objectives) {
        return database.addTask(
                userID, eventName, dueDate, completionTime, taskType, objectives);
    }

    public boolean deleteObjective(String objectiveID){
        return database.deleteObjective(objectiveID);
    }

    public boolean updateObjective(String objectiveID, String text, String date) {
        return database.updateObjective(objectiveID, text, date);
    }

    public boolean updateTask(
            String objectiveID,
            String eventName,
            String dueDate,
            String completionTime,
            String taskType,
            String objectives) {
        return database.updateTask(
                objectiveID, eventName, dueDate, completionTime, taskType, objectives);
    }

    public ArrayList<Objective> getObjectives(int userID){
        return database.getObjectives(userID);
    }

    public ArrayList<Objective> getObjectivesForDate(int userID, String date){
        return database.getObjectivesForDate(userID, date);
    }

    public ArrayList<Objective> getObjectivesForFuture(int userID, String date){
        return database.getObjectivesForFuture(userID, date);
    }

    public int deletePastObjectives(String userID, String date){
        return database.deletePastObjectives(userID, date);
    }

}
