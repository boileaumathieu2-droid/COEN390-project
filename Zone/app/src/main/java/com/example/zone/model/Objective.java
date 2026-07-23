package com.example.zone.model;

public class Objective {
    private String objectiveText;
    private String objectiveDate;
    private int objectiveID;
    private String eventName;
    private String completionTime;
    private String taskType;

    public Objective(int objectiveID, String objectiveText, String objectiveDate) {
        this(objectiveID, objectiveText, objectiveDate, "", "Other", objectiveText);
    }

    public Objective(
            int objectiveID,
            String eventName,
            String objectiveDate,
            String completionTime,
            String taskType,
            String objectiveText) {
        this.objectiveID = objectiveID;
        this.objectiveText = objectiveText;
        this.objectiveDate = objectiveDate;
        this.eventName = eventName;
        this.completionTime = completionTime;
        this.taskType = taskType;
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

    public String getEventName() {
        return eventName == null || eventName.trim().isEmpty() ? objectiveText : eventName;
    }

    public String getCompletionTime() {
        return completionTime == null ? "" : completionTime;
    }

    public String getTaskType() {
        return taskType == null || taskType.trim().isEmpty() ? "Other" : taskType;
    }
}
