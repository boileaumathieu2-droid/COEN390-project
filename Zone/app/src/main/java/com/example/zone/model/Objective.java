package com.example.zone.model;

public class Objective {
    private String objectiveText;
    private String objectiveDate;

    private int objectiveID;


    public Objective(int objectiveID, String objectiveText, String objectiveDate) {
        this.objectiveID = objectiveID;
        this.objectiveText = objectiveText;
        this.objectiveDate = objectiveDate;
    }

    String getObjectiveText(){
        return objectiveText;
    }
    String getObjectiveDate(){
        return objectiveDate;
    }
    int getObjectiveID(){
        return objectiveID;
    }
}
