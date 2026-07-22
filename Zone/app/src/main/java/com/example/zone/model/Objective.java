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

    public void setObjectiveText(String objectiveText){
        this.objectiveText = objectiveText;
    }

    public void setObjectiveDate(String objectiveDate){
        this.objectiveDate = objectiveDate;
    }

    public String getObjectiveText(){
        return objectiveText;
    }
    public String getObjectiveDate(){
        return objectiveDate;
    }
    public int getObjectiveID(){
        return objectiveID;
    }
}
